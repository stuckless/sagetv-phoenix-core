package sagex.phoenix.util;

public class Utils {
	/**
	 * Simple Function that returns the non null object
	 * 
	 * @param <T>
	 * @param o1
	 * @param o2
	 * 
	 * @return the object that is not null
	 */
	public static final <T> T returnNonNull(T o1, T o2) {
		return (o1 == null) ? o2 : o1;
	}

	/**
	 * Simple function the return a string value of the object (ie, toString())
	 * but if the object is null, then it only return an emptry string (ie non
	 * null).
	 * 
	 * @param <T>
	 * @param o
	 * @return
	 */
	public static final <T> String toStringNonNull(T o) {
		return (o == null) ? "" : String.valueOf(o);
	}
}