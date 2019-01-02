package annotation.services

import annotation.models.persisted.UserConfig
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.databind.ObjectMapper



private const val CONFIG_FILE = "nimbus.yml"
class ReadUserConfigService {

    private val fileService = FileService()

    fun readUserConfig(): UserConfig {
        val configString = fileService.getFileText(CONFIG_FILE)
        if (configString == "") return UserConfig("nimbus-project")
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue(configString, UserConfig::class.java)
    }
}