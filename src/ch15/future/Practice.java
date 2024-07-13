package com.khc.practice.modernjava.ch15.future;

import com.khc.practice.thisisjava.class1.C;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class Practice {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Practice p = new Practice();
//        p.futureTest();
//        p.completableFutureTest();
        p.completableFutureBasic();
    }
    static int f(int x) {
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){

        }
        return x + 10;
    }
    static int g(int x){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){

        }
        return x + 20;
    }

    static int h(int x){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){

        }
        return x + 30;
    }

    public void futureTest() throws ExecutionException, InterruptedException {
        int x = 1337;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Future<Integer> future1 = executorService.submit(() -> f(x));
        Future<Integer> future2 = executorService.submit(() -> g(x));

        hardWork();

        System.out.println(future1.get() + future2.get());
    }

    public void completableFutureTest() throws ExecutionException, InterruptedException {
        int x = 1337;

        CompletableFuture<Integer> task1 = new CompletableFuture<>();
        CompletableFuture<Integer> task2 = new CompletableFuture<>();
        task1 = CompletableFuture.supplyAsync(() -> {
            return f(x);
        });
        task2 = CompletableFuture.supplyAsync(() -> {
            return g(x);
        });

        hardWork();

        CompletableFuture<Integer> result = task1.thenCombine(task2, Integer::sum);
        System.out.println(result.get());
    }

    public void hardWork(){
        System.out.println("또다른 무거운 작업");
    }

    public void completableFutureBasic() throws ExecutionException, InterruptedException {
        int x = 10;
        Future<Integer> future = doAsync(10);
        hardWork();
        System.out.println(future.get());
    }

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

    private void delay(){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){

        }
    }
}
