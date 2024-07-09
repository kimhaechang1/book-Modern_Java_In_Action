package com.khc.practice.modernjava.ch12;

import java.text.DateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.*;
import java.util.Locale;

public class Practice {

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.of(2017, 9, 21);
        int year = localDate.getYear();
        System.out.println(year);

        // LocalTime과 LocalDate 가 있고, 각각의 atDate(), atTime() 메소드를 통해 LocalDateTime으로 만들 수 있다.
        LocalTime localTime = LocalTime.of(20, 12);
        LocalDateTime localDateTime = localTime.atDate(LocalDate.now());
        System.out.println(localDateTime);

        LocalDate date = LocalDate.now();
        System.out.println(date.get(ChronoField.DAY_OF_WEEK));
        System.out.println(date.getDayOfWeek().name());

        // LocalDateTime, LocalDate, LocalTime 모두 불변 클래스이다.
        // with~ 메소드로 LocalDate 조작하기
        LocalDate before = LocalDate.of(2017, 9, 21);
        LocalDate after = before.withYear(2011);
        System.out.println(before);
        System.out.println(after);
        // MONTH값에 해당하는 필드에 대해서 수정
        LocalDate after2 = before.with(ChronoField.MONTH_OF_YEAR, 2);
        System.out.println(after2);

        // 존재하지 않는 필드에 대해서 접근하려 하여 UnsupportedTemporalTypeException 발생
//        LocalDate after3 = before.with(ChronoField.CLOCK_HOUR_OF_DAY, 13);
//        System.out.println(after3);

        // 상대적으로 조작하기
        LocalDate after4 = before.plus(10, ChronoUnit.DAYS);
        System.out.println(after4);
        LocalDate after5 = before.plusDays(10);
        System.out.println(after5);
        // Duration이 TemporalAmount 인터페이스의 구현체
        // 하지만 Duration은 기본적으로 Seconds 계산을 내부적으로 수행한다.
        // 따라서 Period를 사용해서 계산해야 UnsupportedTemporalTypeException이 발생하지 않는다.
//        LocalDate after6 = before.plus(Duration.of(10, ChronoUnit.DAYS));
        LocalDate after6 = before.plus(Period.ofDays(10));
        System.out.println(after6);
        // 존재하지 않는 필드에 대해서 접근하려 하여 UnsupportedTemporalTypeException 발생
//         LocalDate after7 = before.plus(Duration.of(10, ChronoUnit.SECONDS));
        LocalDate after7 = before.minus(10, ChronoUnit.DAYS);

        // 다음날이 주말이면 평일날짜로 변환시켜주는 Custom TemporalAdjuster 사용
        LocalDate anyFriday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalDate after8 = anyFriday.with(new NextWorkingDay());
        System.out.println("이번주 금요일 날짜: "+anyFriday);
        System.out.println(after8);
        // 단일 추상메소드 이므로, 람다표현식으로 사용가능
        LocalDate after9 = anyFriday.with(t -> {
           DayOfWeek dayOfWeek = DayOfWeek.of(t.get(ChronoField.DAY_OF_WEEK));
            int addToDay = 1;
            if(dayOfWeek == DayOfWeek.FRIDAY) addToDay = 3;
            else if(dayOfWeek == DayOfWeek.SATURDAY) addToDay = 2;
            return t.plus(addToDay, ChronoUnit.DAYS);
        });
        System.out.println(after9);

        // DateTimeFormatter를 사용하여 LocalDate를 String으로
        LocalDate date2 = LocalDate.of(2014, 3, 18);
        String s1 = date2.format(DateTimeFormatter.BASIC_ISO_DATE);
        String s2 = date2.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println("BASIC_ISO_DATE: "+s1);
        System.out.println("ISO_LOCAL_DATE: "+s2);
        System.out.println("BASIC: "+date2);

        // 반대로 String을 LocalDate로
        LocalDate after10 = LocalDate.parse("20240318", DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(after10);

        // DateFormat은 스레드 세이프하지 않다
        // DateTimeFormatter는 스레드 세이프 하다.
        // ofPattern() 메소드를 통해 커스텀 패턴을 제시할 수 있다.
        /*"G{0,5}" +        // Era
        "y*" +            // Year
        "Q{0,5}" +        // Quarter
        "M{0,5}" +        // Month
        "w*" +            // Week of Week Based Year
        "E{0,5}" +        // Day of Week
        "d{0,2}" +        // Day of Month
        "B{0,5}" +        // Period/AmPm of Day
        "[hHjC]{0,2}" +   // Hour of Day/AmPm (refer to LDML for 'j' and 'C')
        "m{0,2}" +        // Minute of Hour
        "s{0,2}" +        // Second of Minute
        "[vz]{0,4}"       // Zone
        *
        * */
        DateTimeFormatter format = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.ITALIAN);
        LocalDate date3 = LocalDate.now();
        String formattedDate = date3.format(format);

        System.out.println(formattedDate);

        // DateTimeFormatterBuilder를 통해 복잡한 포메터를 좀 더 세부적으로 제어하기
        DateTimeFormatter italianFormatter = new DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_MONTH)
                .appendLiteral(". ")
                .appendText(ChronoField.MONTH_OF_YEAR)
                .appendLiteral(" ")
                .appendText(ChronoField.YEAR)
                .parseCaseInsensitive()
                .toFormatter(Locale.ITALIAN);

        String formatted2 = date3.format(italianFormatter);
        System.out.println(formatted2);
    }
    public static class NextWorkingDay implements TemporalAdjuster {

        @Override
        public Temporal adjustInto(Temporal temporal) {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            int addToDay = 1;
            if(dayOfWeek == DayOfWeek.FRIDAY) addToDay = 3;
            else if(dayOfWeek == DayOfWeek.SATURDAY) addToDay = 2;
            return temporal.plus(addToDay, ChronoUnit.DAYS);
        }
    }
}
