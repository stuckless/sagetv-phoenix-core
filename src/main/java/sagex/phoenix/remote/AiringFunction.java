package sagex.phoenix.remote;

import org.apache.commons.lang.math.NumberUtils;

import sagex.api.AiringAPI;
import sagex.phoenix.util.Function;

public class AiringFunction implements Function<String, Object> {
    public AiringFunction() {
    }

    @Override
    public Object apply(String in) {
        int sageid = NumberUtils.toInt(in);
        return AiringAPI.GetAiringForID(sageid);
    }
}
