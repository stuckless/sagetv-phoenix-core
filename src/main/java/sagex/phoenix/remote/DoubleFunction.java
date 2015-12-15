package sagex.phoenix.remote;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.util.Function;

public class DoubleFunction implements Function<String, Double> {
    public DoubleFunction() {
    }

    @Override
    public Double apply(String in) {
        return NumberUtils.toDouble(in, 0d);
    }
}
