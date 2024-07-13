package com.khc.practice.modernjava.ch15.pubsub;

public class Practice {
    public static void main(String[] args) {
        // simplePubSub();

        arithmaticPubSub();


    }
    public static void simplePubSub(){
        SimpleCell c3 = new SimpleCell("C3");
        SimpleCell c2 = new SimpleCell("C2");
        SimpleCell c1 = new SimpleCell("C1");

        // c3가 c1을 구독
        c1.subscribe(c3);
        c1.onNext(10); // c1 발행자를 c3가 구독하고 있기에 notify되어 c3도 onNext이벤트가 전파되었다.
        c2.onNext(20);
    }

    public static void arithmaticPubSub(){
        ArithmeticCell p1 = new ArithmeticCell("ALU1");
        ArithmeticCell p2 = new ArithmeticCell("ALU2");

        SimpleCell c3 = new SimpleCell("C3");
        SimpleCell c2 = new SimpleCell("C2");
        SimpleCell c1 = new SimpleCell("C1");
        c1.subscribe(x -> p1.setLeft(x));
        // c1의 onNext이벤트 전파때 ArithmeticCell의 setLeft를 호출하도록 동작하는 구독자를 구독시킴
        c2.subscribe(x -> p1.setRight(x));
        // c2의 onNext이벤트 전파때 ArithmeticCell의 setLeft를 호출하도록 동작하는 구독자를 구독시킴
        p1.subscribe(x -> p2.setLeft(x));
        // p1의 onNext이벤트 전파때 ArithmeticCell p2의 setLeft를 호출하도록 동작하는 구독자를 구독시킴
        // p1의 subscribe는 SimpleCell 클래스의 subscribe를 호출하게 되고, 최상위 c1이나 c2에 변화가 결국 p1을 거쳐 p2에게 닿게됨
        c3.subscribe(x -> p2.setRight(x));
        // c3의 onNext이벤트 전파때 ArithmeticCell p2의 setRight를 호출하도록 동작하는 구독자를 구독시킴

        c1.onNext(10);
        c2.onNext(20);
        c1.onNext(15);
        c3.onNext(1);
        c3.onNext(3);
    }

}
