package com.khc.practice.modernjava.ch13.diamond;

public interface A {
    default int method(){
        return 123;
    }
}
