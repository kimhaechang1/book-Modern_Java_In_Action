package modernjavainaction.ch07;

import java.util.Spliterator;
import java.util.function.Consumer;

public class WordCounterSpliterator implements Spliterator<Character> {

    private final String string;
    private int currentChar = 0;
    public WordCounterSpliterator(String string){
        this.string = string;
    }
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        // 현재 분할된 String을 소비하는(탐색하는) 메소드
        action.accept(string.charAt(currentChar++));
        return currentChar < this.string.length();
    }

    @Override
    public Spliterator<Character> trySplit() {
        String currentThreadName = Thread.currentThread().getName();

        // 현재 문자열 기준으로 분할하여 새로운 Spliterator를 만드는 메소드
        int currentSize = this.string.length() - currentChar;
        if(currentSize < 10){
             // 충분히 작아졌다면, 더이상 분할이 일어나지 않는다는 의미로 null 반환
            return null;
        }
        for(int splitPos = currentSize / 2 + currentChar;
            // 전체 문자열 중 현재 길이의 중간지점에서 부터
            splitPos < this.string.length();
            splitPos++
        ){
            if(Character.isWhitespace(string.charAt(splitPos))){
                Spliterator<Character> spliterator = new WordCounterSpliterator(
                        string.substring(currentChar, splitPos));
                currentChar = splitPos;
                return spliterator;
            }
        }
        return null;
    }


    @Override
    public long estimateSize() {
        return string.length() - currentChar;
    }

    @Override
    public int characteristics() {
        // ORDERED: 현재 처리해야 하는 문자열의 문자 등장순서가 유의미함을 나타냄
        // SIZED: estimatedSize가 반환하는 값이 정확하다는 의미
        // SUBSIZED: trySplit 으로 생성된 Spliterator도 정확한 크기를 가짐
        // IMMUTABLE: 문자열 자체가 불변이므로 속성이 추가되지 않음
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}
