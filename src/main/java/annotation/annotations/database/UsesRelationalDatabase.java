package annotation.annotations.database;

public @interface UsesRelationalDatabase {
    Class<?> dataModel();
    String stage() default "dev";
}
