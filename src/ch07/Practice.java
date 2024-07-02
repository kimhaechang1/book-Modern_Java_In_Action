package modernjavainaction.ch07;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public static long forkJoinSum(long n){
        long [] numbers = LongStream.rangeClosed(1, n).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        return new ForkJoinPool().invoke(task);
    }
    static final String SENTENCE =
            "Nel mezzo del cammin di nostra vita " +
            "mi ritrovai in una selva oscura " +
            "ch la dritta via era smarrita ";

    public static void main(String[] args) {
        /*System.out.println(new Practice().sideEffectSum(10_000_000));
        System.out.println(new Practice().sideEffectParallelSum(10_000_000));*/
//        System.out.println(forkJoinSum(100000));

        System.out.println(countWordsIteratively(SENTENCE));
        System.out.println(getCountByStream(SENTENCE));
        System.out.println(getCountByParallelStream(SENTENCE));
        System.out.println(getCountBySpliterator(SENTENCE));
    }

    public static int countWordsIteratively(String s){
        int counter = 0;
        boolean lastSpace = true;
        for(char c: s.toCharArray()){
            if(Character.isWhitespace(c)){
                lastSpace = true;
            } else {
                if (lastSpace) counter++;
                lastSpace = false;
            }
        }
        return counter;
    }

    public static int getCountByStream(String s){
        Stream<Character> stream = IntStream.range(0, s.length())
                .<Character>mapToObj(s::charAt); // char는 기본형 Stream을 제공하지 않는다.
        // 따라서 길이만큼의 인덱싱용 IntStream을 열고, mapToObj를 통해 Character Stream으로 매핑한다.

        // 기본 Reducer를 통해서는, 하나의 단어를 계산하는데 필요한 서로다른 형태의 연산 도구가 필요하다
        //    private final int counter; // 단어 수 새기
        //    private final boolean lastSpace; // 화이트 스페이스 여부
        // 그래서 하나의 객체로 묶고, 해당 객체에서 누적자와 콤바인을 구현해서 사용한다.
        WordCounter wordCounter = stream.reduce(
                new WordCounter(0, true),
                WordCounter::accumulator,
                WordCounter::combine
        );
        return wordCounter.getCounter();
    }

    public static int getCountByParallelStream(String s){
        Stream<Character> stream = IntStream.range(0, s.length())
                .<Character>mapToObj(s::charAt);

        // 병렬 스트림을 사용하면, 지네들이 임의의 위치에서 문자열을 나누다 보니, 단어를 셈하는데 있어서 중복이 일어난다.
        // 이를 해결하기 위해 분할의 기준을 Spliterator를 통해 사용자 정의 해주어야 한다.
        WordCounter wordCounter = stream.parallel().reduce(
                new WordCounter(0, true),
                WordCounter::accumulator,
                WordCounter::combine
        );
        return wordCounter.getCounter();
    }

    public static int getCountBySpliterator(String s){
        Spliterator<Character> spliterator = new WordCounterSpliterator(s);
        Stream<Character> stream = StreamSupport.stream(spliterator, true);
        WordCounter wordCounter = stream.parallel().reduce(
                new WordCounter(0, true),
                WordCounter::accumulator,
                WordCounter::combine
        );
        return wordCounter.getCounter();
    }

}
