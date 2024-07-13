package com.khc.practice.modernjava.ch15.asyncAPI;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Practice {
    public static void main(String[] args) throws Exception {
        syncWork();
        asyncWork();
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
