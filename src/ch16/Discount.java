package com.khc.practice.modernjava.ch16;

import static java.lang.String.format;

public class Discount {
    public enum Code {
        NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

        private final int percentage;

        Code(int percentage){
            this.percentage = percentage;
        }
    }

    public static String applyDiscount(Quote quote){
        return quote.getShopName() + " price is "+ Discount.apply(quote.getPrice(), quote.getDiscountCode());
    }

    public static String apply(double price, Code code){
        delay();
        return format("%.2f",price * (100 - code.percentage) / 100);
    }

    public static void delay(){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            throw new RuntimeException();
        }
    }
}
