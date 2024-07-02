package org.example;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Main {
    public static final int N = 10_000_000;
    public static long [] data = new long[N+1];
    public static Long [] wrapperData = new Long[N+1];

    public static ArrayList<Long> list = new ArrayList<>();
    public static LinkedList<Long> linkedList = new LinkedList<>();

    @Setup
    public void before(){
        for(long i  = 0;i <= N;i++){
            list.add(i);
            linkedList.add(i);
        }
    }

    @Benchmark
    public void arrayListParallelSum(){
        long sum = list.parallelStream()
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public void linkedListParallelSum(){
        long sum = linkedList.parallelStream()
                .reduce(0L, Long::sum);
    }


    /*@Setup
    public void before(){
        for(int i  = 0;i <= N;i++){
            data[i] = i;
            wrapperData[i] = Long.valueOf(i);
        }
    }*/



    /*
    @Benchmark
    public long noBoxedSum(){
        return Arrays.stream(data).reduce(0L, Long::sum);
    }

    @Benchmark
    public long boxedSum(){
        return Arrays.stream(wrapperData).reduce(0L, Long::sum);
    }
    */

    /*@Benchmark // 밴치마킹 대상 메서드
    public long sequentialSum(){
        // 1부터 1씩 더하는것을 N번 수행하면서 만들어진 스트림을 0의 누적자에 계속 더한다.
        // 하지만 Stream.iterate()는 제네릭 타입이므로 Long으로 잡히고 이를 누적하면서 오토박싱이 이뤄진다.
        return Stream.iterate(1L, x -> x + 1).limit(N)
                .reduce(0L, Long::sum);
    }



    @Benchmark
    public long rangedSum(){
        // 오토박싱이라도 덜 일으켜보자
        return LongStream.rangeClosed(1, N)
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public long rangedParallelSum(){
        // 오토박싱이라도 덜 일으켜보자
        return LongStream.rangeClosed(1, N)
                .parallel()
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public long iterativeSum(){
        long result = 0;
        for(long i = 1L; i<=N;i++){
            result += i;
        }
        return result;
    }

    @Benchmark
    public long parallelStream(){
        // 그리고 이전 연산의 결과에 따라 다음 함수의 입력이 달라지기에 분할하기 어렵다.
        // 내부적으로는 iterate를 사용하면 분할하기 어려운 Spliterator를 사용하기 에 불리하다.
        return Stream.iterate(1L, x -> x + 1)
                .limit(N)
                .parallel()
                .reduce(0L, Long::sum);
    }*/

    @TearDown(Level.Invocation) // 매 밴치마크 실행한 다음에는 아래의 메서드 실행
    public void tearDown(){
        System.gc();
    }

}