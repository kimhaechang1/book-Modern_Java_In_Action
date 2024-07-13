### Thread.start()를 통해 실행하면 발생하는 일

Thread 클래스에는 start 메서드에 대해서 start0 메소드를 호출한다.

https://d2.naver.com/helloworld/1203723

```java
public void start() {
    synchronized (this) {
        // zero status corresponds to state "NEW".
        if (holder.threadStatus != 0)
            throw new IllegalThreadStateException();
        start0();
    }
}

private native void start0();
```

https://github.com/openjdk/jdk21/blob/master/src/java.base/share/native/libjava/Thread.c#L39

위의 JVM 코드가 간략하게 요약하자면

인자로 넘어온 자바 스레드 객체가(`jobject jthread`) 이미 동작중인지 검사하고

동작중이지 않다면, `new JavaThread`를 JVM에서 스레드를 하나 만들고

해당 스레드가 `osthread()` 메소드를 통해 커널 스레드가 할당되면, `java.lang.Thread`로 만들어진 `jobject jthread`와 C++의 `new JavaThread`를 연결한다.

즉 자바의 스레드와 커널 스레드가 1대1로 매핑된다.

### `Future<T>`

https://mangkyu.tistory.com/259

`Future<T>`는 언젠간 실행이 될 `Callable<T>` 인터페이스 구현체의 반환값을 구하기 위해 사용된다.

중간중간의 비동기 작업의 상태를 확인할 수 있고, 이를 타임제한을 걸어서 타임아웃을 걸 수도 있다.

결과를 가져오는 `get()`메소드는 블록킹 방식으로 기다리게 만든다.

```java
// Future 인터페이스의 구현체 중 FutureTask의 get 메소드 구현사항
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)
        s = awaitDone(false, 0L); // 기다리게 만든다.
    return report(s);
}
```
그래서 엄청 오래걸리는 작업에 대해서 굳이 기다리는것 없이 별도로 실행하게 만들어서 메인스레드가 블록되지 않게 만들고 싶다면 사용하면 좋다.

```java
public class Practice {
    public static void main(String[] args) throws Exception {
        syncWork(); // hardWork가 실행되려면 task1이 먼저 끝나야 한다.
        asyncWork(); // future는 별도의 쓰레드에서 동작시키고, 그 값을 받는 순간을 지정하여 blocking한다.
    }
    static void hardWork(){
        System.out.println("어떤 다른 중요한 작업");
    }

    public static void syncWork() throws Exception {
        Callable<Integer> task1 = () -> {
            Thread.sleep(2000);
            // 약 2초 걸리는 작업이라고 가정
            return 3;
        };

        Integer result = task1.call();

        hardWork();
        System.out.println("작업완료: "+result);

    }
    public static void asyncWork() throws ExecutionException, InterruptedException {
        FutureTask<Integer> future = new FutureTask<>(()-> {
            Thread.sleep(2000);
            // 약 2초 걸리는 작업이라고 가정
            return 3;
        });
        new Thread(future).start();
        hardWork();
        System.out.println("작업 완료: "+future.get());
    }
}
```
### 스레드풀이 나쁠때가 있는 이유

k개의 스레드를 가진 스레드 풀은 오직 k만큼의 스레드를 동시에 실행할 수 있다.

초과로 제출된 테스크는 큐에 저장되며 이전에 태스크 중 하나가 종료되기 전까지는 스레드에 할당되지 않는다.

불필요하게 많은 스레드를 만들어 자바 어플리케이션이 크래시 날 일은 피할 수 있기에 보통은 이 스레드풀이 긍정적이지만

`잠을 자거나` `I/O`를 기다리거나 `네트워크 연결`을 기다리는 태스크가 있다면 주의해야 한다.

블록상황에서는 워커 스레드에 할당된 상태를 유지한체로 아무 작업도 하지않게 된다.

```
예를들어서 네개의 하드웨어 스레드와 5개의 스레드를 갖는 스레드 풀에 20개의 테스크를 제출했다고 가정하자.

그런데 처음 제출한 태스크가 잠을 자거나 I/O를 기다린다고 가정하자.

그러면 남은 2개의 스레드가 15개의 테스크를 실행하게 되므로, 효율이 급격하게 떨어진다.

그래서 블록이 일어날 수 있는 태스크는 스레드풀에 제출하지 말아야하지만, 언제나 지켜지지 않는다.
```

### Java 데드락

https://rightnowdo.tistory.com/entry/JAVA-concurrent-programming-%EA%B5%90%EC%B0%A9%EC%83%81%ED%83%9CDead-Lock

데드락이란 두 개 이상의 작업이 서로 상대방의 작업이 끝나기만을 기다리고 있기 때문에 결과적으로 아무것도 완료되지 못하는 상태를 가리킴

아래의 예제가 2가지 스레드가 각각의 인스턴스가 있고, 반대편 인스턴스의 동기화 메소드를 호출하기 위해 락을 얻으려고 서로 기다리고 있는 상태를 나타낸다.

`reportDeadLockThread()` 메소드에서 데드락을 검사하여 어떤 스레드가 어떤 스레다와 데드락이 걸렸고, 그것에 대한 정보를 얻을 수 있다.

```java
package com.khc.practice.modernjava.ch15.deadlock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Practice {

    public void runDeadLock(){

        Shared s1 = new Shared();
        Shared s2 = new Shared();

        System.out.println("s1: "+ s1);
        System.out.println("s2: "+ s2);

        ExecutorService es = Executors.newFixedThreadPool(3);

        Future<?> future1 = es.submit(new Task(s1, s2));
        Future<?> future2 = es.submit(new Task(s2, s1));

        es.shutdown();

        try{
            int awaitCnt = 0;
            while(!es.awaitTermination(10, TimeUnit.SECONDS)){
                System.out.println("Not a terminate threads. ");
                reportDeadLockThread();
                awaitCnt++;
                if(awaitCnt > 3 && !future1.isCancelled() && !future2.isCancelled()){
                    future1.cancel(true);
                    future2.cancel(true);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void reportDeadLockThread(){
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] threadIds = bean.findDeadlockedThreads();
        System.out.println("thread ids: "+Arrays.toString(threadIds));
        if(threadIds != null){
            ThreadInfo[] infos = bean.getThreadInfo(threadIds);
            for(ThreadInfo info: infos){
                String s = info.getThreadName();
                System.out.println(String.format("DeadLocked Thread Name >> %s, lock info >> %s, current lock owner thread name >> %s", s, info.getLockInfo(), info.getLockOwnerName()));
            }
        }
    }

    static class Shared{
        synchronized void test1(Shared s2) throws InterruptedException{
            System.out.println("test1-begin");
            Thread.sleep(1000);

            s2.test2(this);
            System.out.println("test1-end");
        }

        synchronized void test2(Shared s1) throws InterruptedException{
            System.out.println("test2-begin");
            Thread.sleep(1000);

            s1.test1(this);
            System.out.println("test2-end");
        }
    }

    static class Task implements Runnable{
        private Shared s1;
        private Shared s2;

        Task(Shared s1, Shared s2){
            this.s1 = s1;
            this.s2 = s2;
        }

        @Override
        public void run(){
            try {
                s1.test1(s2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        new Practice().runDeadLock();
    }
}
```

```
s1: com.khc.practice.modernjava.ch15.deadlock.Practice$Shared@2f4d3709
s2: com.khc.practice.modernjava.ch15.deadlock.Practice$Shared@7291c18f
test1-begin
test1-begin
Not a terminate threads. 
thread ids: [22, 23]
DeadLocked Thread Name >> pool-1-thread-1, lock info >> com.khc.practice.modernjava.ch15.deadlock.Practice$Shared@7291c18f, current lock owner thread name >> pool-1-thread-2
DeadLocked Thread Name >> pool-1-thread-2, lock info >> com.khc.practice.modernjava.ch15.deadlock.Practice$Shared@2f4d3709, current lock owner thread name >> pool-1-thread-1
```

큐에 넣어서 작업자 스레드가 각각 가져가게 된다.

3명의 코어 작업자 스레드가 있으므로, 즉각적으로 실행된다.

Shared 클래스는 동기화된 메소드를 보유하고 있기에, 인스턴스 락을 활용하여 멀티 스레드 환경에서 동기화를 보장하게 된다.

s1의 test1(s2) 메소드를 호출하려 하기에 s1의 락을 얻은 상태고 test1메소드는 동기화 된 메소드로서, 1초간 블록상태 후 내부의 s2.test2(s1) 를 호출하려 한다.

thread 1의 블록상태 동안 또다른 스레드는 s2 락을 얻은 상태에서 s2.test1(s1) 메소드를 호출하고, 내부에서 s1는 test2(s2) 를 호출하려 한다.

이 과정에서 Thread-1은 s1.test1()에 대한 콜에 의해 s1락을 얻은 상태에서 s2.test2() 메소드에 대한 s2락을 기다려야 한다.

서로가 서로의 락이 해제되기를 기다리므로 데드락 상태가 된다.

## `CompletableFuture`와 콤비네이터를 이용한 동시성

여러 오래걸리는 함수들에 대해서 그 결과들을 조합해서 마지막 함수의 인자로 전달해야 하는 작업이 있을 수 있다.

```
        task1(): r1
go() -> task2(): r2 => finalTask(r1, r2, r3): result
        task3(): r3
```
여기서 `task1`, `task2`, `task3` 효율적으로 메인스레드를 돌리기 위해, 각각을 별도의 `Future` 객체로 감싸서 쓰레드로 실행시킨다고 하더라도

finalTask를 실행시키기 전 준비상태를 위해서 3개의 `Future`에 대한 `get`을 사용하여야만 했다.

`task1(), task2(), task3()` 의 작업과 `finalTask`를 하나로 묶어서 거대한 `task`로 볼 수 있으면 하나의 `get`만 동작할 것이다.

이를 가능케 하는 `Future`인터페이스의 구현체가 `CompletableFuture`이다.

위를 구현하면 다음과 같다.

```java
public Future<Integer> doAsync(int x) throws ExecutionException, InterruptedException {

    CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
        System.out.println("task1 thread: "+Thread.currentThread().getName());
        return f(x);
    });
    CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> {
        System.out.println("task2 thread: "+Thread.currentThread().getName());
        return g(x);
    });
    CompletableFuture<Integer> task3 = CompletableFuture.supplyAsync(() -> {
        System.out.println("task3 thread: "+Thread.currentThread().getName());
        return h(x);
    });

    CompletableFuture<Integer> result =
            task1.thenCombine(task2, (t1, t2) -> t1 + t2)
            .thenCombine(task3, (sum, t3) -> sum + t3)
            .thenCompose(sum -> CompletableFuture.completedFuture(sum));
    /**
     * 위의 코드는 thenCombine으로서 서로 독립적으로 작동하고
     * 아래의 주석친 코드는 thenCompose로서 앞선 결과를 기다리고 다음 비동기 함수를 호출한다.
     * 꼭 비교해서 쓰레드가 어떻게 작동하는지 실행해보자
     */

    /*CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> {
                System.out.println("task1 thread: "+Thread.currentThread().getName());
                return f(x);
            })
            .thenCompose(t1 -> CompletableFuture.supplyAsync(() -> {
                System.out.println("task2 thread: "+Thread.currentThread().getName());
                return t1 + g(x);
            }))
            .thenCompose(t2 -> CompletableFuture.supplyAsync(() -> {
                System.out.println("task3 thread: "+Thread.currentThread().getName());
                return t2 + h(x);
            }));
        */


    return result;
}

```

## 발행-구독 그리고 리액티브 프로그래밍

자바 9에서 `java.util.concurrent.Flow`의 인터페이스 발행-구독 모델을 적용해 리액티브 프로그래밍을 제공한다.

- 구독자가 구독할 수 있는 발행자.
- 이 연결을 `subscription`이라고 한다.
- 이 연결을 이용해 메시지(또는 이벤트)를 전송한다.

크게 `Publisher<T>`와 `Subscriber<T>`의 역할로 나뉜다.

다음은 셀의 예제로서, 하나의 셀은 누군가를 구독자로 받아들이고 발행할 수 있고(`Publisher`), 구독자(`Subscriber`)로서 어떤 발행자를 구독할 수 있다.

그래서 `SimpleCell`은 두가지 인터페이스를 모두 구현하게 된다

```java
public class SimpleCell implements Flow.Publisher<Integer>, Flow.Subscriber<Integer> {

    private int value = 0;
    private String name;
    private List<Flow.Subscriber> subscribers = new ArrayList<>(); // 구독자 관리체계
    public SimpleCell(String name){
        this.name = name;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
        subscribers.add(subscriber);
        // 구독자가 해당 셀에 구독을 하게되면 구독자를 받는 발행자는 구독자를 추가한다.
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    private void notifyAllSubscribers(){
        // 구독자들에게 발행자가 갖고있는 값으로 갱신시킴
        subscribers.forEach(sub -> sub.onNext(this.value));
    }

    @Override
    public void onNext(Integer item) {
        // 구독한 셀에 새 값이 생겼을 때 값을 갱신해서 반응함.
        this.value = item;
        System.out.println(this.name + ":"+this.value);
        notifyAllSubscribers();
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
```
```java
public static void main(String[] args) {
    simplePubSub();
}
public static void simplePubSub(){
    SimpleCell c3 = new SimpleCell("C3");
    SimpleCell c2 = new SimpleCell("C2");
    SimpleCell c1 = new SimpleCell("C1");

    // c3가 c1을 구독
    c1.subscribe(c3);
    c1.onNext(10); // c1 발행자를 c3가 구독하고 있기에 notify되어 c3도 onNext이벤트가 전파되었다.
    c2.onNext(20);
}
```

그리고 `Consumer`를 활용해서, `Subscriber`인터페이스의 `onNext`이벤트 동작을 넘겨받고

해당 `onNext`를 수행하는 새로운 구독자를 만들어서 구독시키도록 `subscribe`메소드를 오버로딩 할 수 있다.

즉, 또다른 구독자 객체가 아닌 특정 이벤트가 트리거인 구독자를 만들어내는 셈이다.

```java
public class ArithmeticCell extends SimpleCell{
    // 하나의 값이라도 받으면 반대쪽과 덧셈을 수행하는 이벤트를 발생시킨다.
    private int left;
    private int right;
    public ArithmeticCell(String name) {
        super(name);
    }

    public void setLeft(int left){
        this.left = left;
        onNext(left + this.right);
    }

    public void setRight(int right){
        this.right = right;
        onNext(right + this.left);
    }
}
```
```java
public class SimpleCell implements Flow.Publisher<Integer>, Flow.Subscriber<Integer> {

    private int value = 0;
    private String name;
    private List<Flow.Subscriber<? super Integer>> subscribers = new ArrayList<>();
    public SimpleCell(String name){
        this.name = name;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
        subscribers.add(subscriber);
    }

    public void subscribe(Consumer<? super Integer> onNext) {
        // 특정 이벤트의 함수 정의를 인자로 받아서, 해당 이벤트를 처리하는 구독자 객체를 만들고 발행자를 구독
        // 즉, onNext이벤트에 반응하는 구독자를 만들어서 구독하게 만드는 함수
        subscribers.add(new Flow.Subscriber<>() {

            @Override
            public void onSubscribe(Flow.Subscription subscription) {

            }

            @Override
            public void onNext(Integer item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
```
```java
public static void arithmaticPubSub(){
    ArithmeticCell p1 = new ArithmeticCell("ALU1");
    ArithmeticCell p2 = new ArithmeticCell("ALU2");

    SimpleCell c3 = new SimpleCell("C3");
    SimpleCell c2 = new SimpleCell("C2");
    SimpleCell c1 = new SimpleCell("C1");
    c1.subscribe(x -> p1.setLeft(x));
    // c1의 onNext이벤트 전파때 ArithmeticCell의 setLeft를 호출하도록 동작하는 구독자를 구독시킴
    c2.subscribe(x -> p1.setRight(x));
    // c2의 onNext이벤트 전파때 ArithmeticCell의 setLeft를 호출하도록 동작하는 구독자를 구독시킴
    p1.subscribe(x -> p2.setLeft(x));
    // p1의 onNext이벤트 전파때 ArithmeticCell p2의 setLeft를 호출하도록 동작하는 구독자를 구독시킴
    // p1의 subscribe는 SimpleCell 클래스의 subscribe를 호출하게 되고, 최상위 c1이나 c2에 변화가 결국 p1을 거쳐 p2에게 닿게됨
    c3.subscribe(x -> p2.setRight(x));
    // c3의 onNext이벤트 전파때 ArithmeticCell p2의 setRight를 호출하도록 동작하는 구독자를 구독시킴

    c1.onNext(10);
    c2.onNext(20);
    c1.onNext(15);
    c3.onNext(1);
    c3.onNext(3);
}
```

### 역압력

기존의 방식들은 모두 `Publisher`가 `Subscriber`에게 전달하는 `압력`이었다면

`압력`의 반대로 발행자가 무한히 쏟아내는 상황에서, 구독자가 원할때만 데이터를 받을 때에는 `역압력`을 통해 `request`를 하게 된다.

구독이 발생할 때 작동하는 이벤트인 `onSubscribe(Subscription subscription)` 이 있다.

```java
interface Subscription{
    void cancle();
    void request(long n);
}
```
위 인터페이스속 메소드를 구현한 인스턴스를 `Subscriber`인터페이스의 `onSubscribe()`메소드를 통해 전달하고

요청을 보낸 채널에만 `onNext()`와 `onError()`를 보내도록 `notifyAllSubscribers` 코드를 바꿔야 한다.

### 리액티브 시스템 vs 리액티브 프로그래밍

`리액티브 시스템`은 런타임 환경이 변화에 대응하도록 전체 아키텍쳐가 설계된 프로그램을 가리킨다.

리액티브 시스템은 `반응형`, `회복성`, `탄력성`의 특징을 가진다.

- 반응성: 리액티브 시스템이 큰 작업을 처리하느라 간단한 질의의 응답을 지연하지 않고 실시간으로 입력에 반응하는 것을 의미

- 회복성: 한 컴포넌트의 실패로 전체 시스템이 실패해야 하지 않음을 의미

- 탄력성: 네트워크가 고장났어도 이와 관계가 없는 질의에는 아무 영향이 없어야 하며, 반응이 없는 컴포넌트에 대한 질의가 있다면 다른 대안 컴포넌트를 찾아야 함을 의미

이들을 구현하는데 있어서 자바에서는 `java.lang.concurrent.Flow`의 `리액티브 프로그래밍` 형식을 활용하는것이 하나의 방법이다.
