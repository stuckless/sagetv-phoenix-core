package sagex.phoenix.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to use on a method of class that will accept a PhoenixEvent, which
 * is proxy for the SageTV event system.
 * 
 * Methods must have a signature of (String, Map) which is the EventName,
 * EventArgs
 * 
 * @author sean
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = java.lang.annotation.ElementType.METHOD)
public @interface PhoenixEvent {
	String value();
}
