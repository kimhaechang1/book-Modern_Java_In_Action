package com.khc.practice.modernjava.ch04;


import java.util.*;
import java.util.stream.Stream;

public class Practice {
    static class Data implements Comparable<Data>{
        int value;
        String name;

        public Data(int value, String name){
            this.value = value;
            this.name = name;
        }

        @Override
        public int compareTo(Data o) {
            return value - o.value;
        }
    }
    public static void main(String[] args) {
        // no stream
        // 원본 dataList를 훼손시키지 말 것
        List<Data> dataList = Arrays.asList(
                new Data(10, "number1"),
                new Data(7, "number2"),
                new Data(100, "number3"),
                new Data(302, "number4"),
                new Data(22, "number5"),
                new Data(57, "number6"),
                new Data(16, "number7"),
                new Data(99, "number8")
        );

//        List<Data> lowValueList = new ArrayList<>();
//        // 중간 처리를 위한 list가 하나 필요함 (메모리 사용)
//
//        for(Data data: dataList){
//            if(data.value <= 100){
//                lowValueList.add(data);
//            }
//        }
//
//        // 정렬
//        Collections.sort(lowValueList);
//
//        // 최종 결과: nameList
//        List<String> result = new ArrayList<>();

//        for(Data data: lowValueList){
//            result.add(data.name);
//        }
//        System.out.println(result);

        Stream<Data> origin = dataList.stream();
        Stream<Data> sorted = origin.sorted();
        Stream<Data> sliced = sorted.takeWhile((data) -> data.value <= 100);
        Stream<String> mapped = sliced.map(d -> d.name);
        List<String> r = mapped.toList();
        System.out.println(r);

        // 붙여서 작업한다면 사실상

        List<String> result = dataList.stream()
                .sorted()
                .takeWhile(d -> d.value <= 100)
                .map(d -> d.name)
                .toList();

        System.out.println(result);




    }
}
