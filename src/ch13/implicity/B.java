package com.khc.practice.modernjava.ch13.implicity;

public interface B {
    default int method(){
        return 123;
    }
}
