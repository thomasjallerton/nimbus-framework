package com.nimbusframework.nimbuscore.annotations.function;

public enum HttpRequestPartLog {
    /** Log the Origin and User-Agent headers */
    SOURCE,
    /** Path parameters */
    PATH_PARAMETERS,
    /** Query string parameters */
    QUERY_STRING_PARAMETERS
}
