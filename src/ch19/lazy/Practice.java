package com.khc.practice.modernjava.ch19.lazy;

import java.util.stream.IntStream;

import static com.khc.practice.modernjava.ch19.lazy.LazyList.from;

public class Practice {
    static <T> void printAll(MyList<T> list){
        while(!list.isEmpty()){
            System.out.println(list.head());
            list = list.tail();
        }
    }
    public static void main(String[] args) {
        printAll(LazyList.primes(from(2)));
        // primes(numbers());


        /*MyList<Integer> myList = new MyLinkedList<>(5, new MyLinkedList<>(15, new Empty<>()));
        MyList<Integer> next = myList;
        StringBuilder sb = new StringBuilder();
        while(true){
            try{
                sb.append(next.head());
                next = next.tail();
                sb.append(" -> ");
            }catch(UnsupportedOperationException e){
                System.out.println(sb);
                break;
            }
        }

        MyList<Integer> myLazyList = from(2);*/
        // 2부터 시작하는 게으른 리스트를 만들었다.
        // 위의 매서드를 호출하였다고 하여 리스트가 즉각적으로 메모리위에 생성되지 않는다.
        // 아래의 주석을 해결하면 무한히 생성되는 모습을 볼 수 있다.
        /*while(true){
            System.out.print(myLazyList.head()+" -> ");
            myLazyList = myLazyList.tail();
        }*/

        // 심지어 Stream의 게으른평가 처럼 무한히 흘러가는 스트림인것 같지만
        // 평가가 필요할때 발생하기에 필요한 수만큼만 요소를 흘려보낼 수 있다.
//        LazyList<Integer> numbers = from(2);
//        int two = numbers.head();
//        int three = numbers.tail().head();
//        int four = numbers.tail().tail().head();
//        System.out.println(two+" "+three+" "+four);

//        LazyList<Integer> numbers2 = from(2);
//        System.out.println(LazyList.primes(numbers2).head());
//        System.out.println(LazyList.primes(numbers2).tail().tail().head());
//        System.out.println("###########################");
        // new LazyList<>()
        /*int prime1 = LazyList.primes(numbers2).head();
        int prime2 = LazyList.primes(numbers2).tail().head();
        System.out.println("FDafadfa");
        int prime3 = LazyList.primes(numbers2).tail().tail().head();*/
//        System.out.println("FDafadfa12314");
//        LazyList.primes(numbers2).tail().head();
//        LazyList.primes(numbers2).tail().tail().head();
//        System.out.println(LazyList.primes(numbers2).tail().tail().head());

        // System.out.println(prime1+" "+prime2+" "+prime3);
    }
    static IntStream numbers() {
        return IntStream.iterate(2, n -> n+1);
    }
    static int head(IntStream numbers){
        return numbers.findFirst().getAsInt();
    }
    static IntStream tail(IntStream numbers){
        return numbers.skip(1);
    }
    static IntStream primes(IntStream numbers){
        int head = head(numbers);
        return IntStream.concat(
                IntStream.of(head),
                primes(tail(numbers).filter(n -> n % head != 0))
        );
    }
}
