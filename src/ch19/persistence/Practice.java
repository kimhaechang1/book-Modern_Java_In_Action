package com.khc.practice.modernjava.ch19.persistence;

public class Practice {

    public static void main(String[] args) {
        TrainJourney XtoY = new TrainJourney("X", new TrainJourney(16, "Y", null));
        TrainJourney YtoZ = new TrainJourney("Y", new TrainJourney(18, "Z", null));

//        TrainJourney XtoYtoZ = TrainJourney.link(XtoY, YtoZ);

//        System.out.println(XtoY == XtoYtoZ);

        TrainJourney noSideEffectXtoYtoZ = TrainJourney.append(XtoY, YtoZ);
        System.out.println(XtoY == noSideEffectXtoYtoZ);
    }
}
