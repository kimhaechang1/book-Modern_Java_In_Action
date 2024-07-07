package modernjavainaction.ch10.flag;

import modernjavainaction.ch10.beans.Order;

import java.util.function.DoubleUnaryOperator;

import static modernjavainaction.ch10.mixed.MixedBuilder.*;

public class Practice {
    public static void main(String[] args) {
        Order order =  forCustomer("BigBank",
            buy(t -> {
                t.quantity(80)
                        .stock("IBM")
                        .on("NYSE")
                        .at(125.00);
            }),
            sell(t -> {
                t.quantity(50)
                        .stock("GOOGLE")
                        .on("NASDAQ")
                        .at(325.00);
            })
        );
        System.out.println(calculator(order, true, true, false));
        // 세금 계산을 하기위해서 사용자는 인자순서를 외워야 한다.

        System.out.println(new TaxCalculator().withTaxGeneral().withTaxRegional().calculator(order));
        // 사용자 입장에서는 명확하게 구분이 된다.

        System.out.println(new TaxCalculatorWithLambda().with(Tax::regional).with(Tax::general).calculator(order));
        // 사용자가 원하는 알고리즘을 자유롭게 람다표현식으로 넣을 수 있고, 미리 정의된 메소드가 있다면 형식만 맞춰서 메소드참조를 사용할 수 있다.
    }

    public static double calculator(Order order, boolean useRegional, boolean useGeneral, boolean useSurcharge){

        // 그리고 너무많은 분기문 코드로 인해 장황해진다.
        double value = order.getValue();
        if (useRegional) value = Tax.regional(value);
        if (useGeneral) value = Tax.general(value);
        if (useSurcharge) value = Tax.surcharge(value);
        return value;
    }

    public static class TaxCalculator{

        // 체이닝으로 해결해보려 했지만, 여전히 각 플레그변수가 필요하다.
        // 나중에 조건이 늘어나면 늘어날수록 클래스 부피가 커질것이다.
        private boolean useRegional;
        private boolean useGeneral;
        private boolean useSurcharge;

        public TaxCalculator withTaxRegional(){
            useRegional = true;
            return this;
        }

        public TaxCalculator withTaxGeneral(){
            useGeneral = true;
            return this;
        }

        public TaxCalculator withTaxSurcharge(){
            useSurcharge = true;
            return this;
        }

        public double calculator(Order order){
            return Practice.calculator(order, useRegional, useGeneral, useSurcharge);
        }
    }

    public static class TaxCalculatorWithLambda{
        // 어짜피 사용자가 나중에 추가할 경우가 많아질거라면 사용자에게 정의를 맡기면 된다.
        // 그리고 순서대로 적용되도록 함성함수 메소드를 사용한다.
        public DoubleUnaryOperator taxFunction = d -> d; // dummy

        public TaxCalculatorWithLambda with(DoubleUnaryOperator taxFunction){
            this.taxFunction = this.taxFunction.andThen(taxFunction);
            return this;
        }

        public double calculator(Order order){
            return this.taxFunction.applyAsDouble(order.getValue());
        }
    }
}
