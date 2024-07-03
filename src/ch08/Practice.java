package modernjavainaction.ch08;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Practice {
    public static void main(String[] args) {

        List<Integer> numberList = new ArrayList<>(Arrays.asList(10,20,30,40,50,60,70,80,90,100));
        List<String> strList = new ArrayList<>(Arrays.asList("A12","C14", "b13"));
         // iteratorRemove(numberList); // ConcurrentModificationException 발생
        // 왜냐하면 다음 요소가 있는지 검사하는 건 외부 반복자를 통해 이뤄지고 있는데
        // remove는 원본소스를 건드리고 있으므로 일관성이 깨지는 문제가 발생한다.
        // 그래서 안전하게 삭제하고 싶다면 iterator를 명시해주고 해당 인터페이스의 remove 를 해주어야 한다.
        iteratorSafeRemove(numberList);

        // 아래의 CopyOnWriteArrayList는 Iterator를 사용하더라도 복사본을 사용해서 접근하기에, 수정을 가하더라도 영향이 없다.
        concurrentMod(numberList);

        // 여러 변경사항을 일으키기 위해서 Stream, ListIterator등의 방법이 있다.
        noUseReplaceAllIf(strList);

        // 하지만 위의 코드들은 새로운 컬렉션에 할당해야 하거나, 코드가 장황해진다.
        // replaceAll() 을 사용하면 조건에 맞는 요소들을 그대로 교환한다.
        // 내부적으로는 ListIterator를 돌리더라
        useReplaceAll(strList);

        // 이와 마찬가지로 removeIf도 Predicate를 인자로 넘겨받아서
        // 조건에 맞는 요소들을 삭제한다.
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(10,20,30,40,50,60,70,80,90,100));
        useRemoveIf(list);

        // 기존에 합치기 코드 -> 중복이 제거 되긴 하지만, 중복이 발생했을 때, 유연하게 합치고 싶어진다면
        Map<String, String> family = Map.ofEntries(
                Map.entry("Teo", "Star Wars"),
                Map.entry("Cristina", "James Bond")
        );
        Map<String, String> friends = Map.ofEntries(
                Map.entry("Raphael", "Star Wars"),
                Map.entry("Teo", "hello")
        );

        // key를 기준으로 정렬, value를 기준으로 정렬후, forEachOrdered를 통해 순서에 맞게 순회한다.
        Map<Integer, String> dataMap = Map.ofEntries(
                Map.entry(19, "abcdefg"),
                Map.entry(11, "abcdef"),
                Map.entry(15, "abcde")
        );
        dataMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(System.out::println);
        dataMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(System.out::println);

        Map<String, String> everyone = new HashMap<>(family); // family 는 ofEntries Factory method 로 인해 Immutable 상태이다.
//        everyone.putAll(friends);
//        System.out.println(everyone);

        // Map.merge(k, v, (v1, v2) -> v3): 해당 Map 객체에 지금 넣고싶은 key, value가 있고, 만약 충돌났다면 람다 표현식의 결과를 넣는걸로.
        // 기존에 key에 해당하는 value 가 null인 경우 현재 넣을려는 v를 그냥 넣는다.
        // 기존에 key에 해당하는 value 가 null이 아니면서 람다 표현식의 결과가 null인 경우 해당 key를 제거한다.
        friends.forEach((k, v) ->
            everyone.merge(k, v, (lv, rv) -> lv + " & " + rv)
        );

        // 기존에 key에 해당하는 value 가 null이 아니면서 람다 표현식의 결과가 null인 경우 해당 key를 제거한다.
        everyone.put("test", "testValue");
        System.out.println(everyone);
        String present = "testValue";
        everyone.merge("test", present, (v1, v2) -> null);
        System.out.println(everyone);

        // 또한 merge 를 사용하면 초기화 검사를 대체할 수 있다.
        // 예를들어 특정 key가 몇회 존재하였는지 count하는 Map이 있을때
        // 해당 Map을 사용하려면, 해당 key가 이전에 존재했는지 검사해야한다.
        String SENTENCE = "aaaaaaaaaaaabbbbbbbbbbbbbbcccccccccdddddddd";
        computeCountMap(SENTENCE);

        // merge를 활용하면, 기존의 값이 null이면 현재 넣을려는 값을 바로 넣고
        // 기존의 값이 null이 아니면, 람다표현식의 결과를 넣는다.
        computeCountMapByMerge(SENTENCE);


        // 기존에 해당하는 key가 처음 Map에 추가되려 한다면, null 체크를 따로해야 했으나,
        // computeIFAbsent를 사용하면 해당하는 key의 value가 null일 경우 람다표현식의 반환값을 해당 key의 value로 넣는다.
        // 만약 람다 표현식의 결과가 null이라면 해당 key를 제거한다.
        Map<String, List<String>> testMap = Map.ofEntries(
            Map.entry("Raphael", List.of("Star Wars", "반갑습니다.")),
            Map.entry("Teo", List.of("Star Wars", "반갑습니다.2"))
        );
        Map<String, List<String>> map = new HashMap<>(testMap);

        System.out.println(map.computeIfAbsent("Raphael", key -> new ArrayList<>()));
        System.out.println(map.computeIfAbsent("Tico", key -> new ArrayList<>()));

        // 그리고 computeIfPresent는 존재한다면 뒤의 람다표현식을 사용한다는 의미가 된다.
        map.computeIfPresent("Tico", (key, value) -> {
            value.add("Fdaffas");
            return value;
        });
        System.out.println(map);


    }
    public static void computeCountMap(String s){
        Map<Character, Long> countMap = new HashMap<>();
        s = s.toLowerCase();
        char[] alphas = s.toCharArray();
        for(char alpha: alphas){
            if(countMap.containsKey(alpha)){
                countMap.put(alpha, countMap.get(alpha) + 1L);
            }else{
                countMap.put(alpha, 1L);
            }
        }
        System.out.println(countMap);
    }
    public static void computeCountMapByMerge(String s){
        IntStream indexStream = IntStream.range(0, s.length());
        Map<Character, Long> countMap = new HashMap<>();
        indexStream.forEach(idx -> {
            countMap.merge(s.charAt(idx), 1L, Long::sum);
        });
    }
    public static void iteratorRemove(List<Integer> numberList) throws ConcurrentModificationException{
        for(Integer number: numberList){
            numberList.remove(10);
        }

    }

    public static void iteratorSafeRemove(List<Integer> numberList) throws ConcurrentModificationException{
        Iterator<Integer> iterator = numberList.iterator();
        while(iterator.hasNext()){
            System.out.print(iterator.next()+" -> ");
            iterator.remove();
        }
        System.out.println();
        System.out.println("after iterator remove: "+numberList);
    }

    public static void noUseReplaceAllIf(List<String> list){
        // 스트림을 사용하면 새로운 컬렉션을 만들어야 한다.
        List<String> after = list.stream()
                .map(x -> Character.toUpperCase(x.charAt(0)) + x.substring(1))
                .collect(toList());
        System.out.println("use Stream: "+after);
        // iterator를 사용하려 하니 아래처럼 코드가 길어진다.
        ListIterator<String> iterator = list.listIterator();
        while(iterator.hasNext()){
            String before = iterator.next();
            String code = Character.toUpperCase(before.charAt(0)) + before.substring(1);
            iterator.set(code);
        }
        System.out.println("use ListIterator: "+list);
    }

    public static void useReplaceAll(List<String> list){
        list.replaceAll(x -> Character.toUpperCase(x.charAt(0)) + x.substring(1));
        System.out.println("use replaceAll"+list);
    }
    public static void useRemoveIf(List<Integer> list){
        list.removeIf(x -> x > 50);
        System.out.println("after removeIf"+ list);
    }



    public static void concurrentMod(List<Integer> numberList){
        CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>(Arrays.asList(10,20,30,40,50,60,70,80,90,100));
        for(Integer number: copyOnWriteArrayList){
            System.out.print("doing iterator: "+number+" -> ");
            copyOnWriteArrayList.add(1555);
        }
        System.out.println();
        System.out.println("after iterator: "+copyOnWriteArrayList);
    }
}
