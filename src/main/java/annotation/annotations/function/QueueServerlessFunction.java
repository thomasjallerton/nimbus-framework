package annotation.annotations.function;

public @interface QueueServerlessFunction {
    int batchSize();
    String id() default "";
    int timeout() default 10;
    int memory() default 1024;
}