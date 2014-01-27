package sagex.phoenix.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a SageTV User Record Store
 *  
 * @author sean
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = java.lang.annotation.ElementType.TYPE)
public @interface UserRecord {
	String value();
}
