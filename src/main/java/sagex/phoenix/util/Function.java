package sagex.phoenix.util;

/**
 * Basic 'function' that returns a value based on an input
 * 
 * @author sean
 *
 * @param <In> Input
 * @param <Out> Output
 */
public interface Function<In, Out> {
	public Out apply(In in);
}
