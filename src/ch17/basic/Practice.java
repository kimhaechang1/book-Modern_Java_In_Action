package modernjavainaction.ch17.basic;

import java.util.concurrent.Flow;

public class Practice {

    public static void main(String[] args) {
//        getTemperature( "New York" ).subscribe( new TempSubscriber() );
        // 기본 예제

        getCelsiusTemperature("New York").subscribe(new TempSubscriber());
    }

    private static Flow.Publisher<TempInfo> getTemperature( String town ){
        // Publisher는 FuntionalInterface로서 구독자 객체를 받아서 본인에게 구독시키는 추상 메소드 하나를 가진다.
        // 이 때 구독이 되었다는 onSubscribe를 호출함으로서 구독자가 역압력을 행사할 수 있도록 TempSubscription 객체를 전달한다.
        return subscriber -> subscriber.onSubscribe( new TempSubscription( subscriber, town ) );
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

}
