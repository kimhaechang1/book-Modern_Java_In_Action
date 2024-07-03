https://www.javatpoint.com/fail-fast-and-fail-safe-iterator-in-java

https://velog.io/@dudwls0505/fail-fast-fail-safe-iterators

## Fail-Fast

흔히 `Collection`의 자료구조를 사용하면서, 자료구조에 변형을 가하는 메소드(`add`, `set`)등을 사용할 수 있다.

하지만 `Iterator`를 활용하여 반복할 때, 변형 메소드를 사용하면 `ConcurrentModificationException` 이 발생한다.

이는, 외부 반복자가 질의를 하는데 있어서 뷰의 일관성이 원본소스와 달라지는것을 막아야 하기 때문이다.

그래서 내부적으로 `modCount`라고 자료구조에 변경을 일으키는 메소드의 호출횟수를 셈하는 변수가 있는데,

```
HashSet은 HashMap으로 구현되어있고

HashMap과 AbstractList에 modCount가 있다.
```

저값을 `iterator`호출때에 복사해서 저장해두고, `next`와 같은 커서 이동 메소드를 호출할 시, 자료구조의 `modCount`값에 변동을 감지한다.

만약 변동이 일어났으면 `ConcurrentModificationException`이 발생한다.

```java
public static void iteratorRemove(List<Integer> numberList) throws ConcurrentModificationException{
    Set<Integer> set = new HashSet<>(numberList);
    Thread threadA = new Thread(){
        public void run(){
            Iterator<Integer> iter = set.iterator();
            while(iter.hasNext()){
                System.out.println(iter.next());
            }
        }
    };
    Thread threadB = new Thread(){
        public void run(){
            set.remove(10);
        }
    };

    threadA.start();
    threadB.start();
    // ConcurrentModificationException 발생
}
```

위와 같이 에러가 보고되면 즉각적으로 시스템을 종료하는것을 `Fail Fast System` 이라고 한다.

## Fail-Safe

작업도중 에러가 발생하거나, 실패가 발생하였더라도 지속해서 작업을 이어나가는 것을 의미한다.

그것이 가능한 이유는, 원본 객체 대신 컬렉션의 복사본으로 연산을 하기 때문이다.

컬렉션의 구조변경이 있더라도, 그것은 카피본에만 영향을 주는것이기 때문에 원본객체에 영향을 주지 않는다.

이러한 `Fail Safe`를 지원하는 컬렉션으로는

```
CopyOnWriteArrayList, ConcurrentHashMap
```

등이 있다.

물론 `ConcurrentHashMap`의 경우에는 별도의 복사를 하는것은 아니고

내부 구조상 동시 수정을 허용할 수 있다고 한다.

```java
public static void concurrentMod(List<Integer> numberList){
    CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>(numberList);
    for(Integer number: copyOnWriteArrayList){
        System.out.print("doing iterator: "+number+" -> ");
        copyOnWriteArrayList.add(1555);
    }
    System.out.println();
    System.out.println("after iterator: "+copyOnWriteArrayList);
}
```

```
doing iterator: 10 -> doing iterator: 20 -> doing iterator: 30 -> doing iterator: 40 -> doing iterator: 50 -> doing iterator: 60 -> doing iterator: 70 -> doing iterator: 80 -> doing iterator: 90 -> doing iterator: 100 ->
after iterator: [10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 1555, 1555, 1555, 1555, 1555, 1555, 1555, 1555, 1555, 1555]
```
