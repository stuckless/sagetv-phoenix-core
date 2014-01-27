package sagex.phoenix.remote;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.util.Function;

public class IntFunction implements Function<String, Integer> {
	public IntFunction() {
	}

	@Override
	public Integer apply(String in) {
		return NumberUtils.toInt(in, 0);
	}
}
