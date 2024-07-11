## 소프트웨어 모듈화의 필요성

모듈화는 궁극적으로 소프트웨어 아키텍처 즉 고수준에서는 기반 코드를 바꿔야 할 때, 유추하기 쉬워짐으로 생산성이 높아진다.

추론하기 쉬운 소프트웨어를 만드는데 도움을 주는 걸로 `관심사 분리(Seperation of concerns)`와 `정보 은닉(Information hiding)` 가 있다.

### 관심사 분리 [Seperation of concerns]

컴퓨터 프로그램을고유의 기능으로 나누는 동작을 권장하는 원칙이다.

다시말해, 클래스를 그룹화한 모듈을 이용해 어플리케이션의 클래스 간의 관계를 시각적으로 보여줄 수 있다.

기존의 패키지로 클래스를 그룹화하는 것은, 클래스가 어떤 다른 클래스를 볼 수 있는지를 컴파일시간에 정교하게 제어할 수 없다.

이러한 SoC 원칙 개별 기능을 따로 작업할 수 있고, 개별 부분이 모듈화 되어 재사용성이 높아지고, 독립적으로 작동하기에 쉽게 유지보수 할 수 있다.

### 정보은닉

세부 구현을 숨기도록 장려하는 원칙으로서, 변화하는 요구사항에 따라 소프트웨어의 변경이 일어날때, 내부의 다른요소에게 영향을 미치는것을 줄일 수 있다.

### 모듈화의 한계

Java 9이전까지는 모듈화된 소프트웨어를 만드는데 한계가 있었다.

자바에서는 코드 그룹핑의 목적으로 클래스, 패키지, JAR 세 가지 기능을 제공했다.

클래스 수준에서는 접근제한자를 활용한 캡슐화가 가능했다. 하지만 패키지나 JAR수준에서는 지원하지 않았다.

특히 어떤 패키지속 클래스들을 다른 패키지에 공개하기 위해서는 `public`의 접근제한자를 뒀어야 했는데,

이는 결국 어느 패키지에서도 접근이 가능하단 것이 되고, 보안에 취약해졌다.

### 클래스경로

흔히 클래스들을 JAR에 묶어서 자바 프로그램을 실행할 때 클래스 경로에 JAR을 추가하여 동적으로 로딩할 수 있다.

하지만 클래스경로와 JAR에는 몇가지 단점이 존재한다.

1. 클래스 경로에서는 같은 클래스를 구분하는 버전 개념이 없다.

2. 클래스 경로는 명시적인 의존성을 지원하지 않는다. `classes`라는 곳에 합쳐질 뿐이기 때문에, 충돌이 일어나는지 빠진게 있는지 파악하기 어렵다.

## 자바의 모듈

Java 8은 `모듈`이라는 새로운 자바 프로그램 구조 단위를 제공한다.

모듈은 `module`이라는 새 키워드에 이름과 바디를 추가해서 정의한다.

`모듈 디스크립터`는 `module-info.java` 라는 특별한 파일에 저장된다.

```
module 모듈명

exports 패키지명

requires 모듈명

정의한 모듈에서 특정 패키지를 내보내고, 현재의 모듈은 어떤 모듈들을 필요로 한다.
```

### 모듈 만드는법

빈 프로젝트 폴더를 만들고, 모듈을 추가한다.

그리고 src 폴더가 있으면 제거하고 com.xxx.xxx로 진입하게 만든다.

그리고 모듈명 바로 하위에 `module-info.java`를 만든다.

```
module expenses.application {
}
```

그리고 컴파일 후 모듈을 JAR에 추가한다.

```
javac module-info.java com/example/expenses/application/ExpensesApplication.java -d target
```

```
jar --create --file expenses-application.jar --main-class=com.example.expenses.application.ExpensesApplication -v -C target .
```

--verbose 옵션으로 인해 어떤 클래스가 포함되어있는지 확인이 가능하다.

```
added manifest
added module-info: module-info.class
adding: com/(in = 0) (out= 0)(stored 0%)
adding: com/example/(in = 0) (out= 0)(stored 0%)
adding: com/example/expenses/(in = 0) (out= 0)(stored 0%)
adding: com/example/expenses/application/(in = 0) (out= 0)(stored 0%)
adding: com/example/expenses/application/ExpensesApplication.class(in = 477) (out= 315)(deflated 33%)
```

그리고 만들어진 JAR을 실행한다.

```
java -jar expenses-application.jar
```
