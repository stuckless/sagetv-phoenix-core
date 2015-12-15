package sagex.phoenix.util;

/**
 * A Pair is simple container containing 2 paired values and types.
 *
 * @param <First>
 * @param <Second>
 * @author seans
 */
public class Pair<First, Second> {
    private First first;
    private Second second;

    public Pair() {
    }

    public Pair(First f, Second s) {
        this.first = f;
        this.second = s;
    }

    public First first() {
        return first;
    }

    public void first(First first) {
        this.first = first;
    }

    public Second second() {
        return second;
    }

    public void second(Second sec) {
        this.second = sec;
    }
}
