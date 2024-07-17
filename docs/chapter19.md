## 커링

보통 어떤 값을 다른 값으로 변환해야 하는 (섭씨에서 화씨로) 문제가 발생할때

변환요소와 기준치 조정요소가 단위 변환 결과를 좌우한다.

예를들어 섭씨온도를 화씨온도로 변환하는 과정은

```
CtoF(x) = x*9/5 + 32
```
로 표현할 수 있다.

즉, 이런 단위 변환함수들의 주요 패턴을 다음과 같이 정의할 수 있다.

1. 변환요소를 곱함

2. 기준치 조정 요소를 더함

이걸 메소드로 표현하면

```java
static double converter(double x, double f, double b){
    return x * f + b;
}

// x: 변환 하려는 값
// f: 변환 요소
// b: 기준치 조정 요소
```

위의 함수의 문제점은 해당 API를 사용하는 사용자 입장에서는 인수 순서를 헷갈릴 가능성이 크다는것이다.

그리고 무엇을 변환하는 메소드인지도 명확하게 해주려면, 목적에 맞는 이름으로 여러 메소드를 만들어야 덜 헷갈릴 것이다.

이런상황을 해결해주는 개념이 바로 `커링`이다.

우선 커링의 주요핵심은 **여러개의 인수를 받는 함수를 적은 인수를 받는 함수로 대체하는 기법**이다.

결국 커링은 새로운 함수를 정의하는 함수여야 한다.

```java
// 위에서 인자가 3개였는데, 함수를 정의하기 위해 필요한 인자는 f와 b이고 나중에 결과를 얻을때 필요한 인자는 x이다.
// 따라서 팩토리 함수의 인자는 2개이고, 팩토리로 찍어낸 함수의 인자는 1개이면 충분하다.

static DoubleUnaryOperator curriedConverter(double f, double b){
    return (double x) -> x * f + b;
}

public static void main(String[] args) {
    DoubleUnaryOperator cToFConverter = curriedConverter(9.0/5, 32);
    DoubleUnaryOperator USDtoGBPConverter = curriedConverter(0.6, 0);
}
```

## 영속 자료구조

함수형 메소드에서는 당연하게도 전역 자료구조나 인수로 전달된 자료구조를 갱신하면 안된다.

이로인해 다음 호출시에 결과가 달라질 수 있기 때문이다. (참조 투명성 위배)

결국 영속 자료구조라는 것은 갱신이 발생하더라도 기존 버전의 자신을 보존하는 것이다.

즉, 자기자신을 보존하기 위해 따로 복사하는것이 아닌, 필요한 부분을 새롭게 구축하여 기존과 연결한다.

### 예제: TrainJourney 

`TrainJourney`는 단방향 연결리스트로서 다음 목적지를 가리키는 `TrainJourney onward` 필드와 그 가격 정보를 포함하는 `price`필드가 있다.

여기서 두 `TrainJourney`를 연결하는 함수 `link`를 구현한다고 하면 다음과 같이 구현할 수 있다.

```java
static TrainJourney link(TrainJourney a, TrainJourney b){
    if(a == null) return b;

    TrainJourney t = a;
    while(t.onward != null){
        t = t.onward;
    }
    t.onward = b;
    return a;
}
```
위의 `link`는 의도한 대로 연결을 시키지만 연결된 새로운 결과를 만들어내는것이 아닌

기존의 `TrainJourney a`로 들어온 연결리스트를 수정하게 만든다.

따라서 `TrainJourney a`객체를 동시에 사용하는 다른사람들은 변경된것을 인지하지 못한체 잘못된 동작을 일으키게 된다.

```java
public class TrainJourney {
    TrainJourney onward;
    String name;
    int price;
    public TrainJourney(int price, String name, TrainJourney t){
        this.name = name;
        this.onward = t;
        this.price = price;
    }
    public TrainJourney(String name, TrainJourney t){
        this(0, name, t);
    }

    static TrainJourney link(TrainJourney a, TrainJourney b){
        if(a == null) return b;

        TrainJourney t = a;
        while(t.onward != null) {
            t = t.onward;
        }
        t.onward = b;
        return a;
    }
}
public class Practice {

    public static void main(String[] args) {
        TrainJourney XtoY = new TrainJourney("X", new TrainJourney(16, "Y", null));
        TrainJourney YtoZ = new TrainJourney("Y", new TrainJourney(18, "Z", null));

        TrainJourney XtoYtoZ = TrainJourney.link(XtoY, YtoZ);

        System.out.println(XtoY == XtoYtoZ);
        // true
    }
}
```

위를 해결하기 위해서 철저한 문서화 작업을 하는 경우도 있다.

하지만 "위 메소드는 왼쪽으로 들어온 인자를 변경할 가능성이 있음" 이런 류의 주석들이 늘어나면 갈수록

API를 사용하는 사용자 입장에서 복잡해질 뿐이다.

따라서 아래와 같이 함수형 프로그래밍 규칙을 깨지않는 함수를 만드는것이 좋다.
```java
static TrainJourney append(TrainJourney a, TrainJourney b){
    return a == null ? b : new TrainJourney(a.price, a.name, append(a.onward, b));
}

public static void main(String[] args) {
    TrainJourney XtoY = new TrainJourney("X", new TrainJourney(16, "Y", null));
    TrainJourney YtoZ = new TrainJourney("Y", new TrainJourney(18, "Z", null));

    TrainJourney noSideEffectXtoYtoZ = TrainJourney.append(XtoY, YtoZ);
    System.out.println(XtoY == noSideEffectXtoYtoZ);
}
```
## 스트림과 게으른 평가

기존에 자바에서 스트림은 게으른 평가를 지원하기 때문에, 실제로 소비하려고 하는 최종연산 함수가 호출되면 적절한 최적화와 함께 실행되었다.

하지만 다른 일반적인 메서드에서는 인수로 넘어온 값을 즉시 평가한다.

그렇기에 아래와 같은 코드는 무한한 재귀호출을 일으켜 `StackOverflowError`로 인해 터진다.

아래의 코드는 맨 앞(head: 무조건 소수)과 

그 밖의 수들을 head로 나누어떨어지는지 필터링을 거치고(tail: head로 걸러진 나머지 수들) 

tail의 맨앞은 또다시 head가 될 수 있기에 재귀호출을 하는 코드이다.

```java
static IntStream numbers() {
    return IntStream.iterate(2, n -> n+1);
}
static int head(IntStream numbers){
    return numbers.findFirst().getAsInt();
}
static IntStream tail(IntStream numbers){
    return numbers.skip(1);
}
static IntStream primes(IntStream numbers){
    int head = head(numbers);
    return IntStream.concat(
        IntStream.of(head),
        primes(tail(numbers).filter(n -> n % head != 0))
    );
}
```

위의 코드는 `findFirst()`로 인해 최종연산이 실행되어 다른 함수들을 호출 할 수 없다.

또한 호출한다 하더라도 자바의 함수는 즉시 평가되기 때문에, 평가를 미뤄서 할 수 없어서 무한한 재귀호출로 이어진다.

스칼라나 다른 언어에서는 함수에 대한 게으른 평가를 지원하지만

자바에서는 이를 구현하기 위해 평가되어야 할 메서드를 함수형 인터페이스로 둘러싸서 게으른 평가를 구현할 수 있다.

아래는 그것의 예제로, 실제 평가될때 연산되는 연결리스트인 `LazyList`를 만들 수 있다.

### 기존의 즉각적으로 메모리위에 존재해야하는 연결리스트

기존의 연결리스트는 평가가 이뤄지기전에도 이미 메모리위에 생성된 상태이다.

게으른 평가와 같이 최종소비될때 평가가 이루어지는것이 아니다.

```java
public interface MyList<T> {

    T head();

    MyList<T> tail();

    default boolean isEmpty(){
        return true;
    }
}

public class MyLinkedList<T> implements MyList<T>{
    private final T head;
    private final MyList<T> tail;
    public MyLinkedList(T head, MyList<T> tail){
        this.head = head;
        this.tail = tail;
    }

    public T head(){
        return head;
    }

    public MyList<T> tail(){
        return tail;
    }

    public  boolean isEmpty(){
        // 연결리스트의 요소들을 추가할때 사용하는 클래스이기 때문에
        // 이 클래스를 사용한단건 자연스럽게 비어있지 않음을 의미
        return false;
    }
}

public class Empty<T> implements MyList<T> {
    // 리스트의 끝 요소를 나타내기 위한 더미요소
    // 더미요소에 대한 정보를 추출하는 메소드 호출은 막아야한다.
    @Override
    public T head() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MyList<T> tail() {
        throw new UnsupportedOperationException();
    }
}

public class Practice {
    public static void main(String[] args) {
        MyList<Integer> myList = new MyLinkedList<>(5, new MyLinkedList<>(15, new Empty<>()));
        // 연결리스트 생성
        // 위의 선언부와 동시에 평가되어 메모리상에 MyLinkedList 속 tail 객체들이 존재하게됨
        MyList<Integer> next = myList;
        StringBuilder sb = new StringBuilder();
        while(true){
            try{
                sb.append(next.head());
                next = next.tail();
                sb.append(" -> ");
            }catch(UnsupportedOperationException e){
                // 더미 노드에 접근하여 필드에 접근하는 메소드를 호출할 시 Exception 발생
                // 즉, 마지막 노드임을 알 수 있음
                System.out.println(sb);
                break;
            }
        }
    }
}
```
### `LazyList`

결국 함수형 인터페이스를 활용하여 나중에 평가되도록 감싸는 방법을 사용한다.

이 때 사용되는 함수형 인터페이스는 `Supplier<T>`를 사용한다.

```java
public class LazyList<T> implements MyList<T>{

    final T head;
    final Supplier<MyList<T>> tail;

    public LazyList(T head, Supplier<MyList<T>> tail){
        this.head = head;
        this.tail = tail;
    }

    @Override
    public T head() {
        return head;
    }

    @Override
    public MyList<T> tail() {
        // get()을 통해 다음 연결리스트 요소를 얻어와야 할 때에 생성하여 연결한다.
        return tail.get();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
```

이제 `tail()`메소드를 호출하기 전까지는 시작점만 알고 무한히 생성되는 연결리스트임을 의미하게 되고

`tail()`이 호출되는 순간에 평가되어 연결리스트 요소가 메모리위에 생성된다.

```java
MyList<Integer> myLazyList = from(2);
// 2부터 시작하는 게으른 리스트를 만들었다.
// 위의 매서드를 호출하였다고 하여 리스트가 즉각적으로 메모리위에 생성되지 않는다.
// 아래의 무한루프 코드를 통해 계속해서 tail을 호출하여 무한히 생성되는 모습을 볼 수 있다.
while(true){
    System.out.print(myLazyList.head()+" -> ");
    myLazyList = myLazyList.tail();
}
```

또한 `Stream`의 게으른 평가처럼 `from(int n)` 메소드를 통해 시작점이 주어진 무한한 연결리스트를 생성하고

특정 개수만큼만 생성 후 평가하면 그 개수만큼만 생성된다.

```java
LazyList<Integer> numbers = from(2);
int two = numbers.head();
int three = numbers.tail().head();
int four = numbers.tail().tail().head();
System.out.println(two+" "+three+" "+four);
// 2 3 4
```

위와같은 특성을 사용해서, 다시 무한스트림속 소수를 만들어내는 코드를 수정할 수 있다.

```java
static <T> void printAll(MyList<T> list){
    while(!list.isEmpty()){
        System.out.println(list.head());
        list = list.tail();
    }
}

public static LazyList<Integer> primes(LazyList<Integer> numbers){
    return new LazyList<>(
        numbers.head(),
        ()-> primes(
            numbers.tail().filter(n -> {
                return n % numbers.head() != 0;
            })
        )
    );
}

public LazyList<T> filter(Predicate<T> p){
    return isEmpty() ?
            this :
            p.test(head()) ?
                    new LazyList<>(head, () -> tail().filter(p)) :
                    tail().filter(p);
}

printAll(LazyList.primes(from(2)));
```

### 캐싱 또는 기억화

https://d2.naver.com/helloworld/9223303

어떤 순수함수의 실행을 최적화 하기위해 함수형 프로그래밍에서 캐싱을 유도하는 자료구조로 래핑한다.

이게 가능한 이유는 순수함수이기에 동일한 인자를 넣으면 동일한 결과가 언제나 유도되기 때문이다.

```java
static HashMap<String, Integer> cache;
public static void main(String[] args) {
    cache = new HashMap<>();
    System.out.println("result: "+toString("10"));
    System.out.println("result: "+toString("10"));
}

static Integer toString(String value){
    if(cache.containsKey(value)){
        System.out.println("get from cache...");
        return cache.get(value);
    }
    System.out.println("get after compute...");
    return cache.computeIfAbsent(value, Integer::parseInt);
}
```

하지만 위와같은 상황은 단일 스레드가 메서드에 진입할때에만 유효하고 멀티스레드환경에서는 공유 가변 변수 `cache`에 동시 접근이 되기 때문에, ConcurrentHashMap과 같은 멀티스레드 환경에서 보장이되는 캐시용 자료구조를 채택하는것이 좋다.

어찌되었건, 참조 투명성 특징으로 인해 함수형 프로그래밍의 순수함수는 캐싱을 적용시킬 수 있다.

