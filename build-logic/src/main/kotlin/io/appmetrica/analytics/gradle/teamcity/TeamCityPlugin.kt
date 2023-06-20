package io.appmetrica.analytics.gradle.teamcity

import org.gradle.api.Plugin
import org.gradle.api.Project

class TeamCityPlugin : Plugin<Project> {
    companion object {
        const val EXTENSION_NAME = "teamcity"
    }

    override fun apply(target: Project) {
        target.extensions.create(EXTENSION_NAME, TeamCityExtension::class.java)
    }
}
