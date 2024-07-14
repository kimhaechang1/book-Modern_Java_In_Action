package modernjavainaction.ch17.basic;

import java.util.concurrent.Flow;

public class TempSubscriber implements Flow.Subscriber<TempInfo> {

    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request( 1 );
        // 구독 이후에 1개의 요청
    }

    @Override
    public void onNext(TempInfo item) {
        System.out.println( item );
        subscription.request( 1 );
        // 요청의 결과를 출력하고 다음을 요청
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println( throwable.getMessage() );
    }

    @Override
    public void onComplete() {
        System.out.println("DONE !");
    }
}
