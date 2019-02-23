package annotation.annotations.function;

import annotation.annotations.persistent.StoreUpdate;

public @interface KeyValueStoreServerlessFunction {
    Class<?> dataModel();
    StoreUpdate method();
    int timeout() default 10;
    int memory() default 1024;
}
