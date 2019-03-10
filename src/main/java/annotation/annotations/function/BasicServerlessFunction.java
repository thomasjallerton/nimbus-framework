package annotation.annotations.function;

import annotation.annotations.function.repeatable.BasicServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(BasicServerlessFunctions.class)
public @interface BasicServerlessFunction {
    String cron() default "";
    int timeout() default 10;
    int memory() default 1024;
    String stage() default "dev";
}
