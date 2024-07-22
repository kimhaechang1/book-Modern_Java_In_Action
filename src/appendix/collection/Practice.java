package com.khc.practice.modernjava.appendix.collection;

import java.util.HashMap;
import java.util.Map;

public class Practice {
    static Map<String, Integer> map = new HashMap<>();
    public static void main(String[] args) {
        Integer result1 = map.computeIfAbsent("10", key -> {
            System.out.println(key+" is absent");
            return Integer.parseInt(key);
        });
        System.out.println("result1 : "+result1);
        Integer result2 = map.computeIfAbsent("10", key -> {
            System.out.println(key+" is absent");
            return Integer.parseInt(key);
        });
        System.out.println("result2 : "+result2);

    }
}
