package com.khc.practice.modernjava.ch19.lazy;

public interface MyList<T> {

    T head();

    MyList<T> tail();

    default boolean isEmpty(){
        return true;
    }
}
