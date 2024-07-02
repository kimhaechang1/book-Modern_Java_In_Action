package modernjavainaction.ch07;


public class WordCounter {

    // 자바에는 여러가지 다형의 상태를 표현하는 자료구조가 없으므로 클래스로 래핑한다.

    private final int counter;
    private final boolean lastSpace;

    public WordCounter(int counter, boolean lastSpace){
        this.counter = counter;
        this.lastSpace = lastSpace;
    }

    public WordCounter accumulator(Character c){
        if(Character.isWhitespace(c)){
            return lastSpace ?
                    this :
                    new WordCounter(counter, true);
        } else {
            return lastSpace ?
                    new WordCounter(counter + 1, false) :
                    this;
        }
    }
    public WordCounter combine(WordCounter wordCounter){
        return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
    }

    public int getCounter(){
        return counter;
    }
}
