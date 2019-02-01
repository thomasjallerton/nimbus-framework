package annotation.annotations.function;

public @interface QueueServerlessFunction {
    public int batchSize();
    int timeout() default 10;
    int memory() default 1024;
}