package com.khc.practice.modernjava.appendix.lambda;

import java.util.function.Function;

public class Practice {

    public static void main(String[] args) {
        Function<Object, String> f = obj -> obj.toString();
    }
}
