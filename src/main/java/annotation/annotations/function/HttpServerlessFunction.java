package annotation.annotations.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface HttpServerlessFunction {
    public String method();
    public String path();
    int timeout() default 5;
    int memory() default 1024;
}
