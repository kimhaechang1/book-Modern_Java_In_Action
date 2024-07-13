package com.khc.practice.modernjava.ch16;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Shop {


    private String product;
    private static final Random random = new Random();

    public Shop(){

    }
    public Shop(String product){
        this.product = product;
    }


    public String getName(){
        return this.product;
    }

    public double getPrice(String product){
        // impl
        // 이 메소드는 실행과 결과가 한곳에서 일어날 수 있으며, 실제 연산 메소드에 의해 블록킹 상황이 일어난다.
        return calculatePrice(product);
    }

    public String getPriceWithDiscount(String product){
        double price = calculatePrice(product);
        Discount.Code code = Discount.Code.values()[random.nextInt(Discount.Code.values().length)];
        return String.format("%s:%.2f:%s", getName(), price, code);
    }

    public Future<Double> getPriceAsync(String product){
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        // 비동기 함수의 결과를 조작할 수 있는 CompletableFuture를 사용한다.
        new Thread(() -> {

            try{
                double price = calculatePrice(product);
                // 별도의 스레드에서 비동기 API를 호출하고
                // throw new RuntimeException("product invalid"); -> Exception 체험
                futurePrice.complete(price);
                // 그 결과값을 CompletableFuture로 관리하도록 호출한다.
            }catch(Exception e){
                futurePrice.completeExceptionally(e);
            }

        }).start();
        // 이 스레드의 결과를 이 메소드에서 기다리지 않고 바로 반환된다.
        // 왜냐하면 CompletableFuture가 결과값에 대해 감시? 하고있을거임
        return futurePrice;
    }

    public Future<Double> getPriceSupply(String product){
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> {
            // throw new RuntimeException("product invalid"); exception test
            return calculatePrice(product);
        });
        future.exceptionally(ex -> {
            future.completeExceptionally(ex);
            return 0.0;
        });
        return future;
    }

    private double calculatePrice(String product){
        delay();
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    public static void delay(){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            throw new RuntimeException();
        }
    }




}
