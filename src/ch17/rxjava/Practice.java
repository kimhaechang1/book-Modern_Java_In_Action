package modernjavainaction.ch17.rxjava;

// add library rxjava-3.1.8
// add reactive-streams-1.0.4
import io.reactivex.rxjava3.core.Observable;
import modernjavainaction.ch17.basic.TempInfo;

import java.util.concurrent.TimeUnit;

public class Practice {

    public static void main(String[] args) {
        Observable<String> strings = Observable.just("first", "second");

        Observable<Long> onePerSec = Observable.interval(1, TimeUnit.SECONDS);

//        onePerSec.subscribe(i -> System.out.println(TempInfo.fetch("New York")));
        // 위의 코드를 실행해도 아무런 변화가 없다.
        // 이유는 별도의 데몬스레드에서 동작시키기 때문이다.
        // 데몬스레드는 메인스레드의 흐름이 종료되면 무조건 같이 종료되기에
        // 아무런 동작이 일어나지 않게된다.
        onePerSec.blockingSubscribe(i -> System.out.println(TempInfo.fetch("New York")));

        // blockingSubscribe는 메인 스레드의 흐름을 막고 동작한다.

    }
}
