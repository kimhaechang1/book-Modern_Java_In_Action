package com.khc.practice.modernjava.ch15.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class SimpleCell implements Flow.Publisher<Integer>, Flow.Subscriber<Integer> {

    private int value = 0;
    private String name;
    private List<Flow.Subscriber<? super Integer>> subscribers = new ArrayList<>();
    public SimpleCell(String name){
        this.name = name;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
        subscribers.add(subscriber);
    }

    public void subscribe(Consumer<? super Integer> onNext) {
        // 특정 이벤트의 함수 정의를 인자로 받아서, 해당 이벤트를 처리하는 구독자 객체를 만들고 발행자를 구독
        // 즉, onNext이벤트에 반응하는 구독자를 만들어서 구독하게 만드는 함수
        subscribers.add(new Flow.Subscriber<>() {

            @Override
            public void onSubscribe(Flow.Subscription subscription) {

            }

            @Override
            public void onNext(Integer item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    private void notifyAllSubscribers(){
        // 구독자들에게 발행자가 갖고있는 값으로 갱신시킴
        subscribers.forEach(sub -> sub.onNext(this.value));
    }

    @Override
    public void onNext(Integer item) {
        // 구독한 셀에 새 값이 생겼을 때 값을 갱신해서 반응함.
        this.value = item;
        System.out.println(this.name + ":"+this.value);
        notifyAllSubscribers();
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
