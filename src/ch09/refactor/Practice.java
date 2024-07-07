package modernjavainaction.ch09.refactor;


import thisisjava.thread.common.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Practice {

    int a = 10;

    Consumer<Integer> consumer = (num) -> {
        int a = 11;
        System.out.println(a + num);
    };

    Consumer<Integer> consu = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) {
            int a = 10;
        }
    };
    public void printer(List<Integer> list, Consumer<Integer> consumer){
        for(Integer num: list){
            consumer.accept(num);
        }
        System.out.println("Practice: "+this);
    }

    public void go(Practice practice){
        List<Integer> list = List.of(10, 20, 30);

        printer(List.of(10, 20, 30), new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                System.out.println("number: "+integer+" this: "+this);
            }
        });
        int a= 10;
        printer(list, (num)->{
            System.out.println("number: "+num+" this: "+this);
            System.out.println("isSame?: "+(this == practice));
        });
    }
    
    public static void m1(Runnable runnable){
        runnable.run();
    }
    public static void m1(Task task){
        task.run();
    }
    
    interface Task{
        public void run();
    }

    public static void main(String[] args) throws Exception{
        Practice practice = new Practice();
        practice.go(practice);
        practice.printer(List.of(10), practice.consumer);
//        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
//        bf.readLine();
        // m1(() -> System.out.println("hello")); 모호한 에러발생
        // 명확하게 어떤 함수형 인터페이스를 쓸건지 캐스팅 해줘야함

        // 로그를 찍으러 하는데, 현재 로깅 레벨이 FINER일 때만 찍으려 한다.
        Logger logger = Logger.getLogger("testLogger");
        logger.setLevel(Level.CONFIG);
//        logger.log(Level.FINEST, generateDiagnostic());
        // 앞의 인자값은 이미 isLoggable에 의해 return 되어야 하나, generateDiagnostic을 기다려야만 한다.
        logger.log(Level.FINER, () -> generateDiagnostic());
        // 람다표현식으로 감싸져있기 때문에, 해당 람다표현식의 메소드를 호출할 때 까지 평가되지 않는다.


        // 전략을 정의하고 사용하기 위해서 별도의 클래스에 알고리즘별로 오버라이딩하고 해당 객체를 인자로 넣음
        doStrategy(practice.new StrategyA());
        doStrategy(practice.new StrategyB());

        // 람다를 사용하면 별도의 class 를 생성하지 않고도 가능
        doStrategy(() -> System.out.println("method1"));
        doStrategy(() -> System.out.println("method2"));

        practice.new Service(){
            @Override
            void templateProcess() {
                System.out.println("template method1");
            }

        }.serving();

        practice.new Service1().serving(()-> System.out.println("template method1"));
    }
    static String generateDiagnostic() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        return "hello";
    }

    interface Strategy{
        void method1();
    }

    class StrategyA implements Strategy{

        @Override
        public void method1() {
            System.out.println("method1");
        }
    }

    class StrategyB implements Strategy{

        @Override
        public void method1() {
            System.out.println("method2");
        }
    }


    abstract class Service{

        public void serving(){
            templateProcess();
        }

        abstract void templateProcess();

    }

    class Service1{

        public void serving(Runnable runnable){
            runnable.run();
        }
    }

    public static void doStrategy(Strategy stra){
        stra.method1();
    }
}
