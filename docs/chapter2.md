## 동작의 파라미터화

아직은 어떻게 실행할 것인지 결정하지 않은 코드 블록을 의미한다.

동작의 파라미터화를 사용하면 자주 바뀌는 요구사항에 효과적으로 대응할 수 있게 도와준다.

## Filtering Apple

요구사항에 맞게 변화하는 필터를 만들어야 한다.

여기서 사과의 어트리뷰트를 기준으로 필터를 만들다 보면 확장성이 제한적이게 된다.

특히, 당시 시점의 사과 어트리뷰트를 통해 모든 가능한 경우의수를 하나의 함수에 표현 하려고 할 수 있을것이다.

```java
public static List<Apple> filterApples(List<Apple> inventory, Color color, int weight, boolean flag){
        // weight 무게 고려, flag 는 true: 색상을 고려, false: 무게를 고려
        List<Apple> result = new ArrayList<>();
        for(Apple apple: inventory){
            if((flag && apple.getColor().equals(color)) ||
                (!flag && apple.getWeight() > weight)
            ){
                result.add(apple);
            }
        }
        return result;
    }
```

근데 저 함수를 사용하는 입장에서는

함수의 파라미터로 어떤값을 넣어야하고 왜 저게 필요한지 유추하기 힘들다.

```java
System.out.println(filterApples(list, GREEN, 0, true));
System.out.println(filterApples(list, null, 150, false));
```

그래서 함수(동작) 자체를 외부에서 주입할 수 있도록 동작을 추상화 하자.

우선 필터링을 한다는 의미를 가지는 인터페이스를 하나 만들 수 있다.

```java
public interface ApplePredicate {
    boolean test(Apple apple);
}
```

위의 인터페이스의 서로다른 구현체를 만들어서 다양한 필터를 구현할 수 있다.

```java
public class AppleHeavyWeightPredicate implements ApplePredicate{
    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
}

public class AppleGreenColorPredicate implements ApplePredicate{

    @Override
    public boolean test(Apple apple) {
        return apple.getColor().equals(Color.GREEN);
    }
}
```

이제 사용자는 상황에 맞게 어떤 필터링을 선택할지를 결정하여 인자로 넘기면 된다.

```java
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p){
    List<Apple> result = new ArrayList<>();
    for(Apple apple: inventory){
        if(p.test(apple)){
            result.add(apple);
        }
    }
    return result;
}

System.out.println(filterApples(list, new AppleGreenColorPredicate()));
System.out.println(filterApples(list, new AppleHeavyWeightPredicate()));
```

여기서 필터링을 실질적으로 수행하는것은 test 메소드인데, 그 메소드를 구현하기 위해서 클래스를 새롭게 선언하는것이 많이 번거롭다.

따라서 익명 클래스를 사용해서 필터링의 핵심 로직인 test 메소드 구현에 신경쓰고 새로운 클래스의 창조에는 신경을 덜쓸 수 있다.

```java
System.out.println(filterApples(list, new ApplePredicate() {
    public boolean test(Apple apple) {
        return RED.equals(apple.getColor());
    }
}));
```

하지만 위의 경우에도 사실 실질적인 기능을 수행하는 test() 메소드 외에 너무많은 코드들이 공간을 차지하고 있다.

마지막으로 다음장에서 배우는 함수형 인터페이스의 특징인 람다 표현식을 사용하여 코드를 간결화 할 수 있다.

```java
System.out.println(filterApples(list, (apple) -> RED.equals(apple.getColor())));
```

또한 filter의 기능에 주목해보면, 우리가 메소드로 넘긴 코드를 기반으로 전체 요소에서 조건에 부합하는 요소들을 따로 `List<T>` 자료구조로 모으는 것을 확인할 수 있다.

여기서 필요한 타입들을 제네릭으로 선언하여 재사용성이 높은 함수를 만들 수 있다.

```java
public static <T> List<T> filter(List<T> list, Predicate<T> p){
    List<T> result = new ArrayList<>();
    for(T t: list){
        p.test(t);
    }
    return result;
}
```
