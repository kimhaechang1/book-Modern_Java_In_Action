package modernjavainaction.ch02.Filter;

public class Apple {
    Color color;

    Integer weight;

    public Apple(Color color, Integer weight) {
        this.color = color;
        this.weight = weight;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Apple{" +
                "color=" + color +
                ", weight=" + weight +
                '}';
    }

    public Integer getWeight() {
        return weight;
    }
}
