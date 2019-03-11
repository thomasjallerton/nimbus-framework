package annotation.annotations.function;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsesBasicServerlessFunctionClient {
    String[] stages() default {NimbusConstants.stage};
}
