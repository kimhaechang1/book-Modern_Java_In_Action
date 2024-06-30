package modernjavainaction.ch06;

import static java.util.stream.Collectors.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class Practice {

    public static void main(String[] args) {
        List<Integer> numberList = asList(
          50, 11,119, 254, 9, 18, 33, 22, 98, 17
        );

        Optional<Integer> max1 = numberList.stream().collect(reducing((a, b) -> b - a));


        // 기존에 maxby를 몰랐다면? 정렬을 하고 젤 앞원소를 들고올 것

        Optional<Integer> maxBySort = numberList.stream()
                .sorted(Comparator.reverseOrder())
                .findFirst();
        if(maxBySort.isPresent()) System.out.println(maxBySort.get());

        // maxby 사용

        Optional<Integer> maxBy = numberList.stream()
                .collect(maxBy(Comparator.comparingInt(a -> a)));

        Integer max = maxBy.orElseGet(()-> 0);
        System.out.println(max);

        String [] srr = {"김", "회", "창", "회창", "김회창"};

        List<Data> list = new ArrayList<>();
        for(int i = 0;i<srr.length;i++){
            if(i >= 2) list.add(new Data(i, srr[i], Data.Type.WHITE));
            else list.add(new Data(i, srr[i], Data.Type.BLACK));
        }


        // 문제가 발생할 수 있다.
        // 아래의 코드는 Data 타입의 스트림에서 람다식의 결과값이 스트림의 타입과 동일해야 하므로 문제가 발생할 수 잇다.
//        String shortMenu = list.stream()
//                .collect(reducing((d1, d2) -> d1.getName() + d2.getName())).get();

        Map<Data.Type, List<Data>> nameByType = list.stream()
                .collect(groupingBy(Data::getType));

        System.out.println(nameByType);

        int length = 2;

        Map<Len, List<Data>> lenByLength = list.stream()
                .collect(groupingBy(data -> {
                    if(data.name.length() < length){
                        return Len.SHORT;
                    }else{
                        return Len.LONG;
                    }
                }));

        System.out.println(lenByLength);


        List<Dish> menu = asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH)
        );

        // 500 칼로리가 넘는 요리를 각 타입에 맞게 넣고싶다.
        // 그런데 아래와 같이 작동시키면 스트림 내에서 필요한 요소들만 추출해버리고 분류해버리기에
        // FISH 타입이 아에 비어있게 된다.
        Map<Dish.Type, List<Dish>> try1 = menu.stream()
                .filter(d -> d.getCalories() >= 500)
                .collect(groupingBy(Dish::getType));

        // 따라서 FISH 타입도 비어있지 않고 빈 리스트가 들어가있도록 하고싶다면
        // Collectors 클래스의 정적 메소드 filtering 을 사용한다.
        Map<Dish.Type, List<Dish>> try2 = menu.stream()
                        .collect(groupingBy(
                                Dish::getType,
                                filtering(d -> d.getCalories() >= 500, toList())
                                )
                        );
        System.out.println(try1);

        // 매핑을 돌리고싶을땐 mapping 을 사용하면 된다.
        Map<Dish.Type, List<String>> try3 = menu.stream()
                        .collect(groupingBy(
                                Dish::getType,
                                mapping(Dish::getName, toList())
                        ));
        System.out.println(try3);

        // 분류에 따라 수집할 요소가 List와 같은 자료구조에 쌓여 있을 경우
        // 기존에 요소가 배열이나 컬렉션인 경우 요소 하나씩 풀던 flatMap과 동일하게 flatMapping을 사용하면 된다.
        Map<String, List<String>> dishTags = new HashMap<>();
        dishTags.put("pork", asList("greasy", "salty")); dishTags.put("beef", asList("salty", "roasted"));
        dishTags.put("chicken", asList("fried", "crisp")); dishTags.put("french fries", asList("greasy", "fried"));
        dishTags.put("rice", asList("light", "natural")); dishTags.put("season fruit", asList("fresh", "natural"));
        dishTags.put("pizza", asList("tasty", "salty")); dishTags.put("prawns", asList("tasty", "roasted"));
        dishTags.put("salmon", asList("delicious", "fresh"));

        Map<Dish.Type, Set<String>> dishNamesByType = menu.stream()
                .collect(groupingBy(Dish::getType,
                        flatMapping(dish -> dishTags.get( dish.getName() ).stream(),toSet())));

        System.out.println(dishNamesByType);

        // 만약 Map안에 또다른 기준으로 Map을 형성하여서 2중 Map을 만들고 싶은 경우
        // groupingBy를 두번 하면 된다.
        // 타입으로 나누고 그 내부에서 CaloricLevel에 따라 Dish를 분류하자
        Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = menu.stream()
                .collect(groupingBy(
                        Dish::getType,
                        groupingBy(dish -> {
                            if(dish.getCalories() <= 400){
                                return CaloricLevel.DIET;
                            }else if(dish.getCalories() <= 700){
                                return CaloricLevel.NORMAL;
                            }else{
                                return CaloricLevel.FAT;
                            }
                        })
                ));

        System.out.println(dishesByTypeCaloricLevel);

        // 분할 예시

        Map<Boolean, List<Dish>> partitionedMenu = menu.stream()
                .collect(partitioningBy(Dish::isVegetarian));
        System.out.println(partitionedMenu);

        // 채식요리와 채식이 아닌 요리 각각의 그룹에서 가장 칼로리가 높은 요리
        Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream()
                .collect(
                        partitioningBy(
                                Dish::isVegetarian,
                                collectingAndThen(maxBy(Comparator.comparingInt(Dish::getCalories)), Optional::get)
                        )
                );
        System.out.println(mostCaloricPartitionedByVegetarian);

        Map<Boolean, Long> countPartition = menu.stream()
                .collect(
                        partitioningBy(Dish::isVegetarian, counting())
                );

        System.out.println(countPartition);

        // 특정 범위의 숫자를 소수와 비소수로 분할하기
        // 우선 소수 판정을 위한 isPrime을 하나 만든다.
        // isPrime은 해당 찾고자 하는 숫자의 sqrt한 값까지의 정수로 나누어떨어지는지 검사한다.
        System.out.println(partitionedPrime(20));


        // 커스텀 컬렉션 ToListCollector 사용해보기
        Stream<Dish> menuStream = menu.stream();
        List<Dish> dishes = menuStream.collect(new ToListCollector<>());
        System.out.println(dishes);

        Map<Dish.Type, List<Dish>> customListCollector = menu.stream()
                .collect(groupingBy(
                        Dish::getType,
                        filtering(dish -> dish.getCalories() > 500, new ToListCollector<>())
                ));
        System.out.println(customListCollector);

        // 커스텀 컬렉터와 이전까지의 partitionedBy 의 성능 비교

        long fastest = Long.MAX_VALUE;
        for(int i = 0;i<10;i++){
            long start = System.nanoTime();
//            partitionedPrime(1_000_000);
            partitionedPrimeByEnhanced(1_000_000);
            long duration = (System.nanoTime() - start) / 1_000_000;
            if(duration < fastest) fastest = duration;
        }
        System.out.println("result: "+fastest);
    }

    public static Map<Boolean, List<Integer>> partitionedPrime(int n){
        return IntStream.rangeClosed(2, n).boxed()
                .collect(partitioningBy(x -> isPrime(x)));
    }

    public static Map<Boolean, List<Integer>> partitionedPrimeByEnhanced(int n){
        return IntStream.rangeClosed(2, n).boxed()
                .collect(new PrimeNumbersCollector());
    }

    static boolean isPrime(int number){
        int candidate = (int) Math.sqrt((double) number);
        return IntStream.rangeClosed(2, candidate) // 2 ~ 해당숫자를 포함하는데 double -> int로 바뀌면서 포함 시켜야한다.
                .noneMatch(i -> number % i == 0);
    }

    public static boolean enhancedIsPrime(List<Integer> primes, int number){
        int candidate = (int) Math.sqrt((double) number);
        return primes.stream()
                .takeWhile(x -> x <= candidate)
                .noneMatch(i -> candidate % i == 0);
    }



    public enum CaloricLevel{
        DIET, NORMAL, FAT
    }
    static class Data{
        private int value;
        private String name;

        private Type type;

        public enum Type{
            BLACK, WHITE
        }

        public Data(int value, String name, Data.Type type) {
            this.value = value;
            this.name = name;
            this.type = type;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "value=" + value +
                    ", name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
    static enum Len{
        LONG, SHORT
    }
}
