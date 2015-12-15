package sagex.phoenix.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteContext {
    private static InheritableThreadLocal<RemoteContext> context = new InheritableThreadLocal<RemoteContext>();

    public static RemoteContext get() {
        RemoteContext ctx = context.get();
        if (ctx == null) {
            ctx = new RemoteContext();
            context.set(ctx);
        }
        return ctx;
    }

    private Timer timer = new Timer();
    private static Map<String, Object> longTermReferences = new HashMap<String, Object>();

    private int serializeDepth = 0;
    private Map<String, Object> data = new HashMap<String, Object>();

    public int getSerializeDepth() {
        return serializeDepth;
    }

    public void setSerializeDepth(int serializeDepth) {
        this.serializeDepth = serializeDepth;
    }

    public void clean() {
        data.clear();
    }

    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public <T> T getData(String key) {
        return (T) data.get(key);
    }

    public void addReference(final String key, Object value, long expires) {
        longTermReferences.put(key, value);
        if (expires > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    longTermReferences.remove(key);
                }
            }, expires);
        }
    }

    public Object getReference(String key) {
        return longTermReferences.get(key);
    }

    public void clearData() {
        data.clear();
    }

    public void reset() {
        clearData();
        serializeDepth = 0;
    }
}
