### 자바의 형식추론 `type infer`

자바에서 기본적으로 변수나 메서드를 정의할 때 형식을 정해주어야 한다.

제네릭의 경우 컨텍스트로 형식을 유추할 수 있을 때에은 제네릭의 형식 파라미터를 생략할 수 있다.

```java
public class Practice {
    public static void main(String[] args) {

        Map<String, Integer> map = new HashMap<>();
        // JAVA 7 부터 가능
        // 여기서 new HashMap<String, Integer>을 하지않더라도 컴파일러가 문맥상으로 유추할 수 있다.

    }
}
```

Java 8에 추가된 람다 표현식에 경우에도, 형식이 생략되면 컴파일러가 형식을 추론한다.

```java
Function<Integer, Integer> func1 = i1 -> i1 + 20;
// JAVA 8에 추가된것으로, 문맥상 컴파일러가 파악할 수 있다면 파라미터의 형식을 생략할 수 있다.
```

특히 람다표현식의 경우, 만약 추론 할 함수형 인터페이스가 두개 이상이 포착되어 문제가 발생한 경우

임의의로 하나를 선택하여서 문제를 연기하는 것 보다 즉시 에러를 일으켜서 해결하는것이 바람직하다.

https://openjdk.org/jeps/286

https://developer.oracle.com/ko/learn/technical-articles/jdk-10-local-variable-type-inference

`지역 변수형 추론`은 Java 10에 추가된 것으로, `var`키워드로 변수를 선언하며 변수 오른쪽에 변수할당문을 통해 형식을 추론한다.

```java
static class Vehicle{
    void method(){
        System.out.println("Vehicle");
    }

}

static class Car extends Vehicle{
    void method(){
        System.out.println("Car");
    }
}
var t2 = new Car();
// t2 = new Vehicle(); error
```

위의 상황에서 t2를 `Car` 타입이라고 생각해야 하지, 다형성이 보장된다고 생각하여 `Vehicle`타입이라고 생각하면 안된다.

물론 아래와 같은 상황에서는 당연히 다형성이 적용된다. 사실 `var`키워드는 항상 할당문에 의해 타입이 정해지기 때문에, 다형성과 관계없이 규칙을 위반하긴 한다.

```java
var t2 = new Vehicle();
t2 = new Car();
```

이러한 `var`키워드에는 초기값이 없을때도 사용할 수 없으며, `null` 할당도 할 수 없다.

### 선언 사이트 변종

제네릭이 기본적으론 불변성이지만, 와일드카드를 통해 어느정도 해소시켰다. 와일드 카드는 제네릭이 있는 파라미터의 타입, 제네릭 클래스 타입등에서 사용할 수 있다.

그래서 상한 경계(`extends`)를 통해 공변성 (ex: 늑대는 개과 일 때, List<? extends 개> = List<늑대>) 을 어느정도 만족시켰고, 하한 경계(`super`)를 통해 반공변성(ex: 늑대는 개과 일 때, List<? super 늑대> = List<개>)도 어느정도 만족하였다.

여기서 이러한 와일드카드를 통해 제네릭에 유연함을 불어넣는것을 `사용 사이트 변종`이라고 한다.

이것말고도 다른언어에서 지원하는 `선언 사이트 변종`이 있는데, 이는 제네릭 클래스나 메소드를 선언할 때, 필요한 변성을 추가할 수 있다.
 
즉, 애초에 이 클래스는 본질적으로 `~변성`을 가집니다 라는것을 명세함으로서, 해당 제네릭클래스나 메소드 사용자 입장에서 "이런 목적으로 쓸거니까 <? super > 혹은 <? extends > 선언해야지"라는 생각을 하지않아도 된다.

```java
public static void main(String[] args) {
    List<Dog> dogList = new ArrayList<>();
    List<Wolf> wolfList = new ArrayList<>();

    Dog dog = new Wolf(); // 늑대가 곧 개일때

    // dogList = wolfList; // 공변성이 없음 -> 무공변성

    // wolfList = dogList; // 반 공변성도 기본적으로 없음

    List<? extends Dog> dogExtendsList = wolfList; // 최대 개까지 올 수 있어서, 상위타입에 하위타입 제네릭클래스를 할당할 수 있다. -> 공변성

    List<? super Wolf> dogSuperList = dogList; // 늑대가 곧 개일때, 하위타입 하한경계를 두어서 자신보다 상위의 제네릭 타입도 받을 수 있다. -> 반공변성
    
}

static class Wolf extends Dog{

}
static class Dog{

}
```

## 제네릭

### 구체화된 제네릭

자바 5에서 와일드카드와 함께 등장한 제네릭은 기존 JVM과의 호환성유지 때문에 컴파일시간에 타입검사가 이루어지고 난 뒤,

`ArrayList<Integer>`, `ArrayList<String>` 모두 런타임 때에는 제네릭이 사라져 같게 되었다. 이를 곧 `제네릭의 다형성 삭제 모델`이라고 한다.

이로인해 실제로 타입 안정성이 보장되어야 하는 부분에서는 런타임때 캐스팅이 되어져야 하므로, 자연스럽게 추가적인 런타임 비용을 발생시킨다.

그리고 `primitive-type`을 허용하지 않는데, 이는 런타임 시에 제네릭이 사라지고 그리고 값 13에 대해서 GC대상(Integer), GC 비대상(int)인지 구분할 수 없기 때문이다.

그래서 기본형인지 참조형인지에 대한 정보가 런타임에도 이어져야 한다. 이를 `다형성 구체화 모델`이라고 한다.









