package modernjavainaction.ch10.basic;

import modernjavainaction.ch10.beans.Order;
import modernjavainaction.ch10.beans.Stock;
import modernjavainaction.ch10.beans.Trade;

public class Practice {
    public static void main(String[] args) {
        Order order = new Order();
        order.setCustomer("BigBank");

        Trade trade1 = new Trade();
        trade1.setType(Trade.Type.BUY);

        Stock stock1 = new Stock();
        stock1.setSymbol("IBM");
        stock1.setMarket("NYSE");

        trade1.setStock(stock1);
        trade1.setPrice(125.00);
        trade1.setQuantity(80);
        order.addTrade(trade1);

        Trade trade2 = new Trade();
        trade1.setType(Trade.Type.SELL);

        Stock stock2 = new Stock();
        stock1.setSymbol("GOOGLE");
        stock1.setMarket("NASDAQ");

        trade1.setStock(stock2);
        trade1.setPrice(375.00);
        trade1.setQuantity(50);
        order.addTrade(trade2);

    }
}
