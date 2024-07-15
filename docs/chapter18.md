## 함수형 프로그래밍

함수형 프로그래밍은 먼저 선언형 프로그래밍으로서 객체지향 프로그래밍의 명령형 프로그래밍과는 사뭇 다르다.

명령형 프로그래밍은 "어떻게"에 집중하는 반면, 선언형 프로그래밍은 `함수`를 사용하여 "무엇을" 에 집중하는 것이다.

```
예를들어 어떤 트랜잭션의 결과값 중 가장 큰 값을 꺼내와라고 하면

명령형 프로그래밍에서는
"전체 트랜잭션 리스트중에 첫번째 값을꺼내서 임시로 최대값으로 지정한다. 여기서 값이 null인경우 예외를 던지고, 아닐경우 순회하면서 임시 최대값을 계속해서 갱신한다."

라고 "어떻게" 수행을 진행해서 결과를 만들어 낼 것인지에 집중한다.

List<Transaction> trasactionList = new ArrayList<>();

Transaction most = transactionList.get(0);

if(most == null){
    throw new IllegalArgumentException("Empty list of transactions");
}

for(Transaction t: transactionList){
    if(t.getValue() > most.getValue()){
        most = t;
    }
}

선언형 프로그래밍에서는

리스트의 요소를 순회하는 "함수"를 호출해서, 그 순회하면서 최대값을 찾는 "함수"를 호출한다.

즉, 원하는 결과를 만족하기 위해서 "무엇"이 필요한지 생각하고, 필요한 "무엇"을 선언하여 해결하는 방식이다.

List<Transaction> trasactionList = new ArrayList<>();

Transaction most = transaction.stream().max(comparing(Transaction::getValue));

```

함수형 프로그래밍에는 `부작용 없음(no side effect)`와 `불변성(immutability)` 가 중요한 개념으로 작용한다.

여기서 말하는 `부작용`은 다음과 같은 예시로 들 수 있다.

- 자료구조를 고치거나 필드에 값을 할당

- 예외 발생

- 파일에 쓰기 등의 I/O 동작 수행

특히 함수형 프로그래밍에서는 수학적 함수와 비슷하다.

즉, 동일한 인자를 넣었을 때 언제나 같은 결과를 반환해야 하는것이다.

이 과정속에서 진짜 수학적 함수처럼 표현되면 좋겠지만, 일부 함수들은 동작과정 중에서 다른 시스템의 부분에 영향을 미칠수도 있다.

하지만 이를 해당 시스템이 인지하지 못하면 이것또한 함수형 프로그래밍의 목적을 달성할 수 있다.

```
예를들어서 어떤 함수는 외부 필드의 값을 인수로 넘어온 값에 따라 하나 감소시키거나 하나 증가시켜 사용하고

사용이 끝나고 반환할때 쯤에 그 값을 원본값으로 되돌려놓는다.

이러면 사실 해당 변수의 주인 입장에서는 어짜피 되돌려 놓아지는 값이기에 함수형 프로그래밍을 달성할 수 있다.

하지만 해당 공유변수에 대해서 동시성이 일어날 경우엔 얘기가 달라진다.
```

결국 함수형 프로그래밍에서 함수나 메서드는 지역변수만을 변경할 수 있으며

객체의 경우 참조하고 있다면, 해당 객체의 모든 필드는 `final`이어야 한다.

하지만 예외적으로 함수나 메서드 내부에서 생성한 객체의 경우 외부로 공개하지 않고, 해당 객체로 인해 다음 호출의 결과가 달라지지 않는다면 필드의 수정을 허락한다.

마지막으로 함수형 프로그래밍의 함수는 예외를 발생시켜선 안된다.

예외를 발생시킨다는것은 또다른 반환의 종류를 가질 수 있다는것이 되므로 `Optional`을 활용해야 한다.

```java
다음은 두 double 변수를 받아서 나눠주는 메소드이다.

public static double divide(double a, double b){
    return a / b
}

여기서 발생할 수 있는 문제는, unchecked 예외로 ArithmeticException이 발생할 수 있다는것이다.

따라서 인수에 따라 다양한 함수의 결과가 발생할 수 있으므로, 함수형 프로그래밍을 달성할 수 없다.

public static Double divide(double a, double b){
    Double result;
    try{
        result = a / b;
    }catch(ArithmeticException e){
        result = Double.valueOf(0);
    }
    return result;
}
이런식으로 지역 try-catch 를 사용해서 반환값을 강제로 맞춰주거나

public static Optional<Double> divide(double a, double b){
    if( a == 0 || b == 0 ){
        return Optional.empty();
    }
    return Optional.of( a / b ); 
}

이런식으로 만들어서 함수형의 목적을 달성할 수 있다.
```
