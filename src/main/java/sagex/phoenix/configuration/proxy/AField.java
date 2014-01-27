package sagex.phoenix.configuration.proxy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.ConfigType;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = java.lang.annotation.ElementType.FIELD)
public @interface AField {
    public static final String USE_FIELD_NAME = "";
    public static final String USE_PARENT_GROUP = "";
    public static final String USE_DEFAULT_EDITOR = "";

    String name() default USE_FIELD_NAME;
    String label();
    String description() default "";
    
    /**
     * if fullKey is used, then name is not used
     */
    String fullKey() default USE_PARENT_GROUP;
    
    /**
     * provides a list as a string list "value:label,value1:label1".  Can also use an IOptionFactory class name like,
     * "class:com.myname.MyOptionFactory"
     */
    String list() default "";
    
    /**
     * Used by a setter/getter to know how to separate list items when getting/setting.  Multiple
     * characters can be specified, ie, ";,/", meaning to split on either semi-colon or comma or slash.
     * The first charactar will be used for serialization.
     */
    String listSeparator() default "";
    
    /**
     * Visible expression
     */
    String visible() default "";

    /**
     * Arbitrary hints.  Most likely will be ignore, but can be consumed by UIs to 
     * better handle how to render certain elements. Comma separate.
     * @return
     */
    String hints() default "";
    
    /**
     * Default scope to use for this property
     */
    ConfigScope scope() default ConfigScope.CLIENT;
    
    /**
     * Configuration Type, aligned with the Sage7 Configuration Types, as defined in
     * {@link ConfigType}
     */
    ConfigType type() default ConfigType.TEXT;
}
