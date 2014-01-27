package sagex.phoenix.metadata.proxy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A SageProperty annotates a method that is reposible for setting/getting a SageTV 
 * property from the wiz.bin by a named value.  It's up to an implementation to
 * decide how to store/retrieve each value.
 * 
 * @author seans
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = java.lang.annotation.ElementType.METHOD)
public @interface SageProperty {
    /**
     * The Key for the Sage Metadata property, ie, "Title", "MediaTitle", etc.
     * @return
     */
    public String value();

    /**
     * If format is true, then MessageFormat.format() will be called on the property passing in the method args as
     * paramters to the key.
     * 
     * For example, if the value() is "/test/{0}/{1}/prop", and the method is called with values, "A", "B", then 
     * the actual name of the sage metadata key will be "/test/A/B/prop"
     * 
     * This can only be used on "get" properties... it does not work for "set" properties
     * 
     * @return
     */
    public boolean format() default false;
    
    /**
     * This is a ListFactory class that is responsible for creating the typed list and it's 
     * list item class, and it's item separator.
     * 
     * List factory must implement {@link IPropertyListFactory}
     * 
     * @return
     */
    public String listFactory() default "";

    /**
     * if set to false then do not allow the null property to be set
     * @return
     */
	public boolean allowNULL() default true;
	
    /**
     * The name of the field that is the unique id for the object.  Used only when 
     * table() is used, so that the data can be joined to the table based on the
     * object's unique id
     * 
     * @return
     */
    public String idfield() default "";
    
    /**
     * The name of the table that will hold the custom field.  idfield() is also required
     * so that a 1-1 join can exist.
     * 
     * @return
     */
    public String table() default "";
}
