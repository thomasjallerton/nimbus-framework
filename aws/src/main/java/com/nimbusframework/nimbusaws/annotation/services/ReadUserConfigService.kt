package com.nimbusframework.nimbusaws.annotation.services

import com.nimbusframework.nimbuscore.persisted.UserConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.nimbusframework.nimbusaws.configuration.CONFIG_FILE


class ReadUserConfigService {

    private val fileService = FileReader()

    fun readUserConfig(): UserConfig {
        val configString = fileService.getFileText(CONFIG_FILE)
        if (configString == "") return UserConfig("nimbus-project")
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue(configString, UserConfig::class.java)
    }
}