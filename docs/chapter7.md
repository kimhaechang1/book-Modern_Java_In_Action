## 병렬 스트림 사용시 주의할 점

### 확신이 서지 않는다면 직접 측정하라

순차 스트림에서 병렬 스트림으로는 얼마든지 쉽게 바꿀 수 있다.

하지만 언제나 순차스트림보다 병렬스트림이 빠른것은 아니기 때문에, 직접 벤치마크로 측정해보는 것이 좋다.

### 박싱을 주의하자

오토 박싱이나 언박싱은 성능을 크게 저하시킬 수 있다.

자바 8에서는 오토박싱을 최대한 피할 수 있도록 기본형에 특화된 Stream 을 제공한다.

따라서 되도록이면 기본형 특화 스트림을 사용하는것이 좋다.

```java
public class Main {
    public static final int N = 10_000_000;
    public static long [] data = new long[N+1];
    public static Long [] wrapperData = new Long[N+1];


    @Setup
    public void before(){
        for(int i  = 0;i <= N;i++){
            data[i] = i;
            wrapperData[i] = Long.valueOf(i);
        }
    }

    @Benchmark
    public long noBoxedSum(){
        return Arrays.stream(data).reduce(0L, Long::sum);
    }

    @Benchmark
    public long boxedSum(){
        return Arrays.stream(wrapperData).reduce(0L, Long::sum);
    }
}
```

```java
Benchmark        Mode  Cnt   Score    Error  Units
Main.boxedSum    avgt   20  86.842 ± 23.057  ms/op
Main.noBoxedSum  avgt   20  15.494 ±  2.610  ms/op
```

위의 결과에서 볼 수 있듯이, 박싱이 일어나는 연산에서는 평균적으로 `60~100ms`가 발생하지만

박싱이 일어나지 않는 연산에서는 `13~17ms` 로 줄어든다.

### 병렬 스트림에서 공유자원 접근은 삼가하자

병렬 스트림 사용에서 가장 많이 실수하는 것은 공유된 상태를 바꾸는 알고리즘을 사용하는 것

다음은 N까지의 자연수를 더하면서 공유된 누적자를 바꾸는 프로그램이다.

```java
public class Accumulator {
    public long total = 0;
    public void add(long value){
        total += value;
    }
}

public class Practice {

    public long sideEffectSum(long n){
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).forEach(accumulator::add); // 메소드 참조
        return accumulator.total;
    }

    public long sideEffectParallelSum(long n){
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add); // 메소드 참조
        return accumulator.total;
    }


    public static void main(String[] args) {
        System.out.println(new Practice().sideEffectSum(10_000_000));
        System.out.println(new Practice().sideEffectParallelSum(10_000_000));
    }
}
```

위의 코드를 실행하면 윗줄은 정상적으로 `50000005000000` 이 나오지만

아랫줄은 그 값이 발생하지 않는다.

이는 병렬 처리시 연산에 사용하는 공유된 자원인 `accumulator.total`에 update를 일으키기 때문이다.

즉, 여러 스레드가 `accumulator.add()`를 동시에 호출하여 발생하는 문제이다.

위와 같이 `공유된 가변상태`를 피해야 한다.

### 병렬 스트림 사용시 주의 할 점

양을 기준으로 병렬 스트림 사용여부를 결정하는것은 적절치ㅓ 않다.

그래서 아래와 같은 사항들을 고려해보자.

iterate()는 내부적으로 분할 하기 어려운 Spliterator를 생성한다.

```
Benchmark               Mode  Cnt   Score   Error  Units
Main.iterativeSum       avgt   20   3.298 ± 0.045  ms/op
Main.parallelStream     avgt   20  71.924 ± 3.033  ms/op
Main.rangedParallelSum  avgt   20   0.481 ± 0.028  ms/op
Main.rangedSum          avgt   20   4.305 ± 1.280  ms/op
Main.sequentialSum      avgt   20  68.674 ± 1.332  ms/op
```

### 순차 스트림보다 병렬 스트림에서 성능이 떨어지는 연산을 주의하자

특히 `limit`나 `findFirst`와 같은 요소의 순서에 의존하는 연산은 병렬 스트림에서 수행하려면 비싼 비용을 치뤄야 한다.

대신, 정렬이 굳이 필요없는 상황에서는 `unordered`를 호출하여 비정렬된 스트림을 얻고

이 스트림에서 `limit`를 호출하는 것이 더욱 효율적이다.

또한 `findFirst`가 아니라 어떠한 순서도 상관없이 선택하는 `findAny`가 더욱 효율적이다.

### 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라

처리해할 요소수가 단순히 많은것 뿐만아니라

더욱 중요한것은 하나의 요소에 대한 연산이 얼마나 무거운가가 병렬 스트림으로 얻을 수 있는 이득을 상승 시킨다.

### 스트림을 구성하는 자료구조가 적절한지 확인하라

예를들어 `ArrayList`와 같이 인덱스를 가지고 순차적으로 존재하는 데이터는 분할하기 편하지만

`LinkedList`의 경우에는 분할을 위해서 전체 데이터를 탐색해야 하는 비효율이 있다.

또한 `range` 혹은 `rangeClosed`로 생성한 기본형 스트림도 쉽게 분할이 가능하다.

### 스트림의 특징과 파이파인의 중간 연산이 스트림 특성을 어떻게 바꾸는지에 따라 분할 과정 성능이 달라진다.

예를들어 `SIZED` 스트림의 경우 정확히 같은 크기의 두 스트림으로 분할이 가능하다.

하지만 중간 연산에 필터연산이 끼여있다면, 길이를 예측하기 힘들기에 효과적으로 처리가 가능한지 예측하기 힘들어진다.

### 최종 연산의 병합과정이 비싼지 살펴보라

만약 최종 연산의 병합과정 (`Collector`의 `combine`메소드) 의 비용이 비싸다면 병렬스트림을 통해 멀티 코어를 활용한 이득이

병합과정에서 걸리는 오버헤드로 인해 상쇄 될 수 도 있다.

## 포크 조인 프레임워크: RecursiveTask 활용

스레드 풀을 사용하려면 `RecursiveTask<R>`의 서브클래스를 만들어야 한다.

여기서 `R`은 병렬화된 테스크가 생성하는 결과 형식 또는 결과가 없을때 `RecursiveAction`의 형식이다.

그 내부에는 구현해야할 메소드로 `compute`가 있는데

다음과 같은 수도 코드를 가진다.

```java
if (테스크가 충분히 작거나 더 이상 분할할 수 없으면){
    순치적으로 태스크 계산하기
} else {
    태스크를 두 서브테스크로 분할
    태스크가 다시 서브테스크로 분할되도록 이 메서드를 재귀적으로 호출
    모든 서브태스크의 연산이 완료될때 까지 기다림(join)
    각 서브 태스크의 결과를 합침 (정복)
}
```

`long [] `로 이루어진 숫자배열을 사용하여 n까지의 자연수 덧셈 작업을 포크-조인 프레임워크로 구현한다.

```java
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

    private final long [] numbers;
    private final int start;
    private final int end;
    public static final long THRESHOLD = 10_000;

    public ForkJoinSumCalculator(long [] numbers){
        this(numbers, 0, numbers.length);
    }

    private ForkJoinSumCalculator(long [] numbers, int start, int end){
        this.numbers =numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        System.out.println(Thread.currentThread().getName()+" "+"start: "+this.start+" end: "+this.end);
        int length = end - start;
        if(length <= THRESHOLD){
            // 현재 최소한의 작업단위만큼 충분히 작아졌다면
            // 순차적으로 수행하고 결과를 반환한다.
            return computeSequentially();
        }
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length/2);
        leftTask.fork();
        // 왼쪽 분할영역을 fork를 사용하여 별도의 스레드에서 동작시킨다.
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length/2, end);
        Long rightResult = rightTask.compute();
        // 오른쪽 분할 영역에 대해서 더욱 분할이 가능하도록 compute를 호출한다.
        Long leftResult = leftTask.join();
        // 왼쪽 별도의 스레드에서 하던 작업이 끝날때 까지 기다린다.
        return rightResult + leftResult;
    }

    private long computeSequentially() {
        long sum = 0;
        for(int i= start;i< end;i++){
            sum += numbers[i];
        }
        return sum;
    }
}
```

```
ForkJoinPool-1-worker-1 start: 0 end: 100000
ForkJoinPool-1-worker-1 start: 50000 end: 100000
ForkJoinPool-1-worker-1 start: 75000 end: 100000
ForkJoinPool-1-worker-1 start: 87500 end: 100000
ForkJoinPool-1-worker-1 start: 93750 end: 100000
ForkJoinPool-1-worker-1 start: 87500 end: 93750
ForkJoinPool-1-worker-1 start: 75000 end: 87500
ForkJoinPool-1-worker-2 start: 0 end: 50000
ForkJoinPool-1-worker-1 start: 81250 end: 87500
ForkJoinPool-1-worker-2 start: 25000 end: 50000
ForkJoinPool-1-worker-1 start: 75000 end: 81250
ForkJoinPool-1-worker-3 start: 50000 end: 75000
ForkJoinPool-1-worker-2 start: 37500 end: 50000
ForkJoinPool-1-worker-1 start: 50000 end: 62500
ForkJoinPool-1-worker-2 start: 43750 end: 50000
ForkJoinPool-1-worker-4 start: 0 end: 25000
ForkJoinPool-1-worker-2 start: 37500 end: 43750
ForkJoinPool-1-worker-3 start: 62500 end: 75000
ForkJoinPool-1-worker-2 start: 25000 end: 37500
ForkJoinPool-1-worker-4 start: 12500 end: 25000
ForkJoinPool-1-worker-1 start: 56250 end: 62500
ForkJoinPool-1-worker-5 start: 0 end: 12500
ForkJoinPool-1-worker-6 start: 62500 end: 68750
ForkJoinPool-1-worker-5 start: 6250 end: 12500
ForkJoinPool-1-worker-2 start: 31250 end: 37500
ForkJoinPool-1-worker-8 start: 12500 end: 18750
ForkJoinPool-1-worker-3 start: 68750 end: 75000
ForkJoinPool-1-worker-7 start: 50000 end: 56250
ForkJoinPool-1-worker-1 start: 0 end: 6250
ForkJoinPool-1-worker-4 start: 18750 end: 25000
ForkJoinPool-1-worker-10 start: 25000 end: 31250
5000050000
```
