## 리액티브 프로그래밍

리액티브 프로그래밍은 다양한 시스템과 소스에서 들어오는 데이터 항목 스트림을 비동기적으로 처리하고 합친다.

이런 패러다임으로 설계된 어플리케이션의 경우 발생한 데이터 항목을 곧바로 처리하기에 높은 응답성을 가져다 준다.

### 리액티브 매니패스토

메니페스토는 과거 행적을 설명하고, 미래 행동의 동기를 밝히는 공적인 선언 즉, 선언문이라고 볼수 있다.

- 반응성(responsive): 리액티브 시스템은 빠를 뿐 아니라 더 중요한 특징으로 일정하고 예상할 수 있는 반응 시간을 제공한다.

- 회복성(resilient): 컴포넌트 실행 복제, 발송자와 수신자의 독립적인 생명주기, 서로다른 프로세스에서 실행됨, 비동기적으로 다른 컴포넌트에게 위임하는 등 회복성을 달성할 수 있는 다양한 방법이 있음

- 탄력성(elastic): 무거운 작업 부하가 발생하면 자동으로 관련 컴포넌트에 할당된 자원 수를 늘린다.

- 메시지 주도(message-driven): 서로의 컴포넌트끼리 구분되는 경계가 명확하기에, 컴포넌트사이에 비동기 메세지를 주고받음으로서 통신을 일으키면 탄력성과 회복성 등의 이점이 있음

## 어플리케이션 수준의 리액티브

어플리케이션 수준의 리액티브 프로그래밍의 주요 기능은 비동기로 작업을 수행할 수 있다는 점이다.

특히 개발자 입장에서는 동기 블록, 경쟁, 데드락 같은 저 수준의 멀티스레드 문제를 직접 처리할 필요가 없어진다.

또한 스레드를 쪼개어 사용하는 환경에서 I/O 동작등을 멀리해야 하는데,

`RxJava`와 같은 리액티브 프레임워크에서는 별도의 지정된 스레드풀에서 블록동작을 실행시켜서 메인 풀에 있는 스레드들은 영향을 받지 않는다.

### CPU관련 작업과 I/O 관련 작업

두 작업 모두 현재의 스레드 흐름을 블록하는건 동일하다.

하지만 CPU관련 작업은 현재 CPU코어 또는 스레드를 100% 활용하여 연산하기에 다른 일을 처리할 수 없는 반면

I/O의 경우 사용자 입력이나 외부 응답을 기다리면서 CPU 코어나 스레드가 처리할 일이 없어서 블록되는 상호아이다.

따라서 개발자는 CPU가 최대로 활용될 수 있도록 특정 작업이 CPU관련 작업인지 I/O관련 작업인지 적절하게 선택해야 한다.

## 시스템 수준의 리액티브

리액티브 아키텍처에서는 컴포넌트에서 발생한 장애를 고립시킴으로 문제가 주변의 다른 컴포넌트로 전파되면서 전체 시스템 장애로 이어지는 것을 막음으로 회복성을 제공한다.

이러한 것은 에러 전파를 방지하고 이들을 메세지로 바꾸어 다른 컴포넌트로 보내는 등 감독자 역할을 수행함으로서 이루어진다.

또한 모든 컴포넌트는 수신자의 위치에 상관없이 다른 모든 서비스와 통신이 가능하다는 위치 투명성으로 인해

시스템을 쉽게 복제할 수 있고, 현재 작업의 부하에 따라 어플리케이션을 확장할 수 있다.

이러한 탄력성에 있어서 `리액티브 어플리케이션`은 동시성 및 비동기로 해결하고, `리액티브 시스템`은 위의 위치 투명성을 활용하여 해결한다.

### 자바 9의 `Flow` 규칙

- `Publisher`는 반드시 `Subscription`의 `request`메서드에 정의된 개수 이하의 요소만 `Subscriber`에게 전달해야 한다. 성공적으로 전달하였다면 `onComplete`를 호출하고 문제가 발생하면 `onError`를 호출해 `Subscription`을 종료할 수 있다.

- `Subscriber`는 요소를 받아 처리할 수 있음을 `Publisher`에 알려야 한다. 따라서 역압력을 행사하여 너무많은 요소들을 과하게 받는경우를 피할 수 있다. 더욱이 `onComplete`나 `onError` 신호를 처리하는 상황에서 `Subscriber`는 `Publisher`나 `Subscription`의 어떤 메소드도 호출할 수 없다. 마지막으로 `Subscriber`는 `Subscription.request()` 메소드 호출 없이도 언제든 종료 시그널을 받을 준비가 되어있어야 하며, `Subscription.cancel()` 호출 된 이후에도 한 개 이상의 `onNext`를 받을 준비를 해야 한다.

- `Publisher`와 `Subscriber`는 정확하게 `Subscription`을 공유해야 하며 각각이 고유한 역할을 수행해야 한다. 그러기 위해서는 `onSubscribe`와 `onNext`메서드에서 `Subscriber`는 `request` 메서드를 동기적으로 호출할 수 있어야 한다. 같은 `Subscriber`객체가 다시 가입하는 것은 권잔장하진 않는다.

### `StackOverflow` 문제 해결하기

기존의 예제를 실행하면 랜덤하게 `RuntimeException()`을 일으키기에 큰 문제가 되지않지만

`RuntimeException`코드를 없애고 실행하면 문제가 발생한다.

이유는 구독을 하게되면 `onSubscribe`를 발생시키고, 그 내부에서 `request`를 호출하고 `request`에 의해 `onNext` 가 호출되고, `onNext`에 의해 다시 `request`가 호출된다.

이러한 구조는 재귀구조로서 탈출구조를 따로 만들지 않으면, `StackOverflowError`가 발생하게 된다.

쓰레드마다 스택프레임이 생기기에, 별도의 스레드에서 동작하고 결과를 구독자에게 전달하게 하면 발생하지 않을것이다.

```java
@Override
public void request(long n) {
//        for(long i = 0L; i < n; i++){
//            try {
//                subscriber.onNext( TempInfo.fetch( town ) );
//            } catch (Exception e) {
//                subscriber.onError( e );
//                break;
//            }
//        }
    // 위의 코드는 StackOverflowError를 일으킴
    // 아래의 코드는 별도의 단일스레드 풀을 사용하여 작업큐에 들어온 순서대로 작업자 스레드에서 작동시킴
    executor.submit( () -> {
        for(long i = 0L; i < n; i++){
            try {
                subscriber.onNext( TempInfo.fetch( town ) );
            } catch (Exception e) {
                subscriber.onError( e );
                break;
            }
        }
    });
}
```

### `Processor` 인터페이스의 활용

`Processor` 를 보면 뭔가 기괴하게 `Subscriber`와 `Publisher`모두를 담는 특이한 인터페이스이다.

```java
public static interface Processor<T,R> extends Subscriber<T>, Publisher<R> {

}
```

얘의 역할은 `Publisher`를 구독한 다음 수신한 데이터를 한번 더 가공해서 제공하는 역할이다.

즉, 한번더 가공을 해야하므로 `Subscription`이 `Processor`의 메소드를 호출하게 만들어 위임하고,

`Processor`는 누구에게 전달해야 할지에 대한 정보도 알고있기에 그대로 전달하게 된다.

여기서 중요한점은 구독할 때 프로세서에도 구독자를 같이 등록하여야 하고,

재가공이 일어나지 않는 위임에 대해서는 원래 `Processor`가 없던 동작과 동일한 동작을 수행하도록 구현해야 한다.

```java
public class TempProcessor implements Flow.Processor<TempInfo, TempInfo> {

    private Flow.Subscriber<? super TempInfo> subscriber;

    @Override
    public void subscribe(Flow.Subscriber<? super TempInfo> subscriber) {
        this.subscriber = subscriber;
        // subscribe의 경우에는 사실 Processor의 subscribe을 호출할 일은 없을것
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscriber.onSubscribe(subscription);
        // 2차적으로 가공할 것들이 아니면 그대로 객체를 전달한다.
    }

    @Override
    public void onNext(TempInfo item) {
        subscriber.onNext(new TempInfo(item.getTown(), (item.getTemp() - 32) * 5 / 9));
        // 여기서 Publisher의 onNext로 발행할 온도값을 받아서 구독자에게 전달하기전에 한번 더 가공한다.
    }

    @Override
    public void onError(Throwable throwable) {
        // 2차적으로 가공할 것들이 아니면 그대로 객체를 전달한다.
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
        // 2차적으로 가공할 것들이 아니면 그대로 객체를 전달한다.
    }
}
```

```java
public static void main(String[] args) {
    getCelsiusTemperature("New York").subscribe(new TempSubscriber());
}

private static Flow.Publisher<TempInfo> getCelsiusTemperature(String town){
    return subscriber -> {
        // 여기서 구독에 대한 이벤트로 구독자의 onSubscribe()를 호출시켜 이벤트를 발생시킨다.
        // 만든 Processor의 객체를 넘김으로서 재정의한 메소드를 호출하도록 유도한다.
        // 여기서 Processor의 subscribe를 한번 더 호출하여 담당 프로세서를 연결한다.
        // 프로세서도 어떤 구독자에 대해 기능을 제공해야 하는지 정보를 갖고있어야 하므로 프로세서도 구독하게 한다.
        TempProcessor processor = new TempProcessor();
        processor.subscribe(subscriber);
        processor.onSubscribe(new TempSubscription( processor, town));
    };
}
```
