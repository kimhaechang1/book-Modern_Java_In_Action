## 스트림 소개

스트림은 자바 8 API에 새로 추가된 기능으로

선언형으로 컬렉션 데이터를 처리할 수 있다.

또한 스트림을 사용하면 멀티스레드 코드를 구현하지 않아도 데이터를 투명하게 병렬로 처리할 수 있다.

기존의 메뉴속 요리를 순회하면서 400 칼로리 미만의 메뉴를 선정하고

선정된 메뉴를 칼로리에 따라 정렬 한 후 그 메뉴들의 이름 리스트를 뽑는 자바 7까지의 코드이다.

```java
List<Dish> menu = new ArrayList<>();
List<Dish> lowCaloricDishes = new ArrayList<>();
for(Dish dish: menu){
    if(dish.getCalories() < 400){
        lowCaloricDishes.add(dish);
    }
}

Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
    @Override
    public int compare(Dish o1, Dish o2) {
        return Integer.compare(o1.getCalories(), o2.getCalories());
    }
});

List<String> lowCaloricDishesName = new ArrayList<>();
for(Dish dish: lowCaloricDishes){
    lowCaloricDishesName.add(dish.getName());
}
```

여기서 `lowCaloricDishes`라는 중간 집계용 변수를 사용했다.

이거를 자바 8의 Stream API를 사용하면 다음과 같이 달라질 수 있다.

```java
lowCaloricDishesName = menu.stream()
        .filter((dish -> dish.getCalories() < 400)) // 400 미만의 칼로리를 가진 요소들로
        .sorted(Comparator.comparing(Dish::getCalories)) // 정렬하고
        .map(Dish::getName) // 이름으로 이루어진 String 스트림을 열고
        .collect(Collectors.toList()); // 마지막으로 List로 모아라
```

여기서 어떤 필터의 동작이라던지, 매핑의 동작이라던지 정렬의 동작을 전부 외부로 부터 주입받고 있다.

`filter`, `sorted`, `map`, `collect`와 같은 빌딩 블록 연산을 연결하여 복잡한 데이터 처리 파이프라인을 만들 수 있다.

빌딩 블록이란 스트림을 구성하고 조작하는데 사용되는 기본 구성 요소(스트림 생성, 중간 연산, 최종 연산)를 의미한다.

이렇게 여러 연산을 하나의 파이프라인으로 연결하더라도 가독성과 명확성이 유지된다.

특히 위의 4가가지 빌딩 블록은 고수준의 빌딩 블록으로 이루어져있으며,

특정 스레딩 모델에 제한되지 않고 자유롭게 어떤 상황에서든 사용할 수 있다.

결과적으로 자바 8의 스트림 API의 특징은

'무엇을 해야하는지에 특화된' `선언형`,

'다른 함수들과 이어 붙이는' `조립성`,

'`paralleStream`을 활용한' `병렬화`를 뽑을 수 있다.

### 스트림 시작하기

자바 8 컬렉션에는 스트림을 반환하는 `stream` 메서드가 추가되었다.

이러한 `stream` 메서드는 숫자 범위나 I/O 자원으로부터 등 다양한 방법으로 스트림을 얻을 수 있다.

```java
default Stream<E> stream() {
    return StreamSupport.stream(spliterator(), false);
}
default Stream<E> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
}
```

스트림의 정의는 "데이터 처리 연산을 지원하도록 소스에서 추출된 연속된 요소" 로 정의할 수 있다.

여기서 각각 낱말풀이를 해보면

- 연속된 요소: 컬렉션과 마찬가지로 스트림도 연속된 값 집합의 인터페이스를 제공한다.

  여기서 컬렉션은 자료구조의 특성을 가지기에 시 공간적 복잡성고 관련된 요소 저장및 접근이 주 연산이지만

  스트림의 경우 **표현 계산식**이 주 연산을 이룬다.
  즉, 컬렉션의 주제는 데이터이고, **스트림의 주제는 계산**이다.

- 소스: 스트림은 컬렉션, 배열, I/O 자원등의 데이터 제공 소스로 부터 데이터를 소비한다.

- 데이터 처리 연산: 스트림은 함수형 프로그래밍 언어에서 일반적으로 지원하는 연산과 DB와 비슷한 연산을 지원한다.

결국 스트림은 "DB나 함수형 프로그래밍에서 지원하는 연산과 비슷한 연산을 지원하도록 컬렉션/배열/IO 자원등으로부터 추출한 연속된 값"이라고 볼 수 있다.

또한 스트림에는 두가지 중요한 특징이 있다.

- 파이프 라이닝: 대부분의 스트림연산은 서로의 연산을 연결해서 커다란 파이프 라인을 구성할 수 있도록 스트림 자신을 반환한다. 이로인해 `레이지`, `쇼트서킷`과 같은 최적화를 얻을 수 있다.

- 내부반복: 컬렉션처럼 외부반복을 쓰는것이 아닌 내부반복을 지원한다.

### 스트림과 컬렉션

스트림과 컬렉션 모두 연속된 요소 값을 저장하는 자료구조의 인터페이스를 제공한다.

여기서 `연속된`은 순차적으로 값에 접근한다는 것을 의미한다.

스트림과 컬렉션의 가장 큰 차이는 "데이터를 언제 계산하느냐" 이다.

컬렉션은 현재 자료구조가 포함하는 **모든** 값을 메모리에 저장하는 자료구조이다.

즉, 컬렉션은 어떤 추가나 삭제 연산이 이루어질때 마다 메모리에 저장해야하는 연산이 필요하고,

추가하려는 요소는 미리 계산되어야 한다.

하지만 스트림의 경우 **요청할 때만 요소를 계산**하는 고정된 자료구조이다.

즉, 사용자가 요청하는 값만을 스트림내에서 추출하고 `레이지` 특성에 의해 요청할 때에만 계산한다.

결국 컬렉션은 생산자 중심으로, 연산이 필요할 때 연산의 끝까지 생성하여 어떤 컬렉션 속에 담아야 한다면

스트림은 생산자와 소비자 관계로서 생산하면 다른 블록에서 소비하고, 마지막에 최종결과에 대한 요청이 있을때만 작동한다.

### 딱 한번만 탐색할 수 있다.

스트림도 반복자와 마찬가지로 단 한번만 탐색할 수 있다.

다시 탐색하고 싶다면 초기 데이터 소스에 새로운 스트림을 열어야 한다.

```java
// 스트림은 한번만 탐색할 수 있다.
List<String> title = Arrays.asList("java8", "in", "action");
Stream<String> s = title.stream();
s.forEach(System.out::println);
// s.forEach(System.out::println); // stream has already been operated upon or closed

Iterator<String> iter = title.iterator();
// 두번 돌리고싶다.
while(iter.hasNext()){
    System.out.println(iter.next());
}
// 이미 해당 반복자로 끝까지 탐색했으므로 두번 반복이 안된다.
while(iter.hasNext()){
    System.out.println(iter.next());
}
```

### 내부 반복이 가지는 장점

외부반복을 사용해서 컬렉션을 순회한다면

반복자에게 다음 요소가 있는지 물어보고, 있다면 다음 요소를 들고와서 프로그래머가 필요한 연산을 하고

그다음 다시 다음요소가 있는지 물어보고... 이 과정이 반복된다.

하지만 스트림을 사용하면 병렬적으로 처리할 수 있거나, 최적화된 다양한 순서로 처리할 수 있다.

그리고 프로그래머가 해당 연산에서 어떤 동작을 해야하는지 인자로 넘겨주면 된다.

### 중간 연산

중간연산은 다른 스트림을 반환하는 연산이다.

중간연산의 가장 큰 특징은 스트림의 특징인 레이지와 관련있는 것으로 종단연산 호출이 이뤄지기 전까지는 연산을 수행하지 않는다는 점이다.

이렇게 **나중에** 연산하는 `레이지` 특성 덕분에 `쇼트서킷`과 `루프퓨전`의 최적화를 이룰 수 있다.

```java
menu.stream()
    .filter(dish -> {
        System.out.println("filtering: "+dish.getName());
        return dish.getCalories() > 300;
    })
    .map(dish -> {
        System.out.println("mapping: "+dish.getName());
        return dish.getName();
    })
    .limit(3)
    .collect(Collectors.toList());
```

```
filtering: pork
mapping: pork
filtering: beef
mapping: beef
filtering: chicken
mapping: chicken
```

`filter`를 하는 동안에는 사실 모든 요소가 거치고 `map`할 때 `filter`를 거친 스트림 속 연속된 값이 남아있고

`map` 할때도 `filter`의 조건에 부합하는 요소들이 나오지 않을까 생각했지만

하지만 `filter`동안에도 이미 `limit`까지 적용된 3개만 추출되어 작동한다.

이를 `쇼트 서킷`이라고 한다. (자세한 이야기는 5장에서)

또한 `filter`와 `map`는 서로다른 연산이지만 한 과정으로 병합되었다.

이 기법을 `루프 퓨전`이라고 한다.
