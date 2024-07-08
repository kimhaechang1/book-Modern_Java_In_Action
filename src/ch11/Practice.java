package com.khc.practice.modernjava.ch11;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Practice {

//    public static String getCarInsuranceName(Person p){
//        return p.getCar().getInsurance().getName();
//    }

    public static String checkedGetCarInsuranceName(Optional<Person> p){
        // 서로 중첩구조로 되어있는 형태를 전부 검사해야한다.
        // 아래와 같은 상황을 깊은 의심(deep doubt) 라고 부른다.
//        if(p != null){
//            Car car = p.getCar();
//            if(car != null){
//                Insurance insurance = car.getInsurance();
//                if(insurance != null){
//                    return insurance.getName();
//                }
//            }
//        }

        // after optional
        return p.flatMap(Person::getCar)
                .flatMap(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("Unknown");
    }

    public static Set<String> getCarInsuranceNames(List<Person> persons){
        return persons.stream()
                // Person 스트림 열기
                .map(Person::getCar)
                // Stream의 map 함수로서 하나의 Optional<Car>들로 구성된 Stream 형성 -> Stream<Optional<Car>>
                .map(optCar -> optCar.flatMap(Car::getInsurance))
                // Stream의 요소로 Optional<Car>를 하나씩 내면서 인자로 넘겨진 함수형 인터페이스의 결과 타입을 가지는 Optional로 변환한다. -> Stream<Optional<Insurance>>
                .map(optIns -> optIns.map(Insurance::getName))
                // Stream의 요소로 Optional<Insurance>를 하나씩 꺼내면서 함수형 인터페이스의 결과값을 가지는 Optional로 변환한다. 여기서도 인터페이스의 반환타입이 Optional이 아니기에 flatMap이 아니다.
                // -> Stream<Optional<String>>
                .flatMap(Optional::stream)
                // 마지막으로 Stream내부에 바로 값에 접근이 안되기때문에, 각 Stream 요소들을 Stream으로 반환하여 하나의 Stream내부로 모아주는 Stream flatMap을 사용한다.
                .collect(Collectors.toSet());
    }

    public static Insurance findCheapestInsurance(Person person, Car car){
        return new Insurance();
    }

    public static Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car){
//        if(person.isPresent() && car.isPresent()){
//            return Optional.of(findCheapestInsurance(person.get(), car.get()));
            // 또다시 null 체크를 장황하게 하고있다.
//        }else{
//            return Optional.empty();
//        }

        return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c)));
        // 일단 Person과 Car를 사용해서 전혀다른 Optional<Insurance>를 만들어야 하므로 flatMap이 필요하다.
        // 핵심은 map과 flatMap을 활용해서 안쪽 연산에서부터 안전하게 연산을 수행한다.
    }

    public String getCarInsuranceName(Optional<Person> person, int minAge){
        return person.filter(p -> p.getAge() >= minAge)
                // filter를 통해 일단 값이 조건에 맞는지 검사한다.
                // 만약 person이 empty라면 자기자신을 반환하고, Perdicate가 false면 empty를 반환한다.
                .flatMap(Person::getCar)
                .flatMap(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("Unknown");
    }

    public Optional<Integer> optionalParser(String numStr){
        try{
            return Optional.of(Integer.parseInt(numStr));
        }catch(NumberFormatException e){
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        // null pointer exception이 발생한다.
        // System.out.println(getCarInsuranceName(new Person()));

        // Unknown이 출력된다.
        System.out.println(checkedGetCarInsuranceName(Optional.ofNullable(new Person())));

    }

    public static class Person{
//        private Car car;
//        public Car getCar(){
//            return car;
//        }
        private int age;

        public int getAge() {
            return age;
        }

        private Optional<Car> car; // 사람이 자동차를 소유하지 않았을수도 있으므로
        public Optional<Car> getCar(){
            return car;
        }
    }
    public static class Car{
//        private Insurance insurance;
//        public Insurance getInsurance(){
//            return insurance;
//        }

        private Optional<Insurance> insurance;
        public Optional<Insurance> getInsurance(){
            return insurance;
        }
    }
    public static class Insurance{
        private String name;

        public String getName() {
            return name;
        }
    }
}
