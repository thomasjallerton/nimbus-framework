package com.nimbusframework.nimbuscore.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.nimbusframework.nimbuscore.CONFIG_FILE
import com.nimbusframework.nimbuscore.persisted.userconfig.UserConfig
import java.nio.file.Paths
import javax.annotation.processing.Messager

class ReadUserConfigService {

    private val fileService = FileReader()

    @JvmOverloads
    fun readUserConfig(projectDir: String? = null): UserConfig {
        val configString = if (!projectDir.isNullOrBlank()) {
            val projectFilePath = Paths.get(projectDir, CONFIG_FILE).toString()
            val text = fileService.getFileText(projectFilePath)
            if (text.isEmpty()) {
                fileService.getFileText(CONFIG_FILE)
            } else {
                text
            }
        } else {
            fileService.getFileText(CONFIG_FILE)
        }

        if (configString.isEmpty()) return UserConfig("nimbus-project")
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue(configString, UserConfig::class.java)
    }
}
