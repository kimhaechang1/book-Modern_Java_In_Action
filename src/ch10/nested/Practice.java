package modernjavainaction.ch10.nested;

import modernjavainaction.ch10.beans.Order;
import static modernjavainaction.ch10.nested.NestedFunctionOrderBuilder.*;

public class Practice {
    public static void main(String[] args) {
        Order order = order("BigBank",
                  buy( 80,
                          stock("IBM", on("NYSE")), at(125.00)
                  ),
                  sell(50,
                         stock("IBM", on("NYSE")), at(125.00)
                  )
                );

    }
}
