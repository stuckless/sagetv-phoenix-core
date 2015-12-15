package sagex.phoenix.util;

public class ElapsedTimer {
    private long start;
    private long end;

    public ElapsedTimer() {
        start();
    }

    public long start() {
        start = System.currentTimeMillis();
        return start;
    }

    public long end() {
        end = System.currentTimeMillis();
        return end;
    }

    public long delta() {
        return end() - start;
    }
}
