## `Optional` 클래스 소개

`Optional`은 `java.util.Optional<T>`로서 선택형 값을 캡슐화 하는 클래스이다.

값이 있으면 값을 감싸고, 값이 없으면 `Optional.empty()` 메서드로 `Optional`을 반환한다.

`Optional.empty()`는 정적 팩토리 메소드로서 싱글톤을 반환한다.

기존에 `null`이 나오는상황을 가만히 두면 `if`로 `null` 체크를 해야하는건지 없으면 버그인걸로 간주해서 고쳐야하는건지 구분이 안되었지만,

`Optional`을 사용함으로서, 반드시 해결해야하는 문제인지 아니면 값이 없을수도 있는것도 버그가 아닌지에 대한 명확한 정보를 제공할 수 있다.

```java
public static class Person{

    private Optional<Car> car; 
    // 사람이 자동차를 소유하지 않았을수도 있다.
    public Optional<Car> getCar(){
        return car;
    }
}
public static class Car{

    private Optional<Insurance> insurance;
    // 해당 자동차가 보험이 없을수도 있다.
    public Optional<Insurance> getInsurance(){
        return insurance;
    }
}
public static class Insurance{

    private String name;
    // 보험의 이름은 없으면 버그로 간주한다.
    public String getName() {
        return name;
    }
}
```

### `Optional` 객체 만들기

빈 `Optional`의 경우 `Optional.empty()`로 만들 수 있다.

값을 넣어놓은 `Optional`의 경우 `Optional<T> optional = Optional.of(T t)`로 넣은체로 시작할 수 있다.

넣을려는 값이 `null`이 될수도 있는 경우 `Optional<T> optional = Optional.ofNullable(T t)`로 넣을 수 있다.

### `flatMap`을 통한 `Optional`객체 연결

여러 `Optional` 변수들끼리 서로 연결되어있고 메소드를 호출하여 참조에 참조를 거쳐야할 때

단순 `map`을 사용하면, `Optional<Optional<Optional<T>>>`로 되어버릴 것이다.

왜냐하면 `map`은 `Optional` 변수에 대해서, 인자로 넘긴 동작의 결과가 존재한다면 `Optional`로 감싸서 반환하기 때문이다.

`flatMap`은 중간 결과들에 대해서 값이 있으면 꺼내서 다음의 메소드 호출로 이어지게 도와준다.

```java
person.getCar().getInsurance().getName();
```

위의 코드는 `Optional`을 사용하기 전에 메소드를 연쇄적으로 호출하는 행동이다.

여기서 `getCar()`와 `getInsurance()`가 `Optional`을 반환하도록 바뀌었기에 `flatMap`을 사용해서 위의 코드와 동일하게 만들 수 있다.

```java
public static String checkedGetCarInsuranceName(Optional<Person> p){
//        before optional
//        if(p != null){
//            Car car = p.getCar();
//            if(car != null){
//                Insurance insurance = car.getInsurance();
//                if(insurance != null){
//                    return insurance.getName();
//                }
//            }
//        }
//        return "Unknown"

//      after optional
    return p.flatMap(Person::getCar) 
            // Optional<Car> 반환
            .flatMap(Car::getInsurance) 
            // Optional<Insurance> 반환
            .map(Insurance::getName) 
            // map을 사용한 이유는 flatMap은 함수형인터페이스의 반환값이 Optional 타입이어야 하기에 map을 사용해야함
            .orElse("Unknown"); 
            // Optional<String>이 아니라 기본값으로 Unknown을 제공함으로서 Optional을 벗길 수 있음
}
```

## Java 9에 추가된 `Optional` 스트림 조작

`Optional` 데이터를 포함하고 있는 객체들을 `Stream`으로 조작할 때에는

현재 `Stream`내부가 `Optional`이 있을수 있는지 없는지를 염두해두고 체이닝을 들어가야 한다.

```java
public static Set<String> getCarInsuranceNames(List<Person> persons){
    return persons.stream()
            // Person 스트림 열기
            .map(Person::getCar)
            // Stream의 map 함수로서 하나의 Optional<Car>들로 구성된 Stream 형성 -> Stream<Optional<Car>>
            .map(optCar -> optCar.flatMap(Car::getInsurance))
            // Stream의 요소로 Optional<Car>를 하나씩 내면서 인자로 넘겨진 함수형 인터페이스의 결과 타입을 가지는 Optional로 변환한다. -> Stream<Optional<Insurance>>
            .map(optIns -> optIns.map(Insurance::getName))
            // Stream의 요소로 Optional<Insurance>를 하나씩 꺼내면서 함수형 인터페이스의 결과값을 가지는 Optional로 변환한다. 여기서도 인터페이스의 반환타입이 Optional이 아니기에 flatMap이 아니다.
            // -> Stream<Optional<String>>
            .flatMap(Optional::stream)
            // 마지막으로 Stream내부에 바로 값에 접근이 안되기때문에, 각 Stream 요소들을 Stream으로 반환하여 하나의 Stream내부로 모아주는 Stream flatMap을 사용한다.
            .collect(Collectors.toSet());
}
```

### 두 `Optional` 합치기

`Person`클래스와 `Car`정보를 활용하여 가장 저렴한 보험료를 제공하는 외부 서비스가 있다고 가정하자.

```java
public static Insurance findCheapestInsurance(Person person, Car car){
    return new Insurance();
}

public static Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car){
//        if(person.isPresent() && car.isPresent()){
//            return Optional.of(findCheapestInsurance(person.get(), car.get()));
        // 또다시 null 체크를 장황하게 하고있다.
//        }else{
//            return Optional.empty();
//        }

    return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c)));
    // 핵심은 map과 flatMap을 활용해서 안쪽 연산에서부터 안전하게 연산을 수행한다.
    // 안쪽은 findCheapestInsurance() 메소드를 활용해야 하므로 인터페이스의 반환타입이 Optional이 아닐때 사용하는 map을 사용한다.
    // 바깥쪽 입장에서는 안쪽의 반환값으로 인해 바깥쪽 둘러싸고 있는 람다표현식의 반환타입이 Optional이 되기에 flatMap을 사용한다.
}
```

### `filter`로 특정 값 거르기

`Optional` 인스턴스의 `filter`메소드를 사용하면 `Predicate`의 `true`에 해당하면 값을 반환하고

`false`라면 `Optional.empty()`를 반환한다.

다음은 `Optional<Person> person` 과 `minAge`를 받아서 `minAge`이상일 때에만 가입해놓은 보험회사 이름을 반환하는 메소드이다.

```java
public String getCarInsuranceName(Optional<Person> person, int minAge){
    return person.filter(p -> p.getAge() >= minAge)
            // filter를 통해 일단 값이 조건에 맞는지 검사한다.
            // 만약 person이 empty라면 자기자신을 반환하고, Perdicate가 false면 empty를 반환한다.
            .flatMap(Person::getCar)
            .flatMap(Car::getInsurance)
            .map(Insurance::getName)
            .orElse("Unknown");
}
```

### 예외를 `Optional`로 감싸기

기존에 `NullPointerException`과 같은 `RuntimeException`의 경우 `if`로 체크할 수 없던것은 `try-catch`를 활용하였다.

Java API의 메소드를 변경할 수 없을때에는 따로 사용할 `Optional` 유틸리티 클래스를 만들어서 

`Exception`이 발생하면 `Optional.empty()`를 반환하도록 만들어서 사용하자.

```java
public Optional<Integer> optionalParser(String numStr){
    try{
        return Optional.of(Integer.parseInt(numStr));
    }catch(NumberFormatException e){
        return Optional.empty();
    }
    // 내부에서 try-catch를 사용해서 Optional을 반환하기에 
    // 사용자 입장에서 try-catch로 장황한 코드를 만들필요가 줄어든다.
}
```