### 바이너리 호환성, 소스 호환성, 동작 호환성

뭔가를 바꾼 이후에도 에러 없이 기존 바이너리(마지막으로 컴파일했던 .class)가 실행될 수 있는 상황을 `바이너리 호환성`이라고 한다.

예시상황: 인터페이스에 메서드를 추가했을 때, 추가된 메서드를 호출하지 않는 한 문제가 일어나 지 않는다.

다음과 같은 세가지 `.java`가 있다.

```java
package com.khc.practice.modernjava.ch13.basic;

public interface A {
    int method();
}

public class AImpl implements A{
    @Override
    public int method() {
        return 0;
    }
}

public class Practice{
    public static void main(String[] args) {
        System.out.println(new AImpl().method());
    }
}
```

이들 모두 같은 패키지 속 서로다른 `.java`파일이다.

이들을 모두 컴파일하고 `Practice`를 실행시켜보면 아무 문제가 없다.
```
cd <java파일 경로>

javac *.java

// src폴더로 나와서
java com.khc.practice.modernjava.ch13.basic.Practice
```

여기서 바이너리 호환성이 챙겨진단것은 인터페이스에 메서드를 추가하고 새로 컴파일해서 올려도 

새로 추가된 메소드를 호출하지 않는이상 `Practice`에 영향을 주지 않는다는 것

```java
public interface A {
    int method();
    int addedMethod();
}
```
위와 같이 수정하고 

```
javac A.java

// src폴더로 나와서
java com.khc.practice.modernjava.ch13.basic.Practice
```
로 실행해도 값의 변동이 없다.

`소스 호환성`은 코드를 고쳐도 기존 프로그램을 성공적으로 재컴파일 할 수 있음을 의미한다.

위의 상황에서는 

```
javac *.java
```
를 수행하면 `AImpl.java`에서 문제가 발생한다.

```
AImpl.java:3: error: AImpl is not abstract and does not override abstract method addedMethod() in A
public class AImpl implements A{
```
`소스 호환성`이 지켜지지 않는다는걸 확인할 수 있다.

마지막으로 `동작 호환성`이란 코드를 바꾼 다음에도 같은 입력값이 주어지면 프로그램이 같은 동작을 실행한다는 의미이다.

예를들어 두 정수를 입력받아서 정수를 나누고, 정수중 하나라도 0이 들어오면 `ArithmeticException` 예외를 던지는 메소드가 있다고 가정하자.

```java
public class Cal {

    int divde(int a, int b) throws ArithmeticException{
        if(a == 0 || b == 0) throw new ArithmeticException();
        return 0;
    }
}
```
```java
package com.khc.practice.modernjava.ch13.basic;

public class Practice{
    public static void main(String[] args) {
        try{
            System.out.println(new Cal().divde(1, 0));    
        }catch(ArithmeticException e){
            System.out.println("Exception 발생");
        }
        
    }
}
```

위 메소드를 보유한 클래스를 컴파일하고 사용하는 다른 클래스도 컴파일 하고 실행하면 0이 들어가면 `Exception`을 던지는것에 큰 문제가 없다. 

하지만 위 메소드를 다음과 같이 수정하자.

```java
int divide(int a, int b){
    if(a == 0 || b == 0) return 0;
    return a / b;
}
```

위와같이 수정된 메소드를 갖고있는 클래스를 컴파일했을때 문제가 없다.

이는 `소스 호환성`이 지켜지고 있다. 그리고 메소드를 호출하고 있는 기존의 클래스를 실행해도 문제가 없다. 이는 `바이너리 호환성`에 해당한다.

하지만 이제 0을 집어넣었을 때 Exception이 아니라 0을 반환한다. 이는 `동작 호환성`을 깨트리고 있다.

### 옳지 못한 상속

상속으로 모든 재사용성을 해결할 수는 없다.

예를들어 100개의 메소드를 담고 있는 어떤 클래스 A에 있는 특정 '하나'의 메소드만 사용하려고 쓸데없는 99개의 메소드까지 상속받을 필요가 없다.

이런 상황일 때에는 오히려 `델리게이션`(위임)을 사용하여, 필요한 메소드들을 묶어놓은 클래스를 하나 만들어서 구현 혹은 동작을 위임시킨다.\

### 해석 규칙

다른 클래스나 인터페이스로 부터 같은 시그니처를 갖는 메서드를 상속받을 때는 세가지 규칙이 따른다.

1. 클래스가 항상 이긴다. 클래스나 슈퍼 클래스에서 정의한 메서드가 디폴트 메서드보다 우선권을 갖는다.

2. 1번 규칙 이외의 상황에서는 서브 인터페이스가 이긴다. 즉, 같은 메소드 시그니처를 갖는 A와 B에서 B가 A를 상속받는다면, B가 서브인터페이스로 B가 이긴다.

3. 여전히 디폴트 메소드의 우선순위가 결정되지 않았다면, 여러 인터페이스를 상속받는 클래스가 명식적으로 디폴트 메서드를 오버라이드하고 호출해야 한다.

3번에 상황에 해당하는 명시적인 문제해결을 살펴보면

아래와 같은 상황은 1, 2으로 전혀 구분지을 수 없다.

```java
public interface B {
    default int method(){
        return 123;
    }
}
public interface A {
    default int method(){
        return 456;
    }
}
public class C implements A, B{
    public static void main(String[] args) {
        System.out.println(new C().method());
    }
}
```

```
types com.khc.practice.modernjava.ch13.implicity.A and com.khc.practice.modernjava.ch13.implicity.B are incompatible
```
따라서 C에서 오버라이드 한 다음, 어떤 슈퍼타입의 메소드를 호출할지 결정해야 한다.

```java
public class C implements A, B{
    public static void main(String[] args) {
        System.out.println(new C().method());
    }

    @Override
    public int method() {
        return A.super.method(); // X.super.method() 꼴로 사용하며, X는 method를 갖고있는 슈퍼 인터페이스이다.
        // 위와같은 상황에서는 선택이 가능하다.
    }
}
```

### 다이아몬드 문제에서의 또다른 문제

```java
public interface A {
    default int method(){
        return 123;
    }
}
public interface B extends A{
}
public interface C extends A{
}
public class D implements B, C{
    public static void main(String[] args) {
        System.out.println(new D().method());
    }
}
```

위와같은 상황을 다이어그램으로 표현하면 다이아몬드 같다고 해서 다이아몬드 문제라고 한다.

컴파일러는 선택할 수 있는 메소드가 어짜피 A인터페이스 디폴트 메소드 뿐이므로, 헷갈릴게 없이 정상적으로 실행된다.

여기서 B나 C 둘중 하나에 동일한 시그니처의 디폴트 메소드가 존재한다면, 2번규칙에 의거하여 최하위 인터페이스의 디폴트 메소드를 가져온다.

그런데 B와 C 모두가 동일한 시그니처의 디폴트 메소드가 존재한다면, 이는 D에서 결정지어야 한다.


```java
public interface B extends A{
    default int method(){
        return 456;
    }
}

public class D implements B, C{
    public static void main(String[] args) {
        System.out.println(new D().method()); // 출력: 456 / 2번 규칙
    }
}
```

마찬가지로 C나 B중에 동일한 시그니처의 추상 메소드가 존재한다면, 추상메소드가 우선이므로, C의 추상메소드와 B의 A로부터 받은 디폴트 메소드와 경쟁하는데

이렇게 같은 시그니처의 추상메소드 vs 디폴트 메소드도 또한 3번 규칙에 의거하여 구현하는 클래스에서 결정지어야 한다.

여기서 중요한 것은, B는 여전히 추상메소드 이므로 `B.super.method()`는 B의 추상메소드를 호출하려 하는것으로 에러가 발생한다.

따라서 아에 오버라이딩을 통해 재정의 해야한다. 재정의 한다면 1번 규칙에 의거하여 우선순위를 가진다.

```java
public interface A {
    default int method(){
        return 123;
    }
}
public interface B extends A{
    int method();
}
public interface C extends A{
}


public class D implements B, C{
    public static void main(String[] args) {
        System.out.println(new D().method());
    }

    public int method(){
        // 아에 새롭게 정의해야함 선택할 수 있는게 아님
        return 0;
    }
}
```