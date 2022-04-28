package com.nimbusframework.nimbuscore.persisted

data class HandlerInformation(
        /**
         * The deployment plugin uses this to determine if the class has changed
         */
        val handlerClassPath: String,
        /**
         * The path of the handler from the entry file
         */
        val handlerPath: String,
        /**
         * The deployment plugin replaces this variable with the deployed file path in S3
         */
        val fileReplacementVariable: String,
        /**
         * The deployment plugin will link the function to this file instead of the default shaded one
         */
        val overrideFileName: String? = null,
        /**
         * The runtime of the handler
         */
        val runtime: String = "java11"
) {
        constructor(): this("", "", "")
}
