### takeWhile & dropWhile

일반적으로 어떤 프레디케이트 조건에 맞는 값들로 스트림을 만드는 방법을

`filter` 로 해결하려고 할 것이다.

그게 아니라 만약 소스가 이미 정렬된 상태고 특정 기준을 넘어서면 더이상 순회할 필요도 없을 것이다.

`takeWhile`은 프레디케이트의 false 가 된 순간에 종료된다.

`dropWhile`은 그 반대로 false가 된 순간부터 요소로 취급한다.

```java
// 프레디케이트 값이 false가 되는순간 종료
List<Dish> filteredMenu = specialMenu.stream()
        .takeWhile((dish)-> dish.getCalories() < 320)
        .collect(Collectors.toList());
System.out.println(filteredMenu);

// 프레디케이트 값이 false가 되는 순간부터 수집
List<Dish> filteredMenu1 = specialMenu.stream()
        .dropWhile((dish) -> dish.getCalories() < 320)
        .collect(Collectors.toList());
System.out.println(filteredMenu1);
```

```
[Dish{calories=120, name='seasonal fruit', vegetarian=true, type=OTHER}, Dish{calories=300, name='prawns', vegetarian=false, type=FISH}]
[Dish{calories=350, name='rice', vegetarian=true, type=OTHER}, Dish{calories=400, name='chicken', vegetarian=false, type=MEAT}, Dish{calories=530, name='french fries', vegetarian=true, type=OTHER}]
```

### findAny, findFirst

`findAny`: 비어있거나, 스트림 속에 어떤 무언가를 찾아줌

`findFirst`: 비어있거나, 스트림 속에 연속된 데이터중 첫번째를 반환함

`findAny` 진짜 아무거나 꺼내는것이기 때문에, 별다른 순서가 필요없다면 병렬 작업에 있어서 좋은 효율을 보인다.

### reduce

리듀스는 스트림 내에 연속된 요소들을 어떤 초기값을 기준으로 축적하는 목적으로 사용된다.

리듀스를 사용하는 기본적인 방법은 `reduce(acc, lambda)` 이다.

여기서 람다는 기본적으로 `BiFunction<T, U, R>` 이기 때문에 디스크립터가 `(T t, U u) -> R` 이거면 된다.

리듀스를 하는 메서드에는 초기값을 안주는 방법도 있는데

만약 소스가 비어서 리듀스가 작동하지 않는다면, 초기값이 있는 경우 초기값을 반환하면 되지만

초기값을 안주는 리듀스를 사용했을 때, 소스가 비어서 작동하지 않는 경우에는 반환 할 값이 없으므로 `Optional`을 반환하게 된다.

### reduce 활용: 개수 셈하기

`map`을 통해 각 요소들을 1로 매핑한뒤 `reduce`를 통해 누적하면 스트림 속 개수를 알 수 있다.

```java
Integer count = specialMenu.stream()
        .map(d -> 1)
        .reduce(0, (a, b) -> a + b);
System.out.println(specialMenu.size() == count);
```

```
true
```

### reduce 장점과 병렬화

사실 기존의 합계를 구하는 방법에서는 반복을 수행하면서 외부 변수 (sum과 같은)에 누적하는 방법이 있다.

위와같은 방법을 `가변 누적자 패턴`이라고 한다.

`가변 누적자 패턴`은 말에서도 알다싶히 누적자 변수가 반복 코드블럭 외부에 있고 변할 수 있다.

따라서 여러 스레드로 처리하기 위해서는 스레드간에 동기화를 이뤄야 하고, 이는 결국 동기화로 인해 큰 이득을 보기 힘들다.

하지만 reduce의 경우엔 내부반복이므로 `paralleStream`을 활용한 병렬 처리도 충분히 가능하다.

여기서 작업을 해야하는 요소들을 분할하고 충분히 분할 한 다음 작업후 조인하는 포크-조인 프레임워크를 사용하기 때문이다.

쉽게 병렬 처리가 가능하도록 도와주는 메서드이지만

병렬로 실행하기 위해서는 람다의 상태가 바뀌지 않아야 하며, 연산은 어떤 순서로 진행하더라도 동일한 결과를 뱉어야 한다.

### 스트림과 내부상태

Java Stream API에서 지원하는 API중 `map`, `filter`는 입력 스트림에서 각 요소를 받아서 결과를 출력 스트림으로 보낸다.

만약 사용자가 해당 메소드를 사용하면서, 인자로 넣은 람다나 메서드 참조가 내부적인 가변상태를 갖고 있지만 않는다면

위의 메소드는 보통 **내부상태가 없는 연산**라고 부른다.

하지만 `reduce`, `sum`, `max`와 같은 연산은 누적을 받을 내부 상태가 필요하다.

이 때 사용하는 내부상태는 요소수와 관계없이 크기가 **한정** 되어있다.

이말은 스트림 내에 개수가 몇개가 되었든, **결과는 단 하나의 변수**에 담기는 API이며,

**내부 상태의 크기가 고정**된 체로 작업되기 때문에 병렬 스트림에서도 문제없이 사용된다.

`filter`와 `map`은 `sorted`, `distinct`와는 달리 내부상태를 가지지 않는다.

`sorted`와 `distinct`가 내부상태를 갖는 이유는, 과거의 이력을 알고 있어야 하기 때문이다.

따라서 이런 연산을 **내부상태가 있는 연산** 이라고 한다.

하지만, `reduce`, `sum`, `max`와 다른점은 **모든 요소가 내부적으로 들어온 상태에서 정렬 혹은 중복제거**를 해야 한다.

그래서 스트림의 크기가 크거나 무한이면 문제가 발생할 수 있고, 연산의 결과가 일정치 않기 때문에

병렬 스트림에서 사용했을 때 큰 이득을 취하지 못한다.

## 스트림 만들기

지금까지 어떤 데이터 소스(I/O, 파일, 컬렉션)으로 부터 `stream` 메소드를 통해 스트림을 확보해서

다양한 질의 연산등을 처리할 수 있다.

그리고 `IntStream`의 정적 메소드 `rangeClosed` 등을 통해 특정한 숫자범위의 스트림을 생성할 수 있었다.

그밖에 무한 스트림 등의 다양한 스트림 생성방법을 알아본다.

### 값으로 스트림 만들기

`Stream.<T>of(T ...t)` 를 사용해서 스트림을 생성할 수 있다.

`Stream.<T>empty()`를 사용해서 비어있는 스트림을 생성할 수 있다.

### null 이 될 수 있는 객체로 스트림 만들기

자바 9에서 `null`이 될 수 있는 개체를 스트림으로 만들 수 있는 새로운 메소드가 추가되었다.

주로 `flatMap`등에서 사용하며, 어떤 메소드의 결과가 `null`이라면 `Stream.empty()`를 반환하도록 되어있다.

```java
public static<T> Stream<T> ofNullable(T t) {
        return t == null ? Stream.empty()
                : StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
}
```

### 파일로 스트림 만들기

파일 I/O의 연산에서 자바의 NIO 패키지를 많이 사용하는데,

여기서도 스트림을 사용하기 위해서 업데이트 되었다.

아래는 `File.lines` 정적 메소드를 사용하여서 스트림을 열고 한행씩 읽어오는 예시이다.

```java
// Stream은 AutoCloseable이 구현되어 있기에 try-with resources 사용가능

List<String> result = new ArrayList<>();
try(Stream<String> lines = Files.lines(Paths.get("src/com/khc/practice/modernjava/ch05/file.txt"), Charset.defaultCharset())){
        result = lines.toList();
}catch(IOException e){

}
System.out.println(result);
```

### 무한스트림(Unbounded stream): iterate

`Stream`의 정적 메소드 `iterate()`는 초기값과 함수형 인터페이스를 받아서 새로운 값을 끊임없이 생산할 수 있다.

```java
public static Stream<int []> fiboStream(int max){
        return Stream.<int []>iterate(new int[]{0, 1}, (int [] value) -> new int[]{ value[1], value[0] + value[1] })
                .limit(max);
}

public static void main(String [] args){
        Stream<int[]> fibo = fiboStream(20);
        List<Integer> fiboValues = fibo.map(x -> x[0]).toList();
        System.out.println(fiboValues);
}
```
```
[0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181]
```

자바 9에서는 `Predicate`를 지원하여 `limit`을 사용하지 않고서 제약을 걸 수 있다.

```java

public static IntStream getEven(int max){
        return IntStream.iterate(0, number -> number < max, number -> number + 2);
}

public static void main(String [] args){
        // JAVA 9에서 업데이트된 iterate에 Predicate 사용하기
        IntStream evenStream = getEven(100);
        int[] evenArr = evenStream.toArray();
        System.out.println(Arrays.toString(evenArr));

        // filter로 위의 기능을 구현하려 하면 문제가 된다.
        //        IntStream.iterate(0, number -> number + 2)
        //                .filter(number -> number < 100) // 여기서 중단시킬 수 없음
        //                .forEach(System.out::println);
        // 따라서 쇼트서킷을 지원하는 takeWhile같은걸 사용하는것이 좋음
}
```

### 무한스트림(Unbounded stream): generate

`Stream`의 정적 메소드 `generate`는 `iterate`와는 달리 생산된 각 값을 연속적으로 계산하지 않는다.



