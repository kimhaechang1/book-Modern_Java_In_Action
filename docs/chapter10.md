### 비즈니스 모델링에선 읽기 쉽고, 이해하기 쉬운 코드를 작성하라

개발팀과 도메인 전문가가 공유하고 이해할 수 있는 코드는 `생산성`과 직결되기 때문이다.

도메인 전문가는 전문 소프트웨어 개발자는 아닐 수 있지만, 개발 프로세스에 참여할 수 있고

비즈니스 관점에서 소프트웨어가 재대로 되었는지 확인할 수 있다.

## 도메인 전용 언어 (Domain Specific Language)

DSL은 특정 비즈니스 도메인의 문제를 해결하려고 만든 언어

예를들어서 회계 전용 소프트웨어를 개발한다고 하면, 도메인으로 통장 입출금 내역서, 계좌 통합같은 개념이 있다.

이러한 문제들을 표현하기 위해 DSL 을 사용하고,

이걸 Java와 접목시키면 **DSL은 도메인을 인터페이스로 만든 API**라고 생각할 수 있다.

DSL은 다음의 두가지 필요성을 항상 고려해야 한다.

- 의사 소통의 왕: 프로그래머가 아닌사람도 코드의 의도를 이해할 수 있어야 함
- 한번 코드를 구현하지만 여러 번 읽는다: 동료 프로그래머도 충분히 이해할 수 있어야함. 즉, 가독성이 곧 유지보수임

DSL을 잘만 사용한다면 위의 `가독성`과 `유지보수`성 뿐만아니라

비즈니스로직을 `높은 수준으로 추상화`하고 `캡슐화` 하여 반복을 피하고 외부로 공개할 부분만 공개할 수 있다.

특히 지정된 언어로 비즈니스 로직을 표현하기에, 다른 문제들과는 독립적으로 비즈니스 관련 문제에 집중할 수 있다는 `관심사 분리`가 확실해진다.

하지만 DSL자체를 제한적인 언어로 설계하는것과 초기 개발비용이 증가한다는점 등의 단점도 존재한다.

### 내부 DSL

내부 DSL이란 호스팅 언어로 구현한 DSL을 뜻한다.

### 다중 DSL

JVM 환경과 같이 가상환경에 의해 실행되는 언어들은 같은 가상환경위에서 돌아가는경우 서로 호환성을 기대할 수 있고

이과정에서 다른 언어가 DSL 구현에 더 유리할 수 있다.

하지만 그만큼 해당언어에 대한 기술자가 필요하고, 같은 가상환경이어도 완벽하게 서로 호환되지 않을 경우가 있다.

예를들어 Java의 컬렉션과 스칼라의 컬렉션은 서로 호환되지 않아서 한쪽으로 몰아주기 식으로 개발해야한다.

### 외부 DSL

자신만의 문법과 구문으로 새 언어를 설계해야 한다.

그만큼 비용이 많이 발생하는 방식이지만, 어떠한 제약도 받지 않기에 무한한 유연성을 가진다.

## 자바로 DSL을 만드는 패턴과 기법

예를들어 주식 객체와 주식거래 객체 그리고 손님당 주식거래 내역을 보유하고 있는 주문 객체 위 세가지 모델이 있다.

여기서 주식 도메인의 해결해야 할 API로, 특정 고객의 주식을 사고 파는 과정을 하나의 주문에 담아야 한다.

### 기본적인 해결방법

준비된 각 도메인을 활용해서 한번 만들어보면 다음과 같다.

```java
public class Practice {
    public static void main(String[] args) {
        Order order = new Order(); // 하나의 주문
        order.setCustomer("BigBank");

        Trade trade1 = new Trade();
        trade1.setType(Trade.Type.BUY); // 하나의 매수

        Stock stock1 = new Stock();
        stock1.setSymbol("IBM");
        stock1.setMarket("NYSE");

        trade1.setStock(stock1);
        trade1.setPrice(125.00);
        trade1.setQuantity(80);
        order.addTrade(trade1);

        Trade trade2 = new Trade();
        trade1.setType(Trade.Type.SELL); // 하나의 매도

        Stock stock2 = new Stock();
        stock2.setSymbol("GOOGLE");
        stock2.setMarket("NASDAQ");

        trade2.setStock(stock2);
        trade2.setPrice(375.00);
        trade2.setQuantity(50);
        order.addTrade(trade2);
    }
}
```

쓸데없이 반복되어서 적어야하는 객체명과 호출되는 메소드들을 생각하면 꼭 저렇게 매순간 다 적어야할까 란 생각이 든다.

이러한 상황에서 자바의 내부 DSL 기법들을 활용하면 간결하고 이해하기 쉬운 코드를 작성할 수 있다.

### 메서드 체인

위의 목적은 최종적으로는 해당 고객의 거래내역을 반환해야 하고

그 사이에서는 `sell`과 `buy`에 의해 하나의 거래가 독립적으로 표현된다.

그리고 각 거래마다 주식의 `trade`, `symbol` 그리고 해당주식을 몇개 사거나 팔건지와 가격까지 표현해야 한다.

도메인전문가가 테스트하기 위해서 여러 메소드를 호출하게 될텐데, 그때마다 순서는 강제하되, 개수를 자유롭게 늘릴수 있도록 체이닝기법을 사용한다.

이러한 체이닝기법을 사용하려면 몇개의 빌더를 구현해야 한다.

```java
public class MethodChainingOrderBuilder {

    public final Order order = new Order();
    // 최상위 빌더는 최종 반환값을 감싸고 있다.

    private MethodChainingOrderBuilder(String customer){
        // 빌더 패턴을 사용하기에, new를 사용한 객체생성을 외부로부터 막는다.
        order.setCustomer(customer);
    }

    public static MethodChainingOrderBuilder forCustomer(String customer){
        return new MethodChainingOrderBuilder(customer);
    }

    public TradeBuilder buy(int quantity){
        return new TradeBuilder(this, Trade.Type.BUY, quantity);
        // setType보다는 직관적으로 행위를 설명할 수 있다.
    }

    public TradeBuilder sell(int quantity){
        return new TradeBuilder(this, Trade.Type.SELL, quantity);
    }

    public MethodChainingOrderBuilder addTrade(Trade trade){
        order.addTrade(trade);
        return this;
        // 하나의 거래를 완성했다고 하더라도, 주문을 끝내기 전까지 새로운 거래를 열수 있도록 유연함을 제공
    }

    public Order end(){
        return order;
        // 주문을 끝내는 함수로서, 주문내역을 볼 수 있도록 반환한다.
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

        public TradeBuilderWithStock at(String market){
            stock.setMarket(market);
            trade.setStock(stock);
            return new TradeBuilderWithStock(builder, trade);
            // 최종 최하위 빌더에서는 객체를 결정짓는다.
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
            return builder.addTrade(trade);
            // 마지막으로 가격을 정했으면 하나의 거래가 완성되기에 최상위 빌더를 통해 마무리시킨다.
        }
    }
}
```

이렇게 만들어낸 메소드 체이닝 기법의 DSL을 활용하면 다음과 같이 달라진다.

```java
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
```

빌더 패턴의 단점으로는 도메인 객체의 중첩구조와 일치하게 들여쓰기를 강제하는 방법이 없다.

이말인 즉, 상위에서 하위 수준으로 보자면

```
Order > Trade > Stock
```

순으로 더 큰집합이라는걸 알 수 있지만, 이 집합에따라 다르게 눈에 잘 보이게 들여쓰기를 강제화할 수 가 없다.

```java
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
// 이런식으로 강제 할 수 없다.
```

빌더를 구현하기위해 상위 빌더와 하위 빌더를 연결하는 접착 코드도 필요하다.

### 중첩된 함수 이용

다른 함수내에 또다른 함수의 호출을 통해 도메인 모델을 만드는 방법이다.

```java
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
```

```java
public static void main(String[] args) {
    Order order = order("BigBank",
                buy(80,
                    stock("IBM", on("NYSE")),
                    at(125.00)
                ),
                sell(50,
                    stock("IBM", on("NYSE")),
                    at(125.00)
                )
            );
}
```

이전 DSL방식인 체이닝 보다는 어느정도 도메인의 계층구조를 파악하기가 쉽다.

왜냐하면 체이닝구조는 `Trade`가 `Stock`을 포함하는 이런 도메인적 계층구조를 빌더 사용자가 파악할 수 없기 때문이다.

하지만 중첩 함수구조도 더많은 괄호와 인자를 사용해야 한다는 문제점을 갖고있고,

선택사항 요소를 인자로 처리해야 하면 그만큼의 경우의수를 표현할 오버라이딩 메소드가 필요하다.

그리고 계층구조가 아닌 일반적 요소들은 인자로 넘기는데 있어서 사용자가 역할을 파악하기 힘들다

그나마 위의 메소드 중 `at`, `on`과 같은 의미적 부여를 위한 메소드를 사용함으로서 완화할 수는 있다.

### 람다표현식을 이용한 함수 시퀀싱

람다표현식을 받아 실행해 도메인 모델을 만들어 내는 여러 빌더를 구현해야 한다.

람다표현식을 활용한 함수 시퀀싱의 특징은 위의 메소드 체이닝방식과 중첩된 함수 이용의 장점을 더한다는 것이다.

즉, 메소드 체이닝 방식으로 메소드 호출을 나열하는것이 아닌, 필요한만큼 사용하고 순서는 강제하는것과

중첩된 함수이용의 장점인 도메인 객체의 계층구조를 표현할수 있는 장점이 포함된다.

```java
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
```

```java
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
```

### 다 섞는 혼합방법

모든상황에 적합한 방법은 없으므로, 적당히 섞어 쓰는것도 하나의 방법이다.

```java
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
```

### 합성함수와 람다표현식 그리고 체이닝을 사용하여 플래그 줄이기

어떤 여러 선택지들 중에서 해당사항이 있는 선택지들을 연산에 사용해야 할 때가 있다.

그럴때 일반적으로 플래그 변수로 `Boolean`을 사용해서 분기문으로 처리했다면

`체이닝`, `합성함수` 그리고 `람다표현식`을 활용해서 가독성을 높일수 있다.

```java
System.out.println(calculator(order, true, true, false));
// 세금 계산을 하기위해서 사용자는 인자순서를 외워야 한다.

System.out.println(new TaxCalculator().withTaxGeneral().withTaxRegional().calculator(order));
// 사용자 입장에서는 명확하게 구분이 된다.

System.out.println(new TaxCalculatorWithLambda().with(Tax::regional).with(Tax::general).calculator(order));
// 사용자가 원하는 알고리즘을 자유롭게 람다표현식으로 넣을 수 있고, 미리 정의된 메소드가 있다면 형식만 맞춰서 메소드참조를 사용할 수 있다.

public static double calculator(Order order, boolean useRegional, boolean useGeneral, boolean useSurcharge){

    // 그리고 너무많은 분기문 코드로 인해 장황해진다.
    double value = order.getValue();
    if (useRegional) value = Tax.regional(value);
    if (useGeneral) value = Tax.general(value);
    if (useSurcharge) value = Tax.surcharge(value);
    return value;
}

public static class TaxCalculator{

    // 체이닝으로 해결해보려 했지만, 여전히 각 플레그변수가 필요하다.
    // 나중에 조건이 늘어나면 늘어날수록 클래스 부피가 커질것이다.
    private boolean useRegional;
    private boolean useGeneral;
    private boolean useSurcharge;

    public TaxCalculator withTaxRegional(){
        useRegional = true;
        return this;
    }

    public TaxCalculator withTaxGeneral(){
        useGeneral = true;
        return this;
    }

    public TaxCalculator withTaxSurcharge(){
        useSurcharge = true;
        return this;
    }

    public double calculator(Order order){
        return Practice.calculator(order, useRegional, useGeneral, useSurcharge);
    }
}

public static class TaxCalculatorWithLambda{
    // 어짜피 사용자가 나중에 추가할 경우가 많아질거라면 사용자에게 정의를 맡기면 된다.
    // 그리고 순서대로 적용되도록 함성함수 메소드를 사용한다.
    public DoubleUnaryOperator taxFunction = d -> d; // dummy

    public TaxCalculatorWithLambda with(DoubleUnaryOperator taxFunction){
        this.taxFunction = this.taxFunction.andThen(taxFunction);
        return this;
    }

    public double calculator(Order order){
        return this.taxFunction.applyAsDouble(order.getValue());
    }
}
```
