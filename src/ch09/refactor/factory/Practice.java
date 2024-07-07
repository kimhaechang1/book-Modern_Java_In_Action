package modernjavainaction.ch09.refactor.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Practice {
    final static Map<String, Supplier<Product>> map = new HashMap<>();
    static{
        map.put("loan", Loan::new);
        map.put("stock", Stock::new);
        map.put("bond", Bond::new);
    }
    public static void main(String[] args) {
        // 인스턴스화 로직을 클라이언트에 노출하지 않고 객체를 만들 때 팩토리 디자인 패턴을 사용한다.
        Product p = Product.createProduct("loan");

        // 여기서 함수형 인터페이스와 생성자 참조를 활용하여 swtich-case문을 개선한다.
        Product p1 = Product.createProductByLambda("loan");
    }

    static class Product{
        public static Product createProduct(String name){
            return switch(name){
                case "loan" ->  new Loan();
                case "stock" -> new Stock();
                case "bond" -> new Bond();
                default -> throw new RuntimeException("No such product " + name);
            };
        }

        public static Product createProductByLambda(String name){
            if(map.containsKey(name)) return map.get(name).get();
            throw new RuntimeException("No such product "+name);
        }

    }
    static class Loan extends Product{

    }
    static class Stock extends Product{

    }
    static class Bond extends Product{

    }
}
