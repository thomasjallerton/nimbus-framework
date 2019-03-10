package annotation.annotations.function.repeatable;

import annotation.annotations.function.DocumentStoreServerlessFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentStoreServerlessFunctions {
    DocumentStoreServerlessFunction[] value();
}
