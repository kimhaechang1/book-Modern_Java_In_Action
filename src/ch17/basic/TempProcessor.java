package modernjavainaction.ch17.basic;

import java.util.concurrent.Flow;

public class TempProcessor implements Flow.Processor<TempInfo, TempInfo> {

    private Flow.Subscriber<? super TempInfo> subscriber;
    // 2차적으로 가공할 것들이 아니면 그대로 객체를 전달한다.

    @Override
    public void subscribe(Flow.Subscriber<? super TempInfo> subscriber) {
        this.subscriber = subscriber;
        // subscribe의 경우에는 사실 Processor의 subscribe을 호출할 일은 없을것
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(TempInfo item) {
        subscriber.onNext(new TempInfo(item.getTown(), (item.getTemp() - 32) * 5 / 9));
        // 여기서 Publisher의 onNext로 발행할 온도값을 받아서 구독자에게 전달하기전에 한번 더 가공한다.
    }

    @Override
    public void onError(Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }
}
