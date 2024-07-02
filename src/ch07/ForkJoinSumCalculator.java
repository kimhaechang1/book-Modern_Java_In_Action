package modernjavainaction.ch07;

import java.util.concurrent.RecursiveTask;

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
            return computeSequentially();
        }
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length/2);
        leftTask.fork();
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length/2, end);
        Long rightResult = rightTask.compute();
        Long leftResult = leftTask.join();
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
