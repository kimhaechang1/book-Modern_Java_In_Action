package com.khc.practice.modernjava.ch05;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;

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

    public static void main(String[] args) throws IOException {
        Stream<String> values =
                Stream.of("config", "home", "user")
                        .flatMap(key -> Stream.ofNullable(System.getProperty(key)));

        values.forEach(System.out::println);

        // Stream은 AutoCloseable이 구현되어 있기에 try-with resources 사용가능

        List<String> result = new ArrayList<>();
        try(Stream<String> lines = Files.lines(Paths.get("src/com/khc/practice/modernjava/ch05/file.txt"), Charset.defaultCharset()))
        {
            result = lines.toList();
        }catch(IOException e){

        }
        System.out.println(result);

        Stream<int[]> fibo = fiboStream(20);
        List<Integer> fiboValues = fibo.map(x -> x[0]).toList();
        System.out.println(fiboValues);


        // JAVA 9에서 업데이트된 iterate에 Predicate 사용하기
        IntStream evenStream = getEven(100);
        int[] evenArr = evenStream.toArray();
        System.out.println(Arrays.toString(evenArr));

        // filter로 위의 기능을 구현하려 하면 문제가 된다.
//        IntStream.iterate(0, number -> number + 2)
//                .filter(number -> number < 100) // 여기서 중단시킬 수 없음
//                .forEach(System.out::println);

        // 루프 퓨전이 일어나는 경우 - 1
        Stream.generate(() -> new Random().nextInt())
                .peek(System.out::println)
                .limit(5)
                .toList();

        // 루프 퓨전이 일어나지 않는 경우 - 1
//        Stream.generate(() -> new Random().nextInt())
//                .peek(System.out::println)
//                .sorted() // 여기서 모든 스트림내 데이터가 모여서 정렬될때까지 무한수행하게 된다.
//                .limit(5)
//                .toList();

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

        // 루프 퓨전이 일어나는 경우 - 2

        List<String> list = dataList.stream()
                .peek(System.out::println)
                .map(s -> s.name)

                .limit(4)
                .toList();

        System.out.println(list);

        // 루프 퓨전이 일어나지 않는 경우 - 2

        list = dataList.stream()
                .peek(System.out::println)
                .sorted(Comparator.comparingInt(a -> a.value))
                .map(d -> d.name)
                .limit(4)
                .toList();
    }

    public static Stream<int []> fiboStream(int max){
        return Stream.<int []>iterate(
                new int[]{0, 1}, (int [] value) -> new int[]{ value[1], value[0] + value[1]})
                .limit(max);
    }

    public static IntStream getEven(int max){
        return IntStream.iterate(0, number -> number < max, number -> number + 2);
    }
}
