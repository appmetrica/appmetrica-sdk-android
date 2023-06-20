package io.appmetrica.analytics.gradle.teamcity

import org.gradle.api.logging.Logging

abstract class TeamCityExtension {
    private val logger = Logging.getLogger("TeamCity")

    fun message(name: String, vararg attributes: Pair<String, Any>) {
        logger.quiet("##teamcity[${name} ${attributes.joinToString(" ") { "${it.first}='${it.second}'" }}]")
    }

    fun buildStatisticValue(key: String, value: Any) {
        message("buildStatisticValue", "key" to key, "value" to value)
    }

    fun buildStatus(text: String) {
        message("buildStatus", "text" to text)
    }

    fun appendToBuildStatus(text: String) {
        buildStatus("{build.status.text}; $text")
    }
}
