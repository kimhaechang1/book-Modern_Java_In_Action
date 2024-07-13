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
        // 큐에 넣어서 작업자 스레드가 각각 가져가게 된다.
        // 3명의 코어 작업자 스레드가 있으므로, 즉각적으로 실행된다.
        // Shared 클래스는 동기화된 메소드를 보유하고 있기에, 멀티 스레드 환경에서 동기화를 보장하게 된다.
        // s1의 test1(s2) 메소드를 호출하려 하기에 s1의 락을 얻은 상태고 test1메소드는 동기화 된 메소드로서, 1초간 블록상태 후 내부의 s2.test2(s1) 를 호출하려 한다.
        // 또다른 스레드는 s2 락을 얻은 상태에서 s2.test1(s1) 메소드를 호출하고, 이때 s2.test1()이기에 문제가 발생하지 않는다, 내부에서 s1는 test2(s2) 를 호출하려 한다.
        // 이 과정에서 Thread-1은 s1.test1()에 대한 콜에 의해 s1락을 얻은 상태에서 s2.test2() 메소드에 대한 s2락을 기다려야 한다.
        // 서로가 서로의 락이 해제되기를 기다리므로 데드락 상태가 된다.

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
