package sagex.phoenix.util;

/**
 * Basic 'function' that returns a value based on an input
 *
 * @param <In>  Input
 * @param <Out> Output
 * @author sean
 */
public interface Function<In, Out> {
    public Out apply(In in);
}
