package sagex.phoenix.lucene.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface LuceneIndexable {
	public boolean store() default false;
	public boolean index() default true;
	public boolean fulltextsearch() default false;
}
