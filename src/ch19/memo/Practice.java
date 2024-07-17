package com.khc.practice.modernjava.ch19.memo;

import java.util.HashMap;

public class Practice {
    static HashMap<String, Integer> cache;
    public static void main(String[] args) {
        cache = new HashMap<>();
        System.out.println("result: "+toString("10"));
        System.out.println("result: "+toString("10"));
    }

    static Integer toString(String value){
        if(cache.containsKey(value)){
            System.out.println("get from cache...");
            return cache.get(value);
        }
        System.out.println("get after compute...");
        return cache.computeIfAbsent(value, Integer::parseInt);
    }
}
