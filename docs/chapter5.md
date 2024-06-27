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

병렬로 실행하기 위해서는 람다의 상태가 바뀌지 않고서, 연산은 어떤 순서로 진행하더라도 동일한 결과를 뱉어야 한다.(순수 함수)
