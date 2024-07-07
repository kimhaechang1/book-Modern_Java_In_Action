package modernjavainaction.ch10.chain;

import modernjavainaction.ch10.beans.Order;
import modernjavainaction.ch10.beans.Stock;
import modernjavainaction.ch10.beans.Trade;

import java.lang.reflect.Method;

public class MethodChainingOrderBuilder {

    private final Order order = new Order(); // 최상위 빌더는 최종 반환값을 감싸고 있다.

    private MethodChainingOrderBuilder(String customer){ // 빌더 패턴을 사용하기에, new를 사용한 객체생성을 외부로부터 막는다.
        order.setCustomer(customer);
    }

    public static MethodChainingOrderBuilder forCustomer(String customer){
        return new MethodChainingOrderBuilder(customer);
    }

    public TradeBuilder buy(int quantity){
        return new TradeBuilder(this, Trade.Type.BUY, quantity); // setType보다는 직관적으로 행위를 설명할 수 있다.
    }

    public TradeBuilder sell(int quantity){
        return new TradeBuilder(this, Trade.Type.SELL, quantity);
    }

    public MethodChainingOrderBuilder addTrade(Trade trade){
        order.addTrade(trade);
        return this; // 하나의 거래를 완성했다고 하더라도, 주문을 끝내기 전까지 새로운 거래를 열수 있도록 유연함을 제공
    }

    public Order end(){
        return order; // 주문을 끝내는 함수로서, 주문내역을 볼 수 있도록 반환한다.
    }

    public static class TradeBuilder {
        private final MethodChainingOrderBuilder builder; // 최상위 빌더에 대한 정보를 갖고있어야 한다.
        private final Trade trade = new Trade(); // 빌더는 만들고자 하는 객체를 감싸고있는다.
        // 필요한 정보들을 하나씩 하위 빌더로 내려보내는 이유는
        // 최상위 빌더에서 다 할수있지만, 순서를 강제할 수 없기 때문이다.
        private TradeBuilder(MethodChainingOrderBuilder builder, Trade.Type type, int quantity){
            this.builder = builder; // 이전까지 완성된 빌더 내용을 넣는다.
            trade.setType(type);
            trade.setQuantity(quantity);

        }
        public StockBuilder stock(String symbol){
            return new StockBuilder(builder, trade, symbol);
        }
    }

    public static class StockBuilder{
        private final MethodChainingOrderBuilder builder;
        private final Trade trade;
        private final Stock stock = new Stock();

        private StockBuilder(MethodChainingOrderBuilder builder, Trade trade, String symbol){
            this.builder = builder;
            this.trade = trade;
            stock.setSymbol(symbol);
        }

        public TradeBuilderWithStock on(String market){
            stock.setMarket(market);
            trade.setStock(stock);
            return new TradeBuilderWithStock(builder, trade); // 최종 최하위 빌더에서는 객체를 결정짓는다.
            // 이 주식 도메인에서는 하나의 거래를 완성시키는 것이 빌더의 내부 1차완성이다.
        }
    }

    public static class TradeBuilderWithStock {
        private final MethodChainingOrderBuilder builder;
        private final Trade trade;

        private TradeBuilderWithStock(MethodChainingOrderBuilder builder, Trade trade){
            this.builder = builder;
            this.trade = trade;
        }

        public MethodChainingOrderBuilder at(double price){
            trade.setPrice(price);
            return builder.addTrade(trade); // 마지막으로 가격을 정했으면 하나의 거래가 완성되기에 최상위 빌더를 통해 마무리시킨다.
        }
    }
}
