### 범용 요약연산

`Collectors`의 정적 메소드 `reducing`을 활용하여 초기값을 주고 요약연산이 가능하다.

물론 `reduce` 메소드와 동일하게 초기값이 없는 버전도 가능하며

초기값이 없는 버전을 사용한 경우, 스트림 내부가 비었을수도 있기 때문에 `Optional`이 붙게 된다. 

다음은 하나의 인수를 가지는 `reduce`로서 최대값을 찾는 과정이다.

```java
List<Integer> numberList = asList(
    50, 11,119, 254, 9, 18, 33, 22, 98, 17
);

Optional<Integer> max = numberList.stream().collect(reducing((a, b) -> b - a));
```

### collect와 reduce

사실 메소드 스펙상으로 어떤 연산을 `collect`와  `reduce`로 둘다 구현이 가능하다.

하지만 실용적인 측면에서와 의미론적인 문제에서 두 기능은 차이가 있다.

`collect`는 의미상 도출하려는 결과를 누적하는 컨테이너를 바꾸는 용도로 설계 되었고

즉, 쉽게는 도출하려는 결과가 어떤 집계나 평균 같은 데이터일 때, 소스의 컬렉션 혹은 형식과 다른 데이터 형식 또는 컨테이너를 사용하여 결과를 만든다.

`reduce`는 두 값을 하나로 도출하는 불변형 연산이다.

즉, 누적기와 스트림 데이터를 갖고서 하나의 어떤 결과를 만들어내는 불변형 연산이다.

## 분할

분할은 분할 함수라 불리는 `Predicate`를 분류 함수로 사용하는 특수한 그룹화 기능이다.

결과적으로 그룹화 맵은 최대 두 개의 그룹으로 분류된다.

다음은 `menu` 리스트에서 채식이냐 채식이 아니냐로 나누는 분할 예시이다.

```java
Map<Boolean, List<Dish>> partitionedMenu = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian));
System.out.println(partitionedMenu);
```

## Collector 인터페이스 

`Collector` 인터페이스는 리듀싱 연산을 어떻게 구현할지 제공하는 메서드 집합으로 구성된다.

아래는 `Collector`인터페이스와 주요 메소드 로서

제네릭 T는 수집될 스트림 항목의 제네릭 형식

제네릭 A는 누적자, 즉 수집과정에서 중간결과를 누적하는 객체의 형식

제네릭 R은 수집 연산 결과의 객체형식을 의미한다.

```java
public interface Collector<T, A, R> {

    Supplier<A> supplier();

    BiConsumer<A, T> accumulator();

    BinaryOperator<A> combiner();

    Function<A, R> finisher();

    Set<Characteristics> characteristics();
}
```

`supplier`: 수집과정에서 빈 누적자 인스턴스를 만드는 파라미터가 없는 함수

즉, 새로운 결과 컨테이너를 생성하는 메소드이다.

```java
public Supplier<List<T>> supplier(){
    return () -> new ArrayList<T>();
}
```

`accumulator`: 리듀싱 연산을 수행하는 함수를 반환한다.

누적자(스트림 내 전까지 수집한 결과) 와 이번에 들어오는 N번째 요소를 `accumulator`를 통해 적용한다.

즉, 결과 컨테이너에 요소를 추가한다.

```java
public BiConsumer<List<T>, T> accumulator(){
    return (list, item) -> list.add(item);
}
```

위의 코드는 메소드 참조가 가능하므로
```java
public BiConsumer<List<T>, T> accumulator(){
    return List::add;
}
```

`finisher`: 스트림 객체 내 탐색을 끝내고, 누적자 객체를 최종 결과로 반환하면서 누적과정을 끝낼때 호출할 함수

즉, 최종 변환값을 결과 컨테이너로 적용하는것

`combiner`: 리듀싱 연산에서 사용할 함수를 반환하는 메소드

스트림의 서로 다른 서브파트를 병렬로 처리할 때 누적자가 이 결과를 어떻게 처리할 지 정한다.

예를들어 여러 흩어진 요소들을 하나의 `List`로 수집하는 `collect`를 병렬로 처리한다고 하면

그 일정 범위로 쪼개진 작업을 합칠때 어떻게 합칠건지 결정한다.

```java
public BinaryOperator<List<T>> combiner(){
    return (list1, list2) -> {
        list1.add(list2);
        return list1;
    }
}
```

`characteristics`: 컬렉터의 연산을 정의하는 `Characteristics`형식의 불변 집합을 반환한다.

스트림을 병렬로 리듀스 할 것인지, 그리고 병렬로 리듀스 한다면 어떤 최적화를 선택해야 할 지 힌트를 제공한다.

`Enum`이기 때문에 각 타입별로 어떤 종류가 있는지 살펴본다.

UNORDERED: 말 그대로 순서에 영향을 받지 않는다는 의미, 예를들어 집계나 합계 결과를 만드는 용도일때 사용

CONCURRENT: 다중 스레드에서 `accumulator`함수를 동시에 호출할 수 있으며, 병렬 리듀싱이 가능하다. 컬렉터의 플래그에 UNORDERED를 설정하지 않았다면, 정렬하지 않은 상황에서만 병렬 리듀싱이 가능하다.

IDENTITY-FINISH: `finisher` 메서드가 반환하는 함수는 단순히 `identity`를 적용할 뿐 이므로 이를 생략 할 수 있다. 즉, 리듀싱 과정의 최종 결과로 누적자 객체를 바로 사용이 가능하단 의미

### Collector 인터페이스 응용: 커스텀 Collector만들기 

위의 `Collector`를 구현하여 스트림 내에 요소들을 모아서 하나의 List로 수집하여 주는 `ToListCollector`를 하나 만들어본다.

```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {
    @Override
    public Supplier<List<T>> supplier() {
        // 누적자 초기화
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        // 누적하는 람다식
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        // 병렬 연산시 두개의 나눠진 스트림을 하나로 합침
        return (list, list2) -> {
            list.addAll(list2);
            return list;
        };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        // 최종 누적자가 최종결과이므로 항등함수를 반환
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(
                Characteristics.CONCURRENT, Characteristics.IDENTITY_FINISH
        ));
    }
}
```
```java
// 커스텀 컬렉션 ToListCollector 사용해보기: toList() 메소드 대신해서 쓸 수 있다.
Stream<Dish> menuStream = menu.stream();
List<Dish> dishes = menuStream.collect(new ToListCollector<>());
System.out.println(dishes);

Map<Dish.Type, List<Dish>> customListCollector = menu.stream()
        .collect(groupingBy(
                Dish::getType,
                filtering(dish -> dish.getCalories() > 500, new ToListCollector<>())
        ));
System.out.println(customListCollector);
```

### 커스텀 컬렉터를 구현해서 성능 개선하기

이전에 소수와 비소수를 나누는 것에 대해서 `partitioningBy`와 `collect`를 사용하였다.

```java
public static Map<Boolean, List<Integer>> partitionedPrime(int n){
    return IntStream.rangeClosed(2, n).boxed()
            .collect(partitioningBy(x -> isPrime(x)));
}

static boolean isPrime(int number){
    int candidate = (int) Math.sqrt((double) number);
    return IntStream.rangeClosed(2, candidate) // 2 ~ 해당숫자를 포함하는데 double -> int로 바뀌면서 포함 시켜야한다.
            .noneMatch(i -> number % i == 0);
}
```

`isPrime`메소드는 주어진 숫자의 제곱근까지 스트림을 열어서 나눠떨어지는 수가 존재하는지 검사하는데

여기서 최적화를 위해 지금까지 얻은 소수를 기준으로 나눠떨어지는지만 검사한다면 최적화를 할 수 있다.

즉, 중간결과 리스트(지금 찾으려는 수 보다 작은 수들 중 소수)를 `isPrime`이 받아서 그 소수로 나누어떨어지는지 검사하는 것이다.

이를 구현하기 위해서 `filter(x -> x < candidate)` 를 하는 것은 전체 스트림을 처리하고 난 후에 결과를 반환하게 된다.

즉, 중간 결과가 아닌 끝까지 돌고나서 필터링 거친 결과를 얻는것으로 효율이 안좋다.

어짜피 구한 소수는 정렬된 상태일 것이므로, `takeWhile`을 사용하는것도 좋아보인다.