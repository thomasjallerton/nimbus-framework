package annotation.annotations.keyvalue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValueStore {
    String tableName() default "";
    Class<?> keyType();
    String keyName() default "PrimaryKey";
    String existingArn() default "";
    String stage() default "dev";
}
