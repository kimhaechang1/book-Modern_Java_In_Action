package com.khc.practice.modernjava.ch19.lazy;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class LazyList<T> implements MyList<T>{

    final T head;
    final Supplier<LazyList<T>> tail;

    public LazyList(T head, Supplier<LazyList<T>> tail){
        this.head = head;
        this.tail = tail;
    }

    @Override
    public T head() {
        return head;
    }

    @Override
    public LazyList<T> tail() {
        // get()을 통해 다음 연결리스트 요소를 얻어와야 할 때에 생성하여 연결한다.
        return tail.get();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public static LazyList<Integer> from(int n){
        return new LazyList<Integer>(n, () -> from(n+1));
    }

    public static LazyList<Integer> primes(LazyList<Integer> numbers){
        return new LazyList<>(
            numbers.head(),
            ()-> primes(
                numbers.tail().filter(n -> {
                    return n % numbers.head() != 0;
                })
            )
        );
    }
    public LazyList<T> filter(Predicate<T> p){
        // 현재 filter를 호출하는 LazyList()의 상태는 new LazyList<>(3, () -> from(4)) 이다.
        // 여기서 비어있지 않기 때문에 p.test()를 통해 new LazyList<>(3, () -> from(4))
        return isEmpty() ?
                this :
                p.test(head()) ?
                        new LazyList<>(head, () -> tail().filter(p)) :
                        // 여기서의 tail().filter()를 한다는 것은 new LazyList<>(4, ()-> from(5))를 가져와서
                        // 가져온 LazyList의 filter(p)를 통해 동일한 규칙으로 필터링 하는건데 현재 람다표현식으로 감싸져 있으니까
                        //
                        tail().filter(p);
    }
}
