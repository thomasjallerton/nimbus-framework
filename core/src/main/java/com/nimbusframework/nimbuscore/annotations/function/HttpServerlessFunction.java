package com.nimbusframework.nimbuscore.annotations.function;

import com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions;
import com.nimbusframework.nimbuscore.annotations.http.ContentEncoding;
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HttpServerlessFunctions.class)
public @interface HttpServerlessFunction {
    HttpMethod method();
    String path();
    int timeout() default 10;
    int memory() default 1024;
    String allowedCorsOrigin() default "";
    String[] allowedCorsHeaders() default {};
    String[] stages() default {};

    /**
     * Returns whether encoded requests should be decoded before the function is called.
     *
     * <p>The request will only be decompressed if the request contained the appropriate 'Content-Encoding' header.
     * The function (not the cloud provider) determines the correct encoding, and performs the decoding. The supported
     * encodings can be found in the {@link ContentEncoding} enum. The request should be base64 encoded.
     */
    boolean enableRequestDecoding() default false;

    /**
     * Returns whether responses should be encoded when serialising the result of the function.
     *
     * <p>The response will only be compressed if the request contained the appropriate 'Accept-Encoding' header.
     * The function (not the cloud provider) determines the correct encoding, and performs the encoding. The supported
     * encodings can be found in the {@link ContentEncoding} enum.
     */
    boolean enableResponseEncoding() default false;
}
