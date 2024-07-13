### 기존 `Future<T>`의 한계

기존에도 비동기로 호출하여 별도의 스레드에서 오래걸리는 동작을 동작하게 만들 수 있엇다.

그리고 중간 상태도 확인이 가능하며, 너무 오래걸리지 않도록 시간제한을 걸수 있었다.

하지만 다음과 같은 상황을 `Future<T>` 로 해결하기 쉽지않다.

- 두 개의 비동기 계산결과를 하나로 합친다. 두 가지 계산결과는 독립적일 수 있으며, 두 번째 결과가 첫 번째 결과에 의존하는 상황일 수 있다.

- `Future<T>`집합에서 가장 빨리 완료되는 태스크를 기다렸다가 결과를 얻는다. (같은 결과를 얻기위해 여러 다양한 방식으로 트라이하는 경우)

- 프로그램적으로 `Future`를 완료시킨다. (즉, 비동기 동작에 수동으로 결과 제공)

- `Future` 완료 동작에 반응한다.(즉, 결과를 기다리면서 블록되지 않고 결과가 준빈되었다는 알림을 받은 다음에 Future의 결과로 원하는 추가동작을 수행할 수 있음)

### 동기 API와 비동기 API

`동기 API`는 메서드를 호출한 다음에 메서드가 계산을 완료할 때 까지 기다렸다가 메서드가 반환되면 호출자는 반환된 값으로 계속 다른 동작을 수행한다.

이러한 동기 API를 사용하는 상황을 `블록 호출` 이라고 한다.

동기라는 단어뜻 자체는 동시에 라는 말이고, 호출과 결과가 같은 시점에 발생한다는것

`비동기 API`는 메서드가 즉시 반환되며, 끝내지 못한 작업을 호출자 스레드와 동기적으로 실행될 수 있도록 다른 스레드에게 할당한다. 

동작완료에 대해서는 작업자 스레드에게 콜백 메서드를 함께 던져서 작업이 끝나면 호출하도록 하거나

호출자가 '계산 결과가 끝날 때까지 기다림'메서드를 추가로 호출하면서 전달된다.

비동기라는 단어뜻은 호출과 결과가 같은 시점이 아닌 상황을 의미한다.

```java
public static void main(String[] args) {
    Shop shop = new Shop();
    long start = System.nanoTime();
    Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
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
```
```
Invocation returned after 2 msec
Price is 229.78
Price returned after 1026 msecs
```
즉, 위의 코드 상황에서 호출시점에 리턴문으로 반환되어지지만, 실제 원하는 값을 받는 시점을 별도로 선정할 수 있다. -> 비동기 API

그리고 해당 반복되는 이야기지만 호출 시점에 곧바로 반환코드로 인해 되돌아와서 메인 흐름을 끊지 않는다 -> 논블록 상황

마지막으로 결과를 기다려야만 하는, 호출 시점과는 다른 위치에서 비동기 호출이 완료되기를 기대한다 -> 블록 상황

### 에러 처리방법

기존의 `Future`는 사용하지 않은 버전에서는 에러가 발생할 경우를 대비하여 `TimeoutException`을 잡기위한 목적으로

오버로딩된 `get` 메소드를 호출하였다. 이렇게 되면 영원히 기다리게 되는 블록문제를 해결할 수 있지만 에러가 왜 발생했는지 알 수 없다.

따라서 `completeExceptionally` 메소드를 호출하여 Exception 객체를 누적하여 던지면 원인도 알 수 있다.

### `Stream` 과 `CompletableFuture`

함께 사용할 때 논블럭킹 비동기 호출의 이점을 챙기려면, 

결과를 기다리는 스트림과 호출 스트림을 분리해야한다.

```java
public static List<String> findPricesAsyncStream(String product){
    return shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName()+" price is "+shop.getPrice(product)))
            .map(CompletableFuture::join)
            .toList();
}
```
즉, 위와같이 코드를 작성하면 결과적으로 하나씩 처리하게 된다.
```
Done in 4050 msecs
```

따라서 아래와 같이 변경해야 비동기 이점을 볼 수 있다.

```java
public static List<String> findPricesAsyncStream(String product){
    List<CompletableFuture<String>> completableFutures = shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName()+" price is "+shop.getPrice(product)))
            .toList(); 

    return completableFutures.stream()
            .map(CompletableFuture::join)
            .toList();
}
```
```
Done in 1024 msecs
```

또한 병렬스트림과 `CompletableFuture`를 통한 비동기 호출은 각각의 사용처에 따라 다르게 사용해야 한다.

병렬 스트림의 경우 포크-조인 풀을 사용하기에 `Runtime.getRuntime().availableProcessor()` 에 의존한다.

대부분의 `I/O`가 없는 계산위주 병렬처리의 경우에는 프로세서 코어 수 이상의 스레드가 필요없다.

하지만 비동기 API 호출의 경우 별도의 `Executor`를 사용하여 적합한 스레드수를 설정할 수 있다.

스레드 풀의 크기 조절 공식은 `Number of Thread = Number of core * 0..1 (CPU Utilization rate) * (1 + W/C) ` 

 - W / C 는 대기시간과 계산시간의 비율이다.

 - 비동기 호출의 경우 상황에 따라 다르겠지만 대부분 대기시간이 훨씬 크다.

아래의 두 예시 메소드를 호출시켜보면 차이를 느낄 수 있다.

지금 현재 코어수는 12개로 측정되고, 아이템이 12개일때 병렬 스트림 호출과 Executor를 활용한 비동기 API 호출의 시간차이다.

```java
private static final Executor executor = 
                    Executors.newFixedThreadPool(Math.min(shops.size(), 100),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setDaemon(true); // main 스레드가 종료될시 함께 종료되도록
                            return t;
                        }
                    });

public static List<String> paralleStream(String product){
    return shops.parallelStream()
            .map(shop -> {
                System.out.println(Thread.currentThread().getName()+" working");
                return String.format("%s price is %.2f", shop.getName(), shop.getPrice(product));
            })
            .toList();
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
```
```
available processors: 12
item size: 12
main working
ForkJoinPool.commonPool-worker-1 working
ForkJoinPool.commonPool-worker-3 working
ForkJoinPool.commonPool-worker-2 working
ForkJoinPool.commonPool-worker-6 working
ForkJoinPool.commonPool-worker-7 working
ForkJoinPool.commonPool-worker-5 working
ForkJoinPool.commonPool-worker-4 working
ForkJoinPool.commonPool-worker-9 working
ForkJoinPool.commonPool-worker-8 working
ForkJoinPool.commonPool-worker-10 working
ForkJoinPool.commonPool-worker-11 working
[BestPrice price is 135.47, LetsSaveBig price is 155.83, MyFavoriteShop price is 133.14, BuyItAll1 price is 161.11, BuyItAll2 price is 161.43, BuyItAll3 price is 220.42, BuyItAll4 price is 213.18, BuyItAll5 price is 167.35, BuyItAll6 price is 159.76, BuyItAll6 price is 222.04, BuyItAll6 price is 187.92, BuyItAll6 price is 196.46]

Done in 1034 msecs

Thread-0 working
Thread-2 working
Thread-1 working
Thread-3 working
Thread-4 working
Thread-5 working
Thread-6 working
Thread-7 working
Thread-8 working
Thread-9 working
Thread-10 working
Thread-11 working
[BestPrice price is 131.67964794685759, LetsSaveBig price is 220.7066024127546, MyFavoriteShop price is 189.60729938229446, BuyItAll1 price is 175.9592916555611, BuyItAll2 price is 179.61280211237568, BuyItAll3 price is 215.22031088890023, BuyItAll4 price is 220.69739357070796, BuyItAll5 price is 188.0525419467499, BuyItAll6 price is 129.1584665373743, BuyItAll6 price is 225.05845467328786, BuyItAll6 price is 174.3300210778138, BuyItAll6 price is 191.84396651763154]

Done in 1013 msecs
```

아이템이 12개를 넘어선 22개가 되면 속도 차이가 발생한다.

```
available processors: 12
item size: 22
main working
ForkJoinPool.commonPool-worker-1 working
ForkJoinPool.commonPool-worker-2 working
ForkJoinPool.commonPool-worker-5 working
ForkJoinPool.commonPool-worker-7 working
ForkJoinPool.commonPool-worker-4 working
ForkJoinPool.commonPool-worker-8 working
ForkJoinPool.commonPool-worker-6 working
ForkJoinPool.commonPool-worker-9 working
ForkJoinPool.commonPool-worker-3 working
ForkJoinPool.commonPool-worker-10 working
ForkJoinPool.commonPool-worker-11 working
ForkJoinPool.commonPool-worker-4 working
ForkJoinPool.commonPool-worker-6 working
ForkJoinPool.commonPool-worker-10 working
ForkJoinPool.commonPool-worker-1 working
main working
ForkJoinPool.commonPool-worker-9 working
ForkJoinPool.commonPool-worker-7 working
ForkJoinPool.commonPool-worker-11 working
ForkJoinPool.commonPool-worker-2 working
ForkJoinPool.commonPool-worker-3 working
[BestPrice price is 168.08, LetsSaveBig price is 205.00, MyFavoriteShop price is 138.03, BuyItAll1 price is 126.04, BuyItAll2 price is 172.35, BuyItAll3 price is 202.58, BuyItAll4 price is 121.29, BuyItAll5 price is 155.10, BuyItAll6 price is 161.48, BuyItAll6 price is 153.43, BuyItAll6 price is 201.14, BuyItAll6 price is 222.89, BuyItAll6 price is 193.79, BuyItAll6 price is 159.94, BuyItAll6 price is 175.29, BuyItAll6 price is 138.89, BuyItAll6 price is 121.07, BuyItAll6 price is 205.74, BuyItAll6 price is 194.00, BuyItAll6 price is 135.03, BuyItAll6 price is 214.81, BuyItAll6 price is 197.82]

Done in 2036 msecs -> 병렬 스트림

Thread-0 working
Thread-1 working
Thread-2 working
Thread-3 working
Thread-4 working
Thread-5 working
Thread-6 working
Thread-7 working
Thread-8 working
Thread-9 working
Thread-10 working
Thread-11 working
Thread-12 working
Thread-13 working
Thread-14 working
Thread-15 working
Thread-16 working
Thread-17 working
Thread-18 working
Thread-19 working
Thread-20 working
Thread-21 working
[BestPrice price is 135.40430900361602, LetsSaveBig price is 196.5999417026431, MyFavoriteShop price is 166.3121737853949, BuyItAll1 price is 223.34659420604243, BuyItAll2 price is 127.91699921513299, BuyItAll3 price is 174.87405253660563, BuyItAll4 price is 196.0578610041389, BuyItAll5 price is 154.2739622673079, BuyItAll6 price is 154.76818747866344, BuyItAll6 price is 216.1549343913274, BuyItAll6 price is 171.60145194864725, BuyItAll6 price is 184.6434262335051, BuyItAll6 price is 142.20808949841023, BuyItAll6 price is 172.06643023998754, BuyItAll6 price is 204.00361465677537, BuyItAll6 price is 143.3570871552896, BuyItAll6 price is 225.59263586967072, BuyItAll6 price is 147.20835179046702, BuyItAll6 price is 216.21762745260725, BuyItAll6 price is 138.91722305646536, BuyItAll6 price is 215.70524879662003, BuyItAll6 price is 223.87046264229775]

Done in 1028 msecs -> Executor에 의한 커스텀 풀
```

### JAVA 9에 추가된 `orTimeout()` 과 `completeOnTimeout()`

기존 `Future`에서는 `TimeoutException`을 일으키는 오버로딩된 `get()` 메소드를 통해 영원히 블록하는 현상을 막을 수 있었다.

`CompletableFuture`의 경우에는 동일하게 시간제한을 걸 수 있는 `orTimeout()` 메소드와 일정 시간안에 비동기 호출이 끝나지 않으면 기본값을 반환할 수 있는 `completeOnTimeout()`이 있다.

```java
public static int hardWork(){
    try{
        Thread.sleep(2000);
    }catch(InterruptedException e){}
    return 10;
}

public static void timeoutExample() throws ExecutionException, InterruptedException {
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
    }
}

public static void completeOnTimeout(){
    CompletableFuture future = CompletableFuture.supplyAsync(() -> hardWork())
            .completeOnTimeout(1, 1, TimeUnit.SECONDS) 
            // 1초 기다렸다가 1을 반환
            .thenCombine(CompletableFuture.supplyAsync(() -> hardWork())
                            .completeOnTimeout(1, 1, TimeUnit.SECONDS),
                            // 1초 기다렸다가 1을 반환
                        (x, y) -> x + y);
    try{
        System.out.println(future.get());
        // 따라서 1 + 1이 되므로, 2를 반환
    }catch(ExecutionException e){

    }catch (InterruptedException e) {
        throw new RuntimeException(e);
    }

}
```
