package grails.plugin.mongogee;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class referring to particular environment (@{@link grails.util.Environment})
 *
 * @author binle
 * @since 10/07/2017
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeEnv {
    /**
     * Value that provide a string value of Grails runtime environment such as 'development', 'test', and 'production'
     * If not set, then the annotation is ignored.
     *
     * @return value
     */
    String value() default "";
}
