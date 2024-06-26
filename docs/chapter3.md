## 람다 표현식

기존에 "동작 파라미터화"를 통해 동작을 외부에서 만들어서 메서드에 코드를 주입하는 방식으로 주입하였다.

하지만 동작을 추상화 하여 별도의 클래스로 동작을 다양한 형태로 정의하고 사용하거나 (전략 패턴)

다양한 동작의 형태를 별도의 클래스정의 없이 사용하였던 것도 결국 간결한 코드와는 거리가 있었다.(익명 클래스 사용)

이제 자바 8의 람다 표현식을 활용해서 이전의 "동작 파라미터화"를 좀더 간결한 코드로 변경한다.

## 람다란 무엇인가

람다 표현식은 메서드로 전달하려고 하는 익명 함수를 단순화한 것이다.

여기서 익명은 말그대로 보통의 메서드와 달리 이름이 없는것을 의미하는것

함수라고 표현하는 이유는 메서드는 특정 클래스에 소속되어 있진 않기 때문이다.

저기서 전달 부분은 메서드의 인자로 람다를 전달하거나 변수로 저장 할 수 있는점 (함수형 프로그래밍에서의 일급객체 특징)

람다 표현식의 형태는 다음과 같다.

```java
람다 파리미터    화살표     바디
(param1,  param2)   ->      {  }
```

람다 파라미터: 파라미터 리스트라고 불리며, 구현해야하는 메서드의 파라미터와 동일한 타입을 가진다.

화살표: 람다 파라미터와 람다 바디사이를 구분짓는 요소이다.

람다 바디: 람다의 반환값을 나타내며, 중괄호가 있을시에는 `return`을 명시해야 하고, 반환 타입은 구현하고자 하는 타겟의 반환타입과 일치한다.

```java
public interface ApplePredicate{
    public boolean test(Apple a1, Apple a2);
}

static void filter(List<Apple> inventory, ApplePredicate a){
    // 구현부
}
```

위의 `filter` 메소드는 `동작 파리미터화`를 통해 동작을 외부로 부터 주입 받도록 되어있다.

여기서 `동작`은 `ApplePredicate` 의 `test(Apple a1, Apple a2)` 메소드이고

해당 메소드를 기존의 익명 구현객체로 주입하는 것이 아닌, 람다 표현식으로 주입한다면

```java
filter(Arrays.asList( ... ), (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));
```

위와 같이 쓰일 것이고, 파라미터 리스트에 오는 타입이 구현해야 할 추상 메소드 `test`의 파라미터 타입과 동일하다.

람다 바디의 부분은 중괄호가 없으므로 표현식의 결과가 리턴값이 되고, 추상 메소드가 `boolean` 타입이므로 동일하다.

만약 추상 메소드의 리턴타입이 `void`의 경우엔 리턴값이 별도로 존재하지 않는다.

## 어디에 어떻게 람다를 사용하는가?

람다를 사용할 수 있었던 코드는 아래와 같았다.

```java
public static <T> List<T> filter(List<T> list, Predicate<T> p){
    List<T> result = new ArrayList<>();
    for(T t: list){
        p.test(t);
    }
    return result;
}

public static void main(String[] args) {
    List<Apple> list = Arrays.asList(
        new Apple(GREEN, 150),
        new Apple(RED, 160),
        new Apple(GREEN, 170)
    );

    System.out.println(FilteringApple.<Apple>filter(list, (apple) -> RED.equals(apple.getColor())));
}
```

여기에 결국 `Predicate<T>` 타입에 람다 표현식을 주입한 것이고

람다 표현식인 익명함수의 리턴 타입과 파라미터 타입이 `Predicate<T>`의 어떤 추상 메소드와 일치해야 할 것이다.

### 함수형 인터페이스

인터페이스는 여러 추상 메소드들을 정의하는데, 그 구현부를 다른 클래스에게 맡기는 역할이다.

즉, 메소드의 주요 형태인 리턴 타입과 파라미터의 개수와 타입 만을 결정시키고 내부 동작은 다른 클래스에서 구현한다.

그러한 인터페이스 중에서 추상 메소드의 개수가 **단 하나**일 경우, **함수형 인터페이스**라고 부른다.

```java
public interface Adder{
    int add(int a, int b);
}

public interface SmartAdder extends Adder{
    // 상속 받았으므로 int add(int a, int b); 의 추상메소드도 갖고있게 된다.
    int add(double a, double b);
}

public interface Nothing{
    // 아무런 추상 메소드가 없으면 함수형 인터페이스가 될 수 없다.
}
```

위의 세가지의 인터페이스 중 `Addr`인터페이스만이 함수형 인터페이스가 될 수 있다.

이러한 함수형 인터페이스가 파라미터로 정의되어 있는 메소드는

해당 파라미터에 람다 표현식을 넣을 수 있고, 이러한 **람다 표현식은 결국 함수형 인터페이스의 인스턴스** 라 할 수 있다.

### 함수 디스크립터

함수형 인터페이스의 추상 메서드 시그리처는 곧 람다 표현식의 시그니처를 가리킨다.

### 메소드 시그니처

https://www.thoughtco.com/method-signature-2034235

자바의 메소드 시그니처는 **메소드의 이름**과 **메개변수 리스트**를 의미한다.

```java
public void add(int a, int b);
```

위와 같은 메소드가 있을때, 메소드 시그니처를 얘기하라고 하면

`add` 와 `정수형 파라미터, 정수형 파라미터` 가 된다.

원래 메소드의 시그니처는 전통적으로 파라미터의 순서와 타입, 그리고 이름이 해당되지만

람다 표현식의 시그니처와 동일해야 한다는 의미에서는 메소드의 이름은 익명함수이기에 필요없어지고 리턴타입이 포함된다.

그래서 람다 표현식의 시그니처를 서술하는 메소드를 `함수 디스크립터` 라고 한다.

앞선 예제에서

```java
public interface Adder{
    int add(int a, int b);
}
```

에서 `Adder` 는 단 하나의 추상메서드를 가지므로 함수형 인터페이스이고,

함수형 인터페이스는 람다 표현식으로 표현이 가능하므로, `Adder` 인터페이스의 함수 디스크립터는

```
(int, int) -> int
```

가 된다.

## 람다 활용: 실행 어라운드 패턴

데이터 베이스의 연결과 해제와 같이 자원을 활용하고 필요한 구문을 실행한 후 반납하는 코드들이 있다.

보통 자원을 사용하기 위해서 준비하는 부분과, 실제 작업하는 부분 그리고 자원을 반납하거나 정리하는 코드의 형태를 띈다.

이렇게 진짜 **실행해야하는 구문이 설정과 정리로 둘러싸여진 형태**를 **실행 어라운드 패턴**이라고 부른다.

```java
public String processFile() throws IOException{
    try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))){
        return br.readLine(); // 실제 작업
    }
}
```

위의 형태가 실행 어라운드 패턴의 예시이고, 이를 람다를 활용하여 재사용성 높고 간결하게 바꿔보자.

### 동작의 파라미터화를 진행하자.

언제나 메소드의 내부 동작은 요구사항에 따라 변경이 일어날 수 있다.

위의 예시는 아직까지는 `data.txt`의 한줄을 읽고 반환하는 작업을 수행하지만

"2줄을 읽고 합쳐주는 메소드" 라던가 "읽은 내용 중 가장 반복되는 단어를 찾는 메소드" 등의 요구사항 변경이 일어날 수 있다.

결국 `processFile()` 메소드의 동작을 파라미터화 하여 다양한 구현을 상황에 맞게 람다 표현식으로 주입할 수 있다면

위의 요구사항 변경에도 유연하게 대처할 수 있다

만약 `processFile()` 가 동작의 파라미터화를 통해 구현을 외부로부터 주입 받을때,

"2줄을 읽고 합쳐주는 메소드"로의 변경사항이 생긴다면 아래와 같이 표현할 수 있다.

```java
processFile((BufferedReader br) -> br.readLine() + br.readLine())
```

### 함수형 인터페이스를 이용해서 동작 전달

위와 같은 형태가 되려면 우선 람다 시그니처를 추출하여 함수 디스크립터를 만들어야 한다.

람다 파라미터는 `BufferedReader` 타입의 변수, 람다 바디는 두 문자열을 합친 문자열을 반환한다.

즉, 반환타입이 `String`이어야 한다.

이러한 시그니처에 따른 함수 디스크립터는 다음과 같아진다.

```
(BufferedReader) -> String, throws IOException
```

기존의 전통적인 메소드 시그니처와 다르게 봐야하며,

기존에는 구현부 내에서 직접 `readLine()`을 호출함으로서

따라서 IOException을 던지는 것도 하나의 시그니처로 해석해야 한다.

이제 어떤 함수형 인터페이스를 만들어야 하는지 필요한 시그니처와 디스크립터는 구상했으므로 표현하자면 다음과 같다.

```java
public interface BufferedProcessor{
    public String process(BufferedReader br) throws IOException;
}
```

### 동작 실행

이제 기존의 동작을 파라미터화 시켰기에 파라미터로 넘어온 람다 표현식을 실행시키도록 `processFile` 메소드의 구현부를 변경한다.

```java
public String processFile(BufferedProcessor p) throws IOException{
    try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))){
        return p.process(br); // BufferedReader 타입의 변수가 필요하다.
    }
}
```

### 람다 전달

이제 완성되었으니 젤 처음 구현하고자 하였던 람다 표현식을 그대로 사용하면 된다.

## 함수형 인터페이스, 형식 추론

함수형 인터페이스의 추상메소드는 결국 람다 표현식의 시그니처와 일치해야하고

람다 표현식의 함수 디스크립터는 추상 메서드의 시그니처와 일치한다는 의미가 된다.

자바 API는 다양한 람다 표현식의 시그니처와 일치하는 다양한 함수형 인터페이스를 제공한다.

### 기본형 특화

자바 API에서 제공하는 다양한 함수형 인터페이스는 제네릭 타입을 포함하고

제네릭 타입은 레퍼런스 타입만이 올 수 있다.

이는 제네릭의 내부구현 때문이라고 하는데 (나중에 20장에서 다시 살펴본다고 하네요)

자바에서는 `Wrapper`를 통해 기본형을 참조형으로 나타낼 수 있다.

여기서 기본형의 값을 참조형으로 변환하는 것을 `박싱`이라고 하고 그 반대를 `언박싱`이라고 한다.

그리고 자바에서는 `오토 박싱`을 통해 자연스럽게 값을 `Wrapper` 타입과 기본타입에 대입할 수 있다.

하지만 이런 변환과정은 비용이 소모된다.

왜냐하면 `오토 박싱`이 일어나면서 참조형의 힙메모리속에 저장되고, 이를 참조하여 값을 들고올 때에는 결국 참조하는데 비용이 소모되게 된다.

이에 따라, 함수형 인터페이스 중에서는 기본타입을 제공하는 `IntPredicate`와 같은 함수형 인터페이스를 제공한다.

### 형식검사, 형식 추론, 제약

람다 표현식의 문맥을 사용해서 람다의 형식을 추론할 수 있다.

예를들어서 다음과 같은 두개의 함수형 인터페이스가 있다고 가정하자.

```java
interface Calculator{
    int add(int a, int b);
}

interface Player{
    String play(String name);
}
```

위 두 인터페이스는 각각 하나의 추상 메소드를 가지므로, 함수형 인터페이스로 볼 수 있다.

그리고 해당 함수형 인터페이스를 파라미터로 가져서 동작의 파라미터화를 수행하는 메소드가 있다.

```java
public static int doTest(int a, int b, Calculator calc){
    return calc.calc(a, b);
}
```

그리고 해당 메소드를 호출하는 문맥이 있다.

```java
System.out.println(doTest(2, 3, (a, b)-> a + b ));
```

여기서 람다 표현식의 형식을 분석하여 어떤 함수형 인터페이스를 기대하는지 찾게된다.

여기서 기대하는 형식을 대상 형식(타겟 타입)이라고 부른다.

위에서는 우선 `doTest`메소드의 선언부를 확인하여 파라미터를 확인하고

2번째 파라미터가 현재 `Calculator` 형식을 기대하고 있고, `Calculator`는 단 하나의 추상 메소드 `calc`를 갖고있다.

`calc`메소드의 함수 디스크립터를 살펴보면 `(int, int) -> int` 이고

주입되는 람다 표현식의 시그니처는 위의 함수 디스크립터와 일치해야 한다.

이 모든것이 일치해야 하는 이유는 같은 람다 표현식의 시그니처를 갖는 서로다른 함수형 인터페이스가 존재할 수 있기 때문이다.

```java
public interface Action{
    void run(); // 함수 디스크립터는 () -> void 로서 Runnable과 동일하게 된다.
}

public void execute(Action act){

}

public void execute(Runnable runnable){

}

execute(()-> System.out.println("hello")); // 람다 표현식을 주입시킬 수 있는 메소드가 명확하지 않다...
```

위의 두 `execute` 메소드는 서로 오버라이딩 형태이고, 둘 메소드의 파라미터는 동작의 파라미터화가 되어있는 형태이다.

그런데 메소드 두 형태가 모두 같은 함수 디스크립터를 가지는 서로다른 함수형 인터페이스로 정의되어 있다.

따라서 사용자 입장에서는 타겟 타입을 `Act`와 `Runnable`중 하나로 명시하기 위해 캐스팅을 해야한다.

```java
execute( (Act) ()-> System.out.println("hello"));
```

이와 마찬가지로 컴파일러는 람다 표현식의 타겟 타입을 통해 어떤 함수형 인터페이스의 추상 메소드인지 형식검사를 통해 알 수 있으니

람다의 시그니처에 람다 파라미터의 추론이 가능하다.

보통 람다 표현식을 사용할 때, 람다 파라미터의 타입을 명시하지 않는 경우가 대부분이다.

왜냐하면 타겟 타입을 통해 어떤 추상메소드인지 찾을 수 있고, 결국 함수 디스크립터를 알 수 있기 때문이다.

마지막으로 람다 표현식은 인수를 갖고 람다 바디에서 사용하였는데,

인수 뿐만아니라 자유변수(외부에 정의된 변수)도 바디에서 사용할 수 있다. 이와 같은 동작을 람다 캡쳐링 이라고 한다.

여기서, 캡쳐링할 수 있는 변수는 `final` 선언이 되어있거나 실질적으로 해당 제한자가 달려있는 변수여야 한다.

왜 이런 제약이 필요한걸까? 이는 클로저 개념과 관련이 있다.

클로저는 함수의 비 지역변수를 자유롭게 참조하는 것을 원칙으로 한다.

만약 지역변수에 대해 접근이 가능하다고 하고 람다가 별도의 스레드에서 실행된다고 가정해보자.

여기서 지역변수 `a`에 값 할당을 담당하는 스레드가 동작을 그만두게 되면

스택에 있는 `a` 변수의 값은 갑자기 할당 해제가 될 것이고, 이에 따라 순수함수로 구동되는 자바의 함수가 영향을 받게 될 것이다.

따라서 람다는 정의된 지역의 변수에 대해서 복사본을 갖게 되는 것이고, 이 복사본은 값이 바뀌지 않아야 한다.
