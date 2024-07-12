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

```
\
```
