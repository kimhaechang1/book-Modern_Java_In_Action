package com.khc.practice.modernjava.ch13.implicity;

public class C implements A, B{
    public static void main(String[] args) {
        System.out.println(new C().method());
    }

    @Override
    public int method() {
        return A.super.method();
    }
}
