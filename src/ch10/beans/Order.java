package modernjavainaction.ch10.beans;

import java.util.List;
import java.util.ArrayList;

public class Order {

    private String customer;
    private List<Trade> trades = new ArrayList<>();

    public void addTrade(Trade trade){
        this.trades.add(trade);
    }

    public String getCustomer(){
        return this.customer;
    }

    public void setCustomer(String customer){
        this.customer = customer;
    }

    public double getValue(){
        return trades.stream().mapToDouble(Trade::getValue).sum();
    }


}
