package modernjavainaction.ch10.nested;

import modernjavainaction.ch10.beans.Order;
import modernjavainaction.ch10.beans.Stock;
import modernjavainaction.ch10.beans.Trade;

import java.util.stream.Stream;

public class NestedFunctionOrderBuilder {

    public static Order order(String customer, Trade...trades){
        Order order = new Order();
        order.setCustomer(customer);
        Stream.<Trade>of(trades).forEach(order::addTrade);
        return order;
    }
    public static Trade buy(int quantity, Stock stock, double price){
        return buildTrade(quantity, Trade.Type.BUY, stock, price);
    }

    public static Trade sell(int quantity, Stock stock, double price){
        return buildTrade(quantity,  Trade.Type.SELL, stock, price);
    }

    private static Trade buildTrade(int quantity, Trade.Type type, Stock stock, double price){
        Trade trade = new Trade();
        trade.setPrice(price);
        trade.setStock(stock);
        trade.setQuantity(quantity);
        trade.setType(type);
        return trade;
    }

    public static Stock stock(String symbol, String market){
        Stock stock = new Stock();
        stock.setMarket(market);
        stock.setSymbol(symbol);
        return stock;
    }
    public static double at(double price){
         // stock 메소드 사용자 입장에서 단순히 값만을 넣는것은 해당 값의 역할을 파악하기 힘들다.
        return price;
    }

    public static String on(String market){
        return market;
    }

}
