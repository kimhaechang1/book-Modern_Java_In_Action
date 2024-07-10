package com.khc.practice.modernjava.ch13.diamond;

public class D implements B, C{
    public static void main(String[] args) {
        System.out.println(new D().method());
    }

    public int method(){
        return 0;
    }
}
