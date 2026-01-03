package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbuscore.persisted.userconfig.UserConfig
import javax.annotation.processing.Messager

class UserConfigValidator {

    fun validateUserConfig(userConfig: UserConfig, messager: Messager) {
        if (userConfig.logGroupRetentionInDays != null && !validLogRetentionDays.contains(userConfig.logGroupRetentionInDays)) {
            messager.printMessage(javax.tools.Diagnostic.Kind.ERROR, "Log retention days must be one of $validLogRetentionDays")
        }
    }

    companion object {

        private val validLogRetentionDays = setOf(1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1096, 1827, 2192, 2557, 2922, 3288, 3653)

    }

}
