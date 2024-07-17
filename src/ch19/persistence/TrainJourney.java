package com.khc.practice.modernjava.ch19.persistence;

public class TrainJourney {
    TrainJourney onward;
    String name;
    int price;
    public TrainJourney(int price, String name, TrainJourney t){
        this.name = name;
        this.onward = t;
        this.price = price;
    }
    public TrainJourney(String name, TrainJourney t){
        this(0, name, t);
    }

    static TrainJourney link(TrainJourney a, TrainJourney b){
        if(a == null) return b;

        TrainJourney t = a;
        while(t.onward != null) {
            t = t.onward;
        }
        t.onward = b;
        return a;
    }
    static TrainJourney append(TrainJourney a, TrainJourney b){
        return a == null ? b : new TrainJourney(a.price, a.name, append(a.onward, b));
    }
}
