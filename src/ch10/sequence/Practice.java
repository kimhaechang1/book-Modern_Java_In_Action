package modernjavainaction.ch10.sequence;

import modernjavainaction.ch10.beans.Order;
import static modernjavainaction.ch10.sequence.LambdaOrderBuilder.*;

public class Practice {

    public static void main(String[] args) {
        Order order = order(o -> {
            o.buy(t -> {
                t.quantity(80);
                t.price(125.00);
                t.stock(s -> {
                    s.symbol("IBM");
                    s.market("NYSE");
                });
            });
            o.sell(t -> {
                t.quantity(50);
                t.price(375.00);
                t.stock(s -> {
                   s.symbol("GOOGLE");
                   s.market("NASDAQ");
                });
            });
        });
    }
}
