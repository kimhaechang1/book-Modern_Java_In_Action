package modernjavainaction.ch10.chain;

import modernjavainaction.ch10.beans.Order;

public class Practice {
    public static void main(String[] args) {
        Order order = MethodChainingOrderBuilder.forCustomer("BigBank")
                .buy(80)
                .stock("IBM")
                .on("NYSE")
                .at(125.00)
                .sell(50)
                .stock("GOOGLE")
                .on("NASDAQ")
                .at(375.00)
                .end();
    }
}
