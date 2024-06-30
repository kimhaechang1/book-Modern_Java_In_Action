package modernjavainaction.ch06;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static modernjavainaction.ch06.Practice.enhancedIsPrime;

public class PrimeNumbersCollector implements Collector<Integer, Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> {

    @Override
    public Supplier<Map<Boolean, List<Integer>>> supplier() {
        // 누적자 초기화 부분
        return () ->
            new HashMap<Boolean, List<Integer>>(){{
                put(true, new ArrayList<Integer>());
                put(false, new ArrayList<Integer>());
            }};
    }

    @Override
    public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
        // 누적 연산정의
        return (Map<Boolean, List<Integer>> acc, Integer present) -> {
            acc.get(enhancedIsPrime(acc.get(true), present)).add(present);
            // 검사결과에 따른 분류를 진행
        };
    }

    @Override
    public BinaryOperator<Map<Boolean, List<Integer>>> combiner() {
        return (Map<Boolean, List<Integer>> map1, Map<Boolean, List<Integer>> map2) -> {
            map1.get(true).addAll(map2.get(true));
            map1.get(false).addAll(map2.get(false));
            return map1;
        };
    }

    @Override
    public Function<Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> finisher() {
        // 항등함수이므로
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        // 순서가 존재하며, 굳이 병렬연산을 지원하지 않을 예정이므로
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
    }
}
