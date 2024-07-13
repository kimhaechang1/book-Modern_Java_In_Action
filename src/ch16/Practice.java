package com.khc.practice.modernjava.ch16;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Practice {

    static final List<Shop> shops = Arrays.asList(new Shop("BestPrice"),
            new Shop("LetsSaveBig"),
            new Shop("MyFavoriteShop"),
            new Shop("BuyItAll1"),
            new Shop("BuyItAll2"),
            new Shop("BuyItAll3"),
            new Shop("BuyItAll4"),
            new Shop("BuyItAll5"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll7"),
            new Shop("BuyItAll8"),
            new Shop("BuyItAll9"));
            // 여기까지는 병렬로 처리하나 차이가 크게 없다.

            /*new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"),
            new Shop("BuyItAll6"))*/;

    private static final Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true); // main 스레드가 종료될시 함께 종료되도록
                    return t;
                }
            });

    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        getTimePOfCompletableFuture("myPhone27S");
        System.out.println("available processors: "+Runtime.getRuntime().availableProcessors());
        System.out.println("item size: "+shops.size());
//        getTimeOfParalle();
        // 가용가능한 스레드가 Runtime.getRuntime().availableProcessors로 묶여있다.
//        getTimeOfEnhancedFindPricesAsyncStream("myPhone27S");
        // 최대 한계치는 공식으로 정해져 있지만, 그 수를 넘지않는다면 필요한 만큼 사용한다.
        // printTime(() -> findPricesWithDiscountVer2("myPhone27S"));
//        completeOnTimeout();
        getTimeOfFindPricesStream();

    }

    public static void printTime(Runnable runnable){
        long start = System.nanoTime();
        runnable.run();
        long duration = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Done in "+ duration+" msecs");
    }

    public static void basicSequential(){
        long start = System.nanoTime();
        System.out.println(findPrices("myPhone27S"));
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in "+duration + " msecs");
    }

    public static void getTimeOfParalle(){
        long start = System.nanoTime();
        System.out.println(paralleStream("myPhone27S"));
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in "+duration + " msecs");
    }

    public static List<String> paralleStream(String product){
        return shops.parallelStream()
                .map(shop -> {
                    System.out.println(Thread.currentThread().getName()+" working");
                    return String.format("%s price is %.2f", shop.getName(), shop.getPrice(product));
                })
                .toList();
    }

    public static void getTimePOfCompletableFuture(String product){
        long start = System.nanoTime();
        System.out.println(findPricesAsyncStream("myPhone27S"));
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in "+duration + " msecs");
    }

    public static List<String> findPricesAsyncStream(String product){
        List<CompletableFuture<String>> completableFutures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName()+" price is "+shop.getPrice(product)))
                .toList();

        return completableFutures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public static void getTimeOfEnhancedFindPricesAsyncStream(String product){
        long start = System.nanoTime();
        System.out.println(enhancedFindPricesAsyncStream("myPhone27S"));
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in "+duration + " msecs");
    }

    public static List<String> enhancedFindPricesAsyncStream(String product){

        List<CompletableFuture<String>> completableFutures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> {
                    System.out.println(Thread.currentThread().getName()+" working");
                    return shop.getName()+" price is "+shop.getPrice(product);
                }, executor))
                .toList();

        return completableFutures.stream()
                .map(CompletableFuture::join)
                .toList();
    }


    public static List<String> findPrices(String product){
        return shops.stream()
                .map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)))
                .collect(toList());
    }
    public static void doSomethingElse(){

    }

    public static void doShopExample(){
        Shop shop = new Shop();
        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceSupply("my favorite product");
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Invocation returned after "+ invocationTime + " msec");

        doSomethingElse();

        try{
            double price = futurePrice.get();
            System.out.printf("Price is %.2f\n", price);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Price returned after "+ retrievalTime + " msecs");
    }

    public static List<String> findPricesWithDiscount(String product){
        return shops.stream()
                .map(shop -> shop.getPriceWithDiscount(product))
                .map(Quote::parse)
                .map(Discount::applyDiscount)
                .toList();
    }

    public static List<String> findPricesWithDiscountVer2(String product){
        // 전체 동작은 다음과 같다.
        // 1. 상점별로 product에 포함된 상품의 정보를 들고온다.
        // 2. 들고온 정보를 파싱한다.
        // 3. 파싱된 정보를 기반으로 할인된 가격을 얻어온다.

        // 여기서 1번과 3번이 비동기고, 무조건 앞선 순서가 먼저 행해지고 뒤에 순서 번호가 작동해야 하므로
        // 1번과 2번을 applyThen으로 이어서 비동기 완료후를 보장하고
        // 2 -> 3번을 이을때에는 또다른 비동기 함수에게 인자를 넘겨야하므로 thenCompose를 사용한다.
        List<CompletableFuture<String>> completableFutures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPriceWithDiscount(product), executor))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future.thenCompose(parsed -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(parsed))))
                .toList();

        return completableFutures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public static void timeoutExample() {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e){}
            return 10;
        }).orTimeout(1, TimeUnit.SECONDS);

        Future<Integer> result = future.handle((r, ex)->{
            if(ex != null) throw new RuntimeException(ex);
            System.out.println(r);
           return r;
        });

        try{
            result.get();
        }catch(RuntimeException e){
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static int hardWork(){
        try{
            Thread.sleep(2000);
        }catch(InterruptedException e){}
        return 10;
    }

    public static void completeOnTimeout(){
        CompletableFuture future = CompletableFuture.supplyAsync(() -> hardWork())
                .completeOnTimeout(1, 1, TimeUnit.SECONDS)
                .thenCombine(CompletableFuture.supplyAsync(() -> hardWork()).completeOnTimeout(1, 1, TimeUnit.SECONDS), (x, y) -> x + y);
        try{
            System.out.println(future.get());
        }catch(ExecutionException e){

        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Random random = new Random();

    public static void randomDelay(){
        int delay = 500 + random.nextInt(2000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getTimeOfFindPricesStream(){
        long start = System.nanoTime();
        CompletableFuture[] futures = findPricesStream("myPhone27S")
                .map(f -> f.thenAccept(x -> System.out.println(x + " ( done in "+ ((System.nanoTime() - start) / 1_000_000) + "msecs )")))
                // thenApply: 끝나고나서 적용시키는거고
                // thenAccept: 끝나고나면 소비하는것
                // 즉, 하나하나 비동기 함수들에 thenAccept를 연결하고 연결완료된 CompletableFuture 배열을 반환한다.
                .toArray(size -> new CompletableFuture[size]);

        CompletableFuture.allOf(futures).join();
        // join을 통해 결과를 받을 위치를 선정한다.

    }

    public static Stream<CompletableFuture<String>> findPricesStream(String product){
        return shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPriceWithDiscount(product), executor))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future.thenCompose(quote -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote), executor)));
        // 여기까지 하나씩 비동기를 호출하는 과정들이다.
        // 최종적으로 하나가 완료되면 하나씩 처리하기 위해서이다.
    }
}
