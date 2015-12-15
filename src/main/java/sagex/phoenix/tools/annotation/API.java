package sagex.phoenix.tools.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
public @interface API {
    public String group();

    /**
     * If proxy is set to true, then all methods of the class are simplied
     * proxied, where the first arg of each proxy call is either the the class
     * type, or an object, and the fixed class type is resolved dynamically,
     * using the resolver() value
     *
     * @return
     */
    public boolean proxy() default false;

    /**
     * Resolver is only used if the proxy=true. When a resolver is used, then a
     * second method is created in the api that accepts an Object arg as the
     * first arg. The method implementation will then use the resolver to
     * resolve the object type and then proxy the call, if possible.
     *
     * @return
     */
    public String resolver() default "";

    /**
     * Prefix is used with proxy, to prefix all methods with this value. ie, if
     * the prefix is "Show" and the method is getTitle, then the API method is
     * GetShowTitle
     *
     * @return
     * @deprecated - no longer used, but needed for awhile to build the api
     * tranlation tool
     */
    public String prefix() default "";
}
