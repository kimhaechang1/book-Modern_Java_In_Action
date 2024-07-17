package com.khc.practice.modernjava.ch19.curried;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class Practice {
    public static void main(String[] args) {
        DoubleUnaryOperator cToFConverter = curriedConverter(9.0/5, 32);
        DoubleUnaryOperator USDtoGBPConverter = curriedConverter(0.6, 0);
    }

    // 기존의 변환함수
    // 변환 종류에따라 새롭게 메소드를 정의해야한다.
    static double converter(double x, double f, double b){
        return x * f + b;
    }

    // 위의 변환함수를 보면 항상 값을 얻기위한 변수 x와 공식을 정의하는데 필요한 두 요소 f, b가 있다.
    // 공식을 만들어내는 팩토리 함수를 정의하는것이 하나의 방법이다.
    static DoubleUnaryOperator curriedConverter(double f, double b){
        return (double x) -> x * f + b;
    }

}
