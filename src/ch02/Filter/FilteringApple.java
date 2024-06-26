package modernjavainaction.ch02.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static modernjavainaction.ch02.Filter.Color.GREEN;
import static modernjavainaction.ch02.Filter.Color.RED;

public class FilteringApple {
    public static void main(String[] args) {
        List<Apple> list = Arrays.asList(
          new Apple(GREEN, 150),
          new Apple(RED, 160),
          new Apple(GREEN, 170)
        );
        // 위의 메서드는 초록색 사과만을 걸러낼 수 있다.
        // 하지만 초록색 사과만이 아닌, 다양한 색깔을 조건으로 처리하고싶다.
        System.out.println(filterGreenApples(list));

        // 현재 사과가 갖고 있던 색깔 종류에 따라 조건을 만들었다.
        // 여기서 무게라는 프로퍼티를 기준으로 추가하려 한다면 아래와 같이 변할것이다.
        System.out.println(filterApplesByColor(list, GREEN));
        System.out.println(filterApplesByColor(list, RED));

        // 하지만 이것으로 끝나는것이 아닌, 다양한 프로퍼티가 나중에 추가되고, 검색 조건이 될 수 있다.
        // 그렇다고해서 아래와 같이 메서드를 만들 경우 사용자 입장에서 각 파라미터를 유추하기도 힘들다.
        // 또한 유연한 필터를 목적으로 만든거지만, 조건의 종류가 달라질 경우 여전히 유연하게 대처할 수 없다.
        System.out.println(filterApples(list, GREEN, 0, true));
        System.out.println(filterApples(list, null, 150, false));

        // 그래서 동작의 파라미터화를 하여 인수로 넘기게 된다면 달라진다.
        // 우리는 필터링의 기준을 다양한 전략화라고 볼 수있다.
        // 하지만 우리가 직접 전달해야할 동작은 결국 test() 메소드인데 하나의 클래스를 더 감싸서 인스턴스를 보내야 한다.
        System.out.println(filterApples(list, new AppleGreenColorPredicate()));
        System.out.println(filterApples(list, new AppleHeavyWeightPredicate()));

        // 쓸데없이 일회성으로 동작을 설명하는 클래스를 계속해서 정의할 필요가 있을까?
        // 그래서 익명 구현 객체를 활용한다.
        System.out.println(filterApples(list, new ApplePredicate() {
            public boolean test(Apple apple) {
                return RED.equals(apple.getColor());
            }
        }));

        // 하지만 위의 상황에서도 클래스를 새롭게 정의 할 필요가 없을 뿐이지
        // 익명 구현 객체도 상당부분 코드라인을 차지한다.
        // 이럴때 나온것이 람다식이다.
        System.out.println(filterApples(list, (apple) -> RED.equals(apple.getColor())));

        // 위의 사례를 응용하여 더 넓은 범위에서 보아 추상화 할 수 있다.
        // List 인터페이스를 사용하여, 동작의 파라미터화 시킨 메소드를 기준으로 새로운 리스트를 만드는 메서드를 정의할 수 있다.
        System.out.println(FilteringApple.<Apple>filter(list, (apple) -> RED.equals(apple.getColor())));

        System.out.println(doTest(2, 3, (a, b)-> a + b ));
    
        // 캐스팅 하지 않으면 ambiguous 에러 뜸
        execute( (Action) ()-> System.out.println("hello"));
    }

    public interface Action{
        void run(); // 함수 디스크립터는 () -> void 로서 Runnable과 동일하게 된다.
    }

    public static void execute(Action act){

    }

    public static void execute(Runnable runnable){

    }

    interface Calculator{
        int calc(int a, int b);
    }

    interface Player{
        String play(String name);
    }

    public static int doTest(int a, int b, Calculator calc){
        return calc.calc(a, b);
    }


    public static void print(Runnable runnable){
        runnable.run();
    }

    public static List<Apple> filterGreenApples(List<Apple> inventory){
        List<Apple> result = new ArrayList<>();
        for(Apple apple: inventory){
            if(GREEN.equals(apple.getColor())){
                result.add(apple);
            }
        }
        return result;
    }
    public static List<Apple> filterApplesByColor(List<Apple> inventory, Color color){
        List<Apple> result = new ArrayList<>();
        for(Apple apple: inventory){
            if(color.equals(apple.getColor())){
                result.add(apple);
            }
        }
        return result;
    }
    public static List<Apple> filterApples(List<Apple> inventory, Color color, int weight, boolean flag){
        // weight 무게 고려, flag 는 true: 색상을 고려, false: 무게를 고려
        List<Apple> result = new ArrayList<>();
        for(Apple apple: inventory){
            if((flag && apple.getColor().equals(color)) ||
                (!flag && apple.getWeight() > weight)
            ){
                result.add(apple);
            }
        }
        return result;
    }
    public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p){
        List<Apple> result = new ArrayList<>();
        for(Apple apple: inventory){
            if(p.test(apple)){
                result.add(apple);
            }
        }
        return result;
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> p){
        List<T> result = new ArrayList<>();
        for(T t: list){
            p.test(t);
        }
        return result;
    }
}
