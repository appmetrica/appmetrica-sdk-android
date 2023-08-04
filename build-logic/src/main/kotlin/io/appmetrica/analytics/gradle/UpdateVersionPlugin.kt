package io.appmetrica.analytics.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class UpdateVersionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        registerUpdateRobolectricTask(project)
    }

    private fun registerUpdateRobolectricTask(project: Project) {
        project.tasks.register("updateRobolectricSdk") {
            doLast {
                project.subprojects.forEach { subproject ->
                    val testsDir = File(subproject.projectDir, "src/test")
                    if (testsDir.exists()) {
                        val testResourcesDir = File(testsDir, "resources")
                        testResourcesDir.mkdir()
                        val robolectricFile = File(testResourcesDir, "robolectric.properties")
                        robolectricFile.writeText("""
                            # DON'T CHANGE IT. Change property 'robolectricSdk' in https://nda.ya.ru/t/XCfMpTqZ6RARA7 and run updateRobolectricSdk task
                            sdk=${Constants.robolectricSdk}
                            manifest=--none
                        """.trimIndent() + "\n")
                    }
                }
            }
        }
    }
}
