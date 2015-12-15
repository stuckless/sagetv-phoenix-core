package sagex.phoenix.configuration.proxy;

import org.apache.commons.lang.math.NumberUtils;

public class FloatConverter implements FieldConverter<Float> {
    @Override
    public Float toType(String in) {
        return NumberUtils.toFloat(in);
    }

    @Override
    public String toString(Float in) {
        return in == null ? null : String.valueOf(in);
    }
}
