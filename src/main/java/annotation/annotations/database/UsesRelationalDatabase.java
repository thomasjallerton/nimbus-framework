package annotation.annotations.database;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesRelationalDatabases.class)
public @interface UsesRelationalDatabase {
    Class<?> dataModel();
    String[] stages() default {NimbusConstants.stage};
}
