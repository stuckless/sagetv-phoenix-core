package sagex.phoenix.configuration.proxy;

import org.apache.commons.lang.math.NumberUtils;

public class DoubleConverter implements FieldConverter<Double> {
    @Override
    public Double toType(String in) {
        return NumberUtils.toDouble(in);
    }

    @Override
    public String toString(Double in) {
        return in == null ? null : String.valueOf(in);
    }
}
