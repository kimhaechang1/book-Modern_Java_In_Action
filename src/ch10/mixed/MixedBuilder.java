package modernjavainaction.ch10.mixed;

import modernjavainaction.ch10.beans.Order;
import modernjavainaction.ch10.beans.Stock;
import modernjavainaction.ch10.beans.Trade;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class MixedBuilder {
    public static Order forCustomer(String customer, TradeBuilder ...tradeBuilders){
        // 정의가 완료된 빌더들을 가변인자로 받는다, 여기서 각 빌더들은 중첩된 함수로 구현된다.
        // 각 빌더들의 결과를 받아서 Stream으로 처리한다.
        // TradeBuilder들은 함수 시퀀스로 구현된다.
        Order order = new Order();
        order.setCustomer(customer);
        Stream.<TradeBuilder>of(tradeBuilders).forEach(b -> order.addTrade(b.trade));
        return order;
    }

    private MixedBuilder(){}

    public static TradeBuilder buy(Consumer<TradeBuilder> consumer){
        return buildTrade(consumer, Trade.Type.BUY);
    }

    public static TradeBuilder sell(Consumer<TradeBuilder> consumer){
        return buildTrade(consumer, Trade.Type.SELL);
    }

    private static TradeBuilder buildTrade(Consumer<TradeBuilder> consumer, Trade.Type type){
        // 각 거래들은 람다표현식으로 사용자에게 함수 사용을 맡긴다.
        TradeBuilder builder = new TradeBuilder();
        builder.trade.setType(type);
        consumer.accept(builder);
        return builder;
    }

    public static class TradeBuilder{

        private Trade trade = new Trade();

        public TradeBuilder quantity(int quantity){
            trade.setQuantity(quantity);
            return this;
        }

        public TradeBuilder at(double price){
            trade.setPrice(price);
            return this;
        }
        // 거래에서는 수량과 가격만 맞추고 Stock 정의로 넘어간다.
        // Stock은 Trade의 하위 도메인 계층으로, 자연스럽게 연결되도록 빌더를 연결한다.
        public StockBuilder stock(String symbol){
            return new StockBuilder(this, symbol, trade);
            // 이 과정에서 this와 trade를 넘겨서 젤 마지막에 호출되어야할 at 메소드로 이어지도록 유도한다.
        }
    }

    public static class StockBuilder{
        private final TradeBuilder builder;
        private final Trade trade;

        private final Stock stock = new Stock();

        public TradeBuilder on(String market){
            stock.setMarket(market);
            return builder;
        }

        private StockBuilder(TradeBuilder builder, String symbol, Trade trade){
            this.stock.setSymbol(symbol);
            this.builder = builder;
            this.trade = trade;
        }
    }
}
