package com.khc.practice.modernjava.appendix.Arrays;

import java.util.Arrays;
import java.util.Random;

public class Practice {
    static final int MAX_LENGTH = 10_000_000;
    static int [] array = new int[MAX_LENGTH];
    static int [] array2 = new int[MAX_LENGTH];
    static final int MAX_VALUE = 5_000;
    static final Random random =  new Random();
    public static void main(String[] args) {
        for(int i = 0;i<MAX_LENGTH;i++){
            array[i] = random.nextInt(MAX_VALUE);
        }
        array2 = Arrays.copyOf(array, array.length);

        long startTime = System.nanoTime();
        Arrays.parallelSort(array);
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1_000_000 +" ms");

        startTime = System.nanoTime();
        Arrays.sort(array2);
        endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1_000_000 +" ms");
    }
}
