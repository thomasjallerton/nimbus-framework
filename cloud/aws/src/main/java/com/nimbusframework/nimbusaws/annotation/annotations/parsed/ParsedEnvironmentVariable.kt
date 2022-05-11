package com.nimbusframework.nimbusaws.annotation.annotations.parsed

import com.nimbusframework.nimbuscore.annotations.function.EnvironmentVariable

class ParsedEnvironmentVariable(
    val key: String,
    val value: String,
    val testValue: String,
    val stages: Array<String>
): ParsedAnnotation {

    constructor(environmentVariable: EnvironmentVariable): this(
        environmentVariable.key,
        environmentVariable.value,
        environmentVariable.testValue,
        environmentVariable.stages
    )

}
