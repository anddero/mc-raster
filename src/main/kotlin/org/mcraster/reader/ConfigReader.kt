package org.mcraster.reader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.mcraster.builder.BuildConfig
import java.io.File

object ConfigReader {

    fun readConfig(configDir: String, configFile: String): BuildConfig {
        val buildConfig = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule.Builder().build())
            .readValue<BuildConfig>(src = File("$configDir/$configFile"))

        if (buildConfig.relativeInputDir.isEmpty()) {
            return buildConfig.copy(relativeInputDir = "$configDir/${buildConfig.modelAndWorldName}")
        }
        return buildConfig.copy(relativeInputDir = "$configDir/${buildConfig.relativeInputDir}")
    }

}
