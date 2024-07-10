package com.khc.practice.modernjava.ch13.basic;


public class Practice{
    public static void main(String[] args) {
        try{
            System.out.println(new Cal().divde(1, 0));
        }catch(ArithmeticException e){
            System.out.println("Exception 발생");
        }

    }
}
