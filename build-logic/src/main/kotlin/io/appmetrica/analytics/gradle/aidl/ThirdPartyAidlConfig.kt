package io.appmetrica.analytics.gradle.aidl

import org.yaml.snakeyaml.Yaml
import java.io.File

data class ThirdPartyAidlConfig(
    val originalPackage: String,
    val targetPackage: String,
) {
    companion object {
        fun read(file: File): ThirdPartyAidlConfig {
            val yaml = Yaml().load<Map<String, String>>(file.readText())
            return ThirdPartyAidlConfig(
                originalPackage = yaml.getValue("originalPackage"),
                targetPackage = yaml.getValue("targetPackage"),
            )
        }
    }
}
