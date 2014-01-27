package sagex.phoenix.util;

/**
 * Interface to force clone method to be public
 * 
 * @author seans
 */
public interface PublicCloneable extends Cloneable {
	public Object clone() throws CloneNotSupportedException;
}
