https://mangkyu.tistory.com/118

## 가비지 컬렉션과 Heap

JVM의 Heap 영역은 처음 설계될 때 다음 2가지를 전제로 설계되었다고 함

- 대부분의 객체는 금방 접근 불가능한 상태가 된다.

- 오래된 객체에서 새로운 객체로의 참조는 아주 적게 존재한다.

즉, 객체는 대부분 일회성이며, 메모리에 오랫동안 남아있는 경우는 드물다는 것이다.

그래서 객체의 생존기간에 따라 물리적인 Heap 영역을 나누게 되었고

Young 과 Old로 나누었다.

- Young 영역

  - 새롭게 생성된 객체가 할당되는 영역
  - 대부분의 객체가 금방 Unreachable 상태가 되기 때문에, 많은 객체가 Young 영역에서 생성되었다가 사라진다.
  - Young 영역에 대한 GC를 Minor GC라고 부른다.

- Old 영역

  - Young 영역에서 Reachable 상태를 유지하여 살아남은 객체가 복사되는 영역
  - Young 영역보다 크게 할당되며, 영역의 크기가 큰 만큼 가비지는 적게 발생한다.
  - Old 영역에 대한 GC를 Major GC(Full GC)라고 부른다.

Old영역이 Young 영역보다 크게 할당되는 이유는

Young 영역의 수명이 짧은 객체들은 큰 공간을 필요로 하지 않으며, 어짜피 큰 객체들은 바로 Old로 넘어가기 때문이다.

예외적인 상황으로 Old 영역에 있는 객체가 Young 영역의 객체를 참조하는 경우에는

Chunk로 되어있는 카드테이블을 사용한다.

카드테이블에는 Old영역에 있는 객체가 Young 영역의 객체를 참조할 때 마다 참조하고 있다는 정보가 표시된다.

카드테이블이 있어야하는 이유는 Minor GC가 일어날때 마다 모든 Old 객체를 검사하여 참조되지 않는 Young 영역 객체를 식별하는 것이 비효율적이기 때문이다.

결국 Minor GC가 일어날때, 큰 크기인 Old가 아니라 카드 테이블만을 조회하여 GC의 대상인지 식별할수 있게 된다.

## 가비지 컬렉션동작방식

가비지 컬렉션은 Minor GC 와 Full GC에 따라 세부적으로 나뉘지만

기본적으로 `Stop The World`와 `Mark and Sweep`의 단계를 거치게 되어있다.

### `Stop The World`

GC를 실행하기 위해 JVM이 애플리케이션의 실행을 멈추는 작업

GC를 실행하는 쓰레드(데몬 쓰레드임)를 제외하고 모든 쓰레드는 작업이 중단된다.

사실상 애플리케이션이 중단되는것과 다름없기에, 이 시간을 줄여나가는 것이 GC 튜닝이다.

### `Mark and Sweep`

`Stop The World`가 일어나면, GC는 스택의 모든 변수 또는 Reachable 객체를 스캔하면서 각각이 어떤 객체를 참조하고 있는지 탐색한다.

여기서 사용되고 있는 객체들에 대해서 `Mark` 하고, `Mark`가 되지않은 객체들을 메모리에서 제거하는 과정을 `Sweep`이라고 한다.

### Minor GC

Young 영역은 크기가 작으면서, 생성된지 얼마안된(새로 생성된것 까지 포함한) 객체들이 존재하는 곳이다.

더욱 영역을 나누면 Eden, Survivor 로 나눠진다.

여기서 Eden은 새로생성된 객체가 할당되는 영역이고

Survivor은 최소 1번의 GC 이상 살아남은 객체가 존재하는 영역이다. Survivor 영역은 2개 있다.

Minor GC는 Eden 영역이 꽉차게 되면 발생한다.

여기서 더이상 참조되지 않는 객체는 메모리가 해제되고, 그밖의 객체는 Survivor 영역으로 이동한다.

여기서 2개의 Survivor 영역중 한쪽에만 데이터가 존재해야 한다.

그리고 객체의 생존횟수가 일정이상을 넘어가면 Old 영역으로 넘어가고

해당 생존횟수를 카운트하는 변수가 `age bit`이다. 이 변수는 `Object Header`에 기록되어 있다고 한다.

Minor GC의 평균 속도는 약 500ms ~ 1sec 라고 한다.

### Major GC

위의 상황에서 Old 영역으로 계속해서 넘어가면, Old 영역의 크기가 꽉차게 될 것이다.

이때 Major GC가 발생하게 된다.

Major GC의 경우는 보통 Minor GC보다 시간이 오래걸리며 10배 이상의 시간을 사용한다고 한다.

참고로 Young 영역과 Old 영역을 동시에 처리하는 GC는 Full GC라고 한다.

### 왜 Survivor영역이 2개일까

https://medium.com/javarevisited/understanding-garbage-collection-in-java-java2blog-639bceaa4426

여기에 Eden영역에 있다가 Minor GC가 발생하고

또다시 Eden영역이 꽉차서 Minor GC가 한번 더 발생했을 때를 가정한다.

만약 Eden과 Survivor 이 1개씩 있었다면, 연속적인 메모리 블럭의 크기가 매우 작게 형성될 수 밖에 없을 것이다.

중간중간에 할당해제된 메모리들에 의해 구멍이 난 상태를 파편화 상태라고 한다.

이러한 상태를 줄이기 위해 Survivor 공간이 2개 있는것이다.

### Object Header

https://m.blog.naver.com/gngh0101/222322390483

자바에 배열을 제외한 모든 객체는 Mark와 Klass로 이루어져 있다

Mark는 해시코드, GC에 사용되는 age bit, Lock에 사용되는 비트로 구성되어 있고

Klass는 클래스 메타정보에 대한 포인터가 저장되어 있다고 한다.

## GC 종류

운영서버에 절대 사용하면 안되는 GC가 Serial GC이다.

Serial GC는 데스크톱 CPU 코어가 하나만 있을때 사용하기 위한 방식이다.

Old 영역에는 `Mark and Sweep` 하고도 `Compact` 단계가 추가된다.

해당 단계에서는 큰 메모리블럭을 만들기 위해서 흩어진 사용중인 메모리를 모아주는 단계이다.

### Serial GC (-XX:+UseSerialGC)

위의 `Mark and Sweep Compact`를 수행하는 GC로서

모든 가비지 컬렉션의 일을 처리하기 위해 한개의 스레드만을 사용한다.

실행방법은 다음과 같다.

```
java -XX:+UseSerialGC -jar Application.java
```

### Parallel GC (-XX:+UseParallelGC)

Serial GC와 기본적인 알고리즘은 동일하지만, GC를 처리하는 쓰레드가 여러개여서 Serial GC보다 빠르게 처리할 수 있다.

여러 쓰레드를 활용하기에 사용할 쓰레드 개수를 지정해줄 수 있고 (`-XX:ParallelGCThreads`)

최대 가비지 컬렉션 실행시간을 정할 수 있다. (`-XX:MaxGCPauseMillis`)

Java8까지의 기본 가비지 컬렉터로 사용되었다.

### Parallel Old GC (--XX:+UseParallelOldGC)

위의 Parallel GC와 Old 영역의 GC 알고리즘만 다르다.

바로 `Mark Summary Compaction`이 사용되는데, `Summary`는 앞선 GC에서 별도로 살아있는 객체를 색별하는점에서 좀 더복잡하다.

### CMS GC (--XX:+UseConcMarkSweepGC)

Parallel GC와 마찬가지로 여러 개의 쓰레드를 사용하는 GC이다.

그러나 `Mark Sweep`알고리즘을 Concurrent 하게 수행하게 된다.

즉, 원래 애플리케이션이 실행해야할 쓰레드가 중단되지 않은체로 수행되는 GC이므로 Stop The World 시간이 매우짧다.

하지만 다른 GC보다 메모리와 CPU를 더 많이 사용해야 한다.

그리고 `Compaction`을 기본적으로 제공하지 않는다.

### G1 GC

Java7부터 지원되는 GC로서 장기적으로 문제가 될 수 있는 CMS GC를 대체하기 위해 만들어졌다.

기존의 GC 알고리즘은 Heap 영역을 물리적으로 Young영역과 Old 영역으로 나누어 사용해왔다.

G1 GC는 Eden 영역에 할당하고, Survivor로 카피하는 등의 과정을 사용하지만

물리적으로 메모리공간을 나누지 않는다. 대신 Region이라는 개념을 도입하여

Heap을 균등한 여러개의 지역으로 나누고, 각 지역을 역할과 함께 논리적으로 구분하여 객체를 할당한다.

물론 기존에는 물리적으로 구분되어있지만, 역할로 나누자면 Eden, Survivor, Old가 있엇지만

G1 GC에서는 논리적으로 Humonogous와 Available/Unused라는 2가지 역할이 추가된다.

Humonogous는 Region 크기의 50%를 초과하는 객체를 저장하는 역할이며

Available/Unused Region은 사용되지 않은 Region을 의미한다.

G1 GC의 핵심은 Heap을 동일한 크기의 Region으로 나누고

가비지가 많은 Region에 대해 우선적으로 GC를 수행하는 것이다.

이 G1 GC도 다른 GC와 마찬가지로 Minor GC와 Major GC로 나누어 수행된다.

https://velog.io/@akfls221/JVM%EC%9C%BC%EB%A1%9C-%EC%8B%9C%EC%9E%91%ED%95%B4-GC-%EA%B7%B8%EB%A6%AC%EA%B3%A0-GC-%ED%8A%9C%EB%8B%9D%EA%B9%8C%EC%A7%80

여기 블로그를 추가 학습해야 할듯
