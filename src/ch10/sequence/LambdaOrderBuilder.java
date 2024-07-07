package modernjavainaction.ch10.sequence;

import modernjavainaction.ch10.beans.Order;
import modernjavainaction.ch10.beans.Stock;
import modernjavainaction.ch10.beans.Trade;
import modernjavainaction.ch10.chain.MethodChainingOrderBuilder;

import java.util.function.Consumer;

public class LambdaOrderBuilder {

    private Order order = new Order();

    public static Order order(Consumer<LambdaOrderBuilder> consumer){
        LambdaOrderBuilder builder = new LambdaOrderBuilder();
        consumer.accept(builder);
        // 사용자에게 LambdaOrderBuilder 메소드 사용을 온전히 맡긴다.
        return builder.order;
    }

    public void forCustomer(String customer){
        // 사용자에 맡겨질 메소드
        order.setCustomer(customer);
    }

    public void buy(Consumer<TradeBuilder> consumer){
        trade(consumer, Trade.Type.BUY);
    }

    public void sell(Consumer<TradeBuilder> consumer){
        trade(consumer, Trade.Type.SELL);
    }

    private void trade(Consumer<TradeBuilder> consumer, Trade.Type type){
        // buy or sell 타입이 정해지면 Trade를 만들어내는 TradeBuilder를 사용한다.
        // TradeBuilder 역시 람다 표현식으로 사용자에게 Trade를 만드는 메소드 사용을 맡긴다.
        TradeBuilder builder = new TradeBuilder();
        builder.trade.setType(type);
        consumer.accept(builder);
        order.addTrade(builder.trade);
        // 다 끝나면 단순히 하위 빌더의 완성을 저장한다.
    }

    public static class TradeBuilder{
        private Trade trade = new Trade();

        public void quantity(int quantity){
            this.trade.setQuantity(quantity);
        }

        public void price(double price){
            this.trade.setPrice(price);
        }

        public void stock(Consumer<StockBuilder> consumer){
            StockBuilder builder = new StockBuilder();
            consumer.accept(builder);
            trade.setStock(builder.stock);
            // 완성되면 하위 빌더의 결과물을 저장한다
        }
    }

    public static class StockBuilder{

        private Stock stock = new Stock();

        public void symbol(String symbol){
            stock.setSymbol(symbol);
        }
        public void market(String market){
            stock.setMarket(market);
        }
    }

}
