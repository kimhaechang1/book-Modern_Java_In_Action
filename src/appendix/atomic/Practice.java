package com.khc.practice.modernjava.appendix.atomic;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Practice {
    private static final int NUM_TASKS = 1_000_000;
    private static final int NUM_THREADS = 100;

    public static void main(String[] args) throws InterruptedException {
        // AtomicLong 성능 테스트
        AtomicLong atomicLong = new AtomicLong(0);
        ExecutorService atomicExecutor = Executors.newFixedThreadPool(NUM_THREADS);

        long startAtomicTime = System.nanoTime();
        for (int i = 0; i < NUM_TASKS; i++) {
            atomicExecutor.execute(() -> atomicLong.incrementAndGet());
        }
        atomicExecutor.shutdown();
        atomicExecutor.awaitTermination(1, TimeUnit.MINUTES);
        long endAtomicTime = System.nanoTime();
        long atomicDuration = endAtomicTime - startAtomicTime;
        System.out.println("AtomicLong final count: " + atomicLong.get());
        System.out.println("AtomicLong duration: " + atomicDuration / 1_000_000 + " ms");

        // LongAdder 성능 테스트
        LongAdder longAdder = new LongAdder();
        ExecutorService adderExecutor = Executors.newFixedThreadPool(NUM_THREADS);

        long startAdderTime = System.nanoTime();
        for (int i = 0; i < NUM_TASKS; i++) {
            adderExecutor.execute(longAdder::increment);
        }
        adderExecutor.shutdown();
        adderExecutor.awaitTermination(1, TimeUnit.MINUTES);
        long endAdderTime = System.nanoTime();
        long adderDuration = endAdderTime - startAdderTime;
        System.out.println("LongAdder final count: " + longAdder.sum());
        System.out.println("LongAdder duration: " + adderDuration / 1_000_000 + " ms");
    }
}
