## 병렬 스트림 사용시 주의할 점

### 확신이 서지 않는다면 직접 측정하라

순차 스트림에서 병렬 스트림으로는 얼마든지 쉽게 바꿀 수 있다.

하지만 언제나 순차스트림보다 병렬스트림이 빠른것은 아니기 때문에, 직접 벤치마크로 측정해보는 것이 좋다.

### 박싱을 주의하자

오토 박싱이나 언박싱은 성능을 크게 저하시킬 수 있다.

자바 8에서는 오토박싱을 최대한 피할 수 있도록 기본형에 특화된 Stream 을 제공한다.

따라서 되도록이면 기본형 특화 스트림을 사용하는것이 좋다.

```java
public class Main {
    public static final int N = 10_000_000;
    public static long [] data = new long[N+1];
    public static Long [] wrapperData = new Long[N+1];


    @Setup
    public void before(){
        for(int i  = 0;i <= N;i++){
            data[i] = i;
            wrapperData[i] = Long.valueOf(i);
        }
    }

    @Benchmark
    public long noBoxedSum(){
        return Arrays.stream(data).reduce(0L, Long::sum);
    }

    @Benchmark
    public long boxedSum(){
        return Arrays.stream(wrapperData).reduce(0L, Long::sum);
    }
}
```

```java
Benchmark        Mode  Cnt   Score    Error  Units
Main.boxedSum    avgt   20  86.842 ± 23.057  ms/op
Main.noBoxedSum  avgt   20  15.494 ±  2.610  ms/op
```

위의 결과에서 볼 수 있듯이, 박싱이 일어나는 연산에서는 평균적으로 `60~100ms`가 발생하지만

박싱이 일어나지 않는 연산에서는 `13~17ms` 로 줄어든다.

### 병렬 스트림에서 공유자원 접근은 삼가하자

병렬 스트림 사용에서 가장 많이 실수하는 것은 공유된 상태를 바꾸는 알고리즘을 사용하는 것

다음은 N까지의 자연수를 더하면서 공유된 누적자를 바꾸는 프로그램이다.

```java
public class Accumulator {
    public long total = 0;
    public void add(long value){
        total += value;
    }
}

public class Practice {

    public long sideEffectSum(long n){
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).forEach(accumulator::add); // 메소드 참조
        return accumulator.total;
    }

    public long sideEffectParallelSum(long n){
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add); // 메소드 참조
        return accumulator.total;
    }


    public static void main(String[] args) {
        System.out.println(new Practice().sideEffectSum(10_000_000));
        System.out.println(new Practice().sideEffectParallelSum(10_000_000));
    }
}
```

위의 코드를 실행하면 윗줄은 정상적으로 `50000005000000` 이 나오지만

아랫줄은 그 값이 발생하지 않는다.

이는 병렬 처리시 연산에 사용하는 공유된 자원인 `accumulator.total`에 update를 일으키기 때문이다.

즉, 여러 스레드가 `accumulator.add()`를 동시에 호출하여 발생하는 문제이다.

위와 같이 `공유된 가변상태`를 피해야 한다.

### 순차 스트림보다 병렬 스트림에서 성능이 떨어지는 연산을 주의하자

특히 `limit`나 `findFirst`와 같은 요소의 순서에 의존하는 연산은 병렬 스트림에서 수행하려면 비싼 비용을 치뤄야 한다.

대신, 정렬이 굳이 필요없는 상황에서는 `unordered`를 호출하여 비정렬된 스트림을 얻고

이 스트림에서 `limit`를 호출하는 것이 더욱 효율적이다.

또한 `findFirst`가 아니라 어떠한 순서도 상관없이 선택하는 `findAny`가 더욱 효율적이다.

### 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라

처리해할 요소수가 단순히 많은것 뿐만아니라

더욱 중요한것은 하나의 요소에 대한 연산이 얼마나 무거운가가 병렬 스트림으로 얻을 수 있는 이득을 상승 시킨다.

### 스트림을 구성하는 자료구조가 적절한지 확인하라

예를들어 `ArrayList`와 같이 인덱스를 가지고 순차적으로 존재하는 데이터는 분할하기 편하지만

`LinkedList`의 경우에는 분할을 위해서 전체 데이터를 탐색해야 하는 비효율이 있다.

또한 `range` 혹은 `rangeClosed`로 생성한 기본형 스트림도 쉽게 분할이 가능하다.

```java
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Main {
    public static final int N = 10_000_000;
    public static long [] data = new long[N+1];
    public static Long [] wrapperData = new Long[N+1];

    public static ArrayList<Long> list = new ArrayList<>();
    public static LinkedList<Long> linkedList = new LinkedList<>();

    @Setup
    public void before(){
        for(long i  = 0;i <= N;i++){
            list.add(i);
            linkedList.add(i);
        }
    }

    @Benchmark
    public void arrayListParallelSum(){
        long sum = list.parallelStream()
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public void linkedListParallelSum(){
        long sum = linkedList.parallelStream()
                .reduce(0L, Long::sum);
    }

    @TearDown(Level.Invocation) // 매 밴치마크 실행한 다음에는 아래의 메서드 실행
    public void tearDown(){
        System.gc();
    }
}
```

```java
Benchmark                   Mode  Cnt    Score    Error  Units
Main.arrayListParallelSum   avgt   20   28.930 ± 11.925  ms/op
Main.linkedListParallelSum  avgt   20  155.109 ± 22.948  ms/op
```

### 스트림의 특징과 파이프라인의 중간 연산이 스트림 특성을 어떻게 바꾸는지에 따라 분할 과정 성능이 달라진다.

예를들어 `SIZED` 스트림의 경우 정확히 같은 크기의 두 스트림으로 분할이 가능하다.

하지만 중간 연산에 필터연산이 끼여있다면, 길이를 예측하기 힘들기에 효과적으로 처리가 가능한지 예측하기 힘들어진다.

### 최종 연산의 병합과정이 비싼지 살펴보라

만약 최종 연산의 병합과정 (`Collector`의 `combine`메소드) 의 비용이 비싸다면 병렬스트림을 통해 멀티 코어를 활용한 이득이

병합과정에서 걸리는 오버헤드로 인해 상쇄 될 수 도 있다.

## 포크 조인 프레임 워크

포크 조인 프레임워크는 일반적인 스레드풀과 동일하게 작업큐에서 작업을 꺼내서 작업하는데,

작업자는 꺼낸 작업의 크기를 측정하여, 그 크기가 일정 기준보다 크다면 분할하여 별도의 스레드가 가질수 있도록 `fork`를 호출한다.

포크 조인 프레임워크의 특징은 다른 스레드풀과는 달리 각 작업자 스레드가 별도의 작업 큐를 갖고 있고

워크-스틸링 알고리즘이 적용되어, 다른 작업자 스레드의 큐에 접근이 가능하다는 점이 있다.

따라서 `fork`는 별도의 스레드가 작업을 가질 수 있도록, 작업자 본인의 큐에 다시 집어넣는다

그래서 작업자 스레드를 관리하는 구현체는 `ForkJoinPool` 이며, 작업을 구현하는 추상 클래스는 `RecursiveTask<R>` 과 `RecursiveAction`이 있다.

그리고 `ForkJoinPool` 클래스를 내부적으로 보면 `Runtime.getRuntime().availableProcessors()`가 있는데

```java
public ForkJoinPool() {
    this(Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()),
            defaultForkJoinWorkerThreadFactory, null, false,
            0, MAX_CAP, 1, null, DEFAULT_KEEPALIVE, TimeUnit.MILLISECONDS);
}

public ForkJoinPool(int parallelism,
                    ForkJoinWorkerThreadFactory factory,
                    UncaughtExceptionHandler handler,
                    boolean asyncMode,
                    int corePoolSize,
                    int maximumPoolSize,
                    int minimumRunnable,
                    Predicate<? super ForkJoinPool> saturate,
                    long keepAliveTime,
                    TimeUnit unit){
                        //...
                    }
```

생성자를 면밀히 보면 `parallelism`으로 값이 대입된다. `parallelism`은 해당 포크 조인 풀에서 동시에 작업할 스레드의 수를 정하는 변수이다.

멀티코어를 최대한 활용하려면, 보통 작업자 스레드수와 하드웨어 스펙이 비슷해야 한다.

따라서 `Runtime.getRuntime().availableProcessors()`은 JVM이 가용가능한 코어 개수이며, 실제로 본인 노트북의 논리 프로세서 개수와 동일하게 반환한다.

저기서 `MAX_CAP`은 약 32000으로, `ForkJoinPool`에서 지원가능한 최대 작업자 스레드수 제한이다.

### 포크 조인 프레임워크: RecursiveTask 활용

스레드 풀을 사용하려면 `RecursiveTask<R>`의 서브클래스를 만들어야 한다.

여기서 `R`은 병렬화된 테스크가 생성하는 결과 형식 또는 결과가 없을때 `RecursiveAction`의 형식이다.

그 내부에는 구현해야할 메소드로 `compute`가 있는데

다음과 같은 수도 코드를 가진다.

```java
if (테스크가 충분히 작거나 더 이상 분할할 수 없으면){
    순치적으로 태스크 계산하기
} else {
    태스크를 두 서브테스크로 분할
    태스크가 다시 서브테스크로 분할되도록 이 메서드를 재귀적으로 호출
    모든 서브태스크의 연산이 완료될때 까지 기다림(join)
    각 서브 태스크의 결과를 합침 (정복)
}
```

`long [] `로 이루어진 숫자배열을 사용하여 n까지의 자연수 덧셈 작업을 포크-조인 프레임워크로 구현한다.

```java
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

    private final long [] numbers;
    private final int start;
    private final int end;
    public static final long THRESHOLD = 10_000;

    public ForkJoinSumCalculator(long [] numbers){
        this(numbers, 0, numbers.length);
    }

    private ForkJoinSumCalculator(long [] numbers, int start, int end){
        this.numbers =numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        System.out.println(Thread.currentThread().getName()+" "+"start: "+this.start+" end: "+this.end);
        int length = end - start;
        if(length <= THRESHOLD){
            // 현재 최소한의 작업단위만큼 충분히 작아졌다면
            // 순차적으로 수행하고 결과를 반환한다.
            return computeSequentially();
        }
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length/2);
        leftTask.fork();
        // 왼쪽 분할영역을 fork를 사용하여 별도의 스레드에서 동작시킨다.
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length/2, end);
        Long rightResult = rightTask.compute();
        // 오른쪽 분할 영역에 대해서 더욱 분할이 가능하도록 compute를 호출한다.
        Long leftResult = leftTask.join();
        // 왼쪽 별도의 스레드에서 하던 작업이 끝날때 까지 기다린다.
        return rightResult + leftResult;
    }

    private long computeSequentially() {
        long sum = 0;
        for(int i= start;i< end;i++){
            sum += numbers[i];
        }
        return sum;
    }
}
```

```
ForkJoinPool-1-worker-1 start: 0 end: 100000
ForkJoinPool-1-worker-1 start: 50000 end: 100000
ForkJoinPool-1-worker-1 start: 75000 end: 100000
ForkJoinPool-1-worker-1 start: 87500 end: 100000
ForkJoinPool-1-worker-1 start: 93750 end: 100000
ForkJoinPool-1-worker-1 start: 87500 end: 93750
ForkJoinPool-1-worker-1 start: 75000 end: 87500
ForkJoinPool-1-worker-2 start: 0 end: 50000
ForkJoinPool-1-worker-1 start: 81250 end: 87500
ForkJoinPool-1-worker-2 start: 25000 end: 50000
ForkJoinPool-1-worker-1 start: 75000 end: 81250
ForkJoinPool-1-worker-3 start: 50000 end: 75000
ForkJoinPool-1-worker-2 start: 37500 end: 50000
ForkJoinPool-1-worker-1 start: 50000 end: 62500
ForkJoinPool-1-worker-2 start: 43750 end: 50000
ForkJoinPool-1-worker-4 start: 0 end: 25000
ForkJoinPool-1-worker-2 start: 37500 end: 43750
ForkJoinPool-1-worker-3 start: 62500 end: 75000
ForkJoinPool-1-worker-2 start: 25000 end: 37500
ForkJoinPool-1-worker-4 start: 12500 end: 25000
ForkJoinPool-1-worker-1 start: 56250 end: 62500
ForkJoinPool-1-worker-5 start: 0 end: 12500
ForkJoinPool-1-worker-6 start: 62500 end: 68750
ForkJoinPool-1-worker-5 start: 6250 end: 12500
ForkJoinPool-1-worker-2 start: 31250 end: 37500
ForkJoinPool-1-worker-8 start: 12500 end: 18750
ForkJoinPool-1-worker-3 start: 68750 end: 75000
ForkJoinPool-1-worker-7 start: 50000 end: 56250
ForkJoinPool-1-worker-1 start: 0 end: 6250
ForkJoinPool-1-worker-4 start: 18750 end: 25000
ForkJoinPool-1-worker-10 start: 25000 end: 31250
5000050000
```

### 포크 조인 프레임워크 사용시 주의 할 점

- `RecursiveTask` 내에서는 `ForkJoinPool`의 `invoke`메서드를 사용하지 말고 `compute`나 `fork`를 호출해야 한다.

  - 그 차이는 내부 구현에 있어서 차이가 있다. `invoke`는 내부적으로 `join`을 호출해야 한다. 왜냐하면 작업이 끝나야고 반환을 해야하기 때문이다.
    하지만 `fork`의 경우에는 자신의 작업큐에 넣고서 기다리지 않고, 다른 작업자가 스틸링 해가기를 기대하게 된다.

- 멀티코어에서 포크 조인 프레임워크가 무조건적으로 빠르다는 생각은 버려야 한다. 특히, 컴파일러의 최적화는 순차버전에 집중되어 있다는 점이 있다.

## Spliterator 인터페이스

Java8에 추가된 `Spliterator`는 분할 가능한 반복자 로서 `Iterator`와 같이 요소 내 탐색 기능을 기본으로 제공하고

병렬작업에 특화되어 있다.

그리고 컬렉션 프레임워크에 포함된 모든 자료구조에 사용할 수 있는 디폴트 `Spliterator` 구현을 제공한다.

```java
// List.java에서 발췌...
@Override
default Spliterator<E> spliterator() {
    if (this instanceof RandomAccess) {
        return new AbstractList.RandomAccessSpliterator<>(this);
    } else {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
}
```

그 내부는 다음과 같다.

```java
public interface Spliterator<T>{
    boolean tryAdvance(Consumer<? super T> action);
    Spliterator<T> trySplit();
    long estimateSize();
    int characteristics();
}
```

- `boolean tryAdvance(Consumer<? super T> action)`: 일반적인 `Iterator`와 동일하게, 다음 탐색할 요소가 있으면 소비하고, 없으면 `false`를 반환한다.
- `Spliterator<T> trySplit()`: `Spliterator`의 일부 요소를 분할해서 두 번째 `Spliterator`를 만들어낸다.
- `long estimateSize()`: 탐색을 해야할 요소의 수 정보를 제공한다.
- `int characteristics()`: `Collector` 만들때와 비슷하게 `Spliterator`의 특성을 정한다.

### Spliterator 인터페이스 활용: 단어수 세기 병렬처리

다음과 같은 문자열 속에서 공백을 기준으로 단어를 측정하는

```java
final String SENTENCE =
    "Nel mezzo del cammin di nostra vita" +
    "mi ritrovai in selva oscura" +
    "ch la dritta via era smarrita";
```

커스텀 `Spliterator`를 만들어보자.

```java
package modernjavainaction.ch07;

import java.util.Spliterator;
import java.util.function.Consumer;

public class WordCounterSpliterator implements Spliterator<Character> {

    private final String string;
    private int currentChar = 0;
    public WordCounterSpliterator(String string){
        this.string = string;
    }
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        // 현재 분할된 String을 소비하는(탐색하는) 메소드
        action.accept(string.charAt(currentChar++));
        return currentChar < this.string.length();
    }

    @Override
    public Spliterator<Character> trySplit() {
        String currentThreadName = Thread.currentThread().getName();

        // 현재 문자열 기준으로 분할하여 새로운 Spliterator를 만드는 메소드
        int currentSize = this.string.length() - currentChar;
        if(currentSize < 10){
             // 충분히 작아졌다면, 더이상 분할이 일어나지 않는다는 의미로 null 반환
            return null;
        }
        for(int splitPos = currentSize / 2 + currentChar;
            // 전체 문자열 중 현재 길이의 중간지점에서 부터
            splitPos < this.string.length();
            splitPos++
        ){
            if(Character.isWhitespace(string.charAt(splitPos))){
                Spliterator<Character> spliterator = new WordCounterSpliterator(
                        string.substring(currentChar, splitPos));
                currentChar = splitPos;
                return spliterator;
            }
        }
        return null;
    }


    @Override
    public long estimateSize() {
        return string.length() - currentChar;
    }

    @Override
    public int characteristics() {
        // ORDERED: 현재 처리해야 하는 문자열의 문자 등장순서가 유의미함을 나타냄
        // SIZED: estimatedSize가 반환하는 값이 정확하다는 의미
        // SUBSIZED: trySplit 으로 생성된 Spliterator도 정확한 크기를 가짐
        // IMMUTABLE: 문자열 자체가 불변이므로 속성이 추가되지 않음
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}
```

만들어진 커스텀 `Spliterator`를 사용하기 위해서 `reducer`를 정의하자

이때, 하나의 아이템에 대한 속성이 여러가지 있기에, 클래스로 정의한다.

```java
package modernjavainaction.ch07;


public class WordCounter {

    // 자바에는 여러가지 다형의 상태를 표현하는 자료구조가 없으므로 클래스로 래핑한다.

    private final int counter;
    private final boolean lastSpace;

    public WordCounter(int counter, boolean lastSpace){
        this.counter = counter;
        this.lastSpace = lastSpace;
    }

    public WordCounter accumulator(Character c){
        if(Character.isWhitespace(c)){
            return lastSpace ?
                    this :
                    new WordCounter(counter, true);
        } else {
            return lastSpace ?
                    new WordCounter(counter + 1, false) :
                    this;
        }
    }
    public WordCounter combine(WordCounter wordCounter){
        return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
    }

    public int getCounter(){
        return counter;
    }
}
```

사용하는건 다음과 같다.

```java
public static int getCountBySpliterator(String s){
    Spliterator<Character> spliterator = new WordCounterSpliterator(s);
    Stream<Character> stream = StreamSupport.stream(spliterator, true);
    WordCounter wordCounter = stream.parallel().reduce(
            new WordCounter(0, true),
            WordCounter::accumulator,
            WordCounter::combine
    );
    return wordCounter.getCounter();
}
```
