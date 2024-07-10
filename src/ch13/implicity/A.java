package com.khc.practice.modernjava.ch13.implicity;

public interface A {
    default int method(){
        return 456;
    }
}
