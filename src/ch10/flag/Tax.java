package modernjavainaction.ch10.flag;

public class Tax {
    public static double regional(double value){
        return value * 1.1;
    }

    public static double general(double value){
        return value * 1.3;
    }

    public static double surcharge(double value){
        return value * 1.05;
    }
}
