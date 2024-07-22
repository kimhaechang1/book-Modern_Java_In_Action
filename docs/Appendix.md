## 어노테이션 반복

이전 자바에서는 선어네서 지정한 하나의 어노테이션만 사용했다.

즉, 다음의 코드는 유효하지 않다.

```java
@Target(ElementType.TYPE)
// Target이 ElementType.TYPE이기에 record, enum, class, interface에 사용이 가능하다.
public @interface Author {
    String name();
}
@Author(name= "김")
@Author(name= "회")
@Author(name= "창")

// 위와같이 클래스에 사용가능하기에 사용하였지만 같은 어노테이션을 반복적용할 수 없기에 Error가 발생한다.
public class Practice {

    public static void main(String[] args) {

    }
}
```

당시에는 이런 문제를 해결하기 위해, 중복하여 선언하고싶은 어노테이션을 배열로 갖는 `~s` 어노테이션을 만들어서 사용하였다.

```java
@Authors(
        { @Author(name = "김"), @Author(name = "회"), @Author(name = "창") }
)
public class Practice {

    public static void main(String[] args) {
         Authors authors = Practice.class.getDeclaredAnnotation(Authors.class);
         Arrays.stream(authors.value()).forEach(anno -> System.out.println(anno.name()));
         // 김
         // 회
         // 창
    }
}
```
코드가 상당히 장황해졌기에, Java 8에서는 중복제한을 해제하였다. 

대신에 중복하여 반복해야하는 순간이 있다면 **반복가능한 어노테이션임을 정의에 명시**해야한다.

반복 가능함을 명시하는 방법으로 `@Repeatable` 어노테이션을 선언하면 된다.

이 때 마치 "이 어노테이션을 반복하여서 쓰는데 사용하는 정의가 B입니다." 라는것을 명시해주는 클래스를 `@Repeatable` 어노테이션에 명시해주어야 한다.

즉, 위의 상황에서 `@Author`입장에서 `@Authors` 어노테이션이 `@Author`반복에 사용되는 어노테이션 이므로, `@Repeatable(Authors.class)` 가 된다.

여기서 중요한 것은 피 대상타입의 유지정책은 반복대상 타입의 유지정책 범위와 같거나 더 작은 범위여야 한다.

```
Author 과 Authors에서 Authors가 같거나 더 큰 유지정책이 적용되어 있어야 한다.
```

```java

@Repeatable(Authors.class)
public @interface Author {
    String name();
}

@Retention(RetentionPolicy.RUNTIME) // 무조건 필요함
public @interface Authors{
    Author[] value();
}
```
실제로 사용할때에는 다음과 같이 쭉 나열하면 된다.

```java
@Author(name = "김")
@Author(name = "회")
@Author(name = "창")
public class Practice {

    public static void main(String[] args) {
        Authors authors = Practice.class.getDeclaredAnnotation(Authors.class);
        Arrays.stream(authors.value()).forEach(anno -> System.out.println(anno.name()));
    }
}
```
이걸 컴파일한 결과를 보면, 컨테이너 어노테이션에 명시적으로 반복하려는 어노테이션 배열을 대입하는 모습을 볼 수 있다.

```java
import java.util.Arrays;

@Authors({@Author(
    name = "김"
), @Author(
    name = "회"
), @Author(
    name = "창"
)})
public class Practice {
    public Practice() {
    }

    public static void main(String[] args) {
        Authors authors = (Authors)Practice.class.getDeclaredAnnotation(Authors.class);
        Arrays.stream(authors.value()).forEach((anno) -> {
            System.out.println(anno.name());
        });
    }
}
```

결국 `@Repeatable`어노테이션은 개발자의 코드 복잡성을 줄이기위함에 젤 큰 목적이다.


## 아토믹

`java.util.concurrent.atomic`은 단일 변수에 원자연산을 지원하는 숫자클래스를 제공한다.

우선 원자적이란 것은 더이상 쪼갤수 없는 걸 의미한다.

그리고 원자적 연산이라는 것은 더이상 나눌 수 없으며, 한번 실행되면 실행이 되던가, 안되던가로 나뉘는 것을 의미한다.

즉, 중간에 멈추거나 오류로 인해 다른 상황이 발생하지 않는다.

해당 숫자클래스들은 `CAS(compare and swap)`를 통해 원자적 연산을 만족시킬 수 있다.

추가적으로 여러 스레드에서 읽기보다 갱신동작이 더많이 수행된다면 `Atomic` 클래스 대신 `~Adder`나 `~Accumulator`를 사용하라고 권장한다.

https://hojoon7807.github.io/adder-accumulator/

즉, 위의 블로그 결론은 엄청나게 많은 스레드가 갱신을 하려고 한다면, 자연스럽게 레이스컨디션도 자주 발생할 것이고, 이를 해결하기위한 방식에 있어서 `~Adder`와 `~Accumulator`가 빨라진다는거

하지만, 셀을 늘리는 방식으로 경합을 줄이기 때문에, 어쨋든 읽기 동작에 있어서는 셀크기만큼 반복하여 누적해야 할 것이기에 더 느리다는것을 인지하면 된다.


### Arrays parallelSort vs Sort

`Arrays.parallelSort`는 포크-조인풀로 정렬을 수행한다.

따라서 연산을 수행하는 컴퓨터의 사양에 따라 결과가 달라질 수 있다.

여러 길이들로 배열에 랜덤한 값을 부여하여 태스트를 해보면 대략 현재 사양에 어디쯤 부터 병렬연산이 이득인지 알 수 있다.

```java
public class Practice {
    static final int MAX_LENGTH = 10_000_000;
    static int [] array = new int[MAX_LENGTH];
    static int [] array2 = new int[MAX_LENGTH];
    static final int MAX_VALUE = 5_000;
    static final Random random =  new Random();
    public static void main(String[] args) {
        for(int i = 0;i<MAX_LENGTH;i++){
            array[i] = random.nextInt(MAX_VALUE);
        }
        array2 = Arrays.copyOf(array, array.length);

        long startTime = System.nanoTime();
        Arrays.parallelSort(array);
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1_000_000 +" ms");

        startTime = System.nanoTime();
        Arrays.sort(array2);
        endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1_000_000 +" ms");
    }
}
```
```
5만개 데이터
parallelSort:   24ms
sort:           8ms

50만개 데이터
parallelSort:   197ms
sort:           27ms

500만개 데이터
parallelSort:   233ms
sort:           196ms

1000만개 데이터
parallelSort:   285ms
sort:           416ms
```

