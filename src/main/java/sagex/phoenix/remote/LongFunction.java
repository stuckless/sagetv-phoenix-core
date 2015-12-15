package sagex.phoenix.remote;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.util.Function;

public class LongFunction implements Function<String, Long> {
    public LongFunction() {
    }

    @Override
    public Long apply(String in) {
        return NumberUtils.toLong(in, 0);
    }
}
