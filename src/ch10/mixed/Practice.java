package modernjavainaction.ch10.mixed;

import modernjavainaction.ch10.beans.Order;
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
    }
}
