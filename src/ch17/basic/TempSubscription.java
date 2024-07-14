package modernjavainaction.ch17.basic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

public class TempSubscription implements Flow.Subscription {

    // Subscriber 를 위한 Subscription 구현
    // 어떤식으로 전달하는지 그 구현부를 담음

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Flow.Subscriber<? super TempInfo> subscriber;
    private final String town;

    public TempSubscription( Flow.Subscriber<? super TempInfo> subscriber, String town ){
        this.subscriber = subscriber;
        this.town = town;
    }

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

    @Override
    public void cancel() {
        subscriber.onComplete();
    }
}
