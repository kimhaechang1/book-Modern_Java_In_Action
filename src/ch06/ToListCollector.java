package com.khc.practice.modernjava.ch06;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {
    @Override
    public Supplier<List<T>> supplier() {
        // 누적자 초기화
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        // 누적하는 람다식
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        // 병렬 연산시 두개의 나눠진 스트림을 하나로 합침
        return (list, list2) -> {
            list.addAll(list2);
            return list;
        };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        // 최종 누적자가 최종결과이므로 항등함수를 반환
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(
                Characteristics.CONCURRENT, Characteristics.IDENTITY_FINISH
        ));
    }
}
