package io.appmetrica.analytics.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

private val VERSION_REG = Regex("[0-9]+\\.[0-9]+\\.[0-9]+(-RC)?")

class UpdateVersionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("updateVersion") {
            doLast {
                val version = project.properties["version"].toString()
                require(version.matches(VERSION_REG)) {
                    "Failed version format: '$version'. Version should be in format '${VERSION_REG}'. " +
                        "Use ./gradlew updateVersion -Pversion=NEW_VERSION"
                }

                project.updateConstants(version)
                project.updateGradleConstants(version)
                project.updateSdkDataClass(version)
                project.updateVersionInCi(version)
                project.updateVersionInAutotests(version)
            }
        }
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

    private fun Project.updateConstants(version: String) {
        val filePath = "build-logic/src/main/kotlin/io/appmetrica/analytics/gradle/Constants.kt"
        updateVersion(filePath, ".*object Library.*\\n.*const val versionName = \"", "\"", version)
    }

    private fun Project.updateGradleConstants(version: String) {
        val filePath = "../appmetrica-sample/constants.gradle"
        val versionCode = getVersionCodeForSample(version)
        updateVersion(filePath, ".*sampleVersion = \"", "\"", version)
        updateVersion(filePath, ".*appmetricaSnapshotVersion = \"", "\"", version)
        updateVersionCode(filePath, ".*sampleVersionCodePrefix = \"", "\"", versionCode)
    }

    private fun Project.updateSdkDataClass(version: String) {
        val filePath = "analytics/src/main/java/io/appmetrica/analytics/impl/SdkData.java"
        updateVersion(filePath, ".*CURRENT_VERSION_NAME_FOR_MAPPING = \"", "\";", version)
    }

    private fun Project.updateVersionInCi(version: String) {
        val filePath = "../a.yaml"
        updateVersion(filePath, ".*appmetrica-sdk/r-", "-.*", version)
    }

    private fun Project.updateVersionInAutotestsVersionProvider(version: String, filePath: String) {
        val versionCode = getVersionCodeForAutotests(version)
        updateVersion(filePath, ".*getDevVersionName\\(\"", "\"\\)", version)
        updateVersionCode(filePath, ".*devVersion\\(", ", \"dev\".*", versionCode)
    }

    private fun Project.updateVersionInAutotests(version: String) {
        updateVersionInAutotestsVersionProvider(
            version,
            "../../../autotests/appmetrica-sdk/framework/appmetrica.build/src/main/java/com/yandex/appmetrica/autotests/android/NativeAndroidMainVersionsProvider.kt"
        )
        updateVersionInAutotestsVersionProvider(
            version,
            "../../../autotests/appmetrica-sdk/framework/appmetrica.build/src/main/java/com/yandex/appmetrica/autotests/android/NativeAndroidYandexVersionsProvider.kt"
        )
    }

    private fun getVersionCodeForAutotests(version: String): String {
        val items = version.substringBefore("-").split(".")
        return (listOf(items[0]) + items.subList(1, 3).map { it.padStart(3, '0') }).joinToString("_")
    }

    private fun getVersionCodeForSample(version: String): String {
        val items = version.substringBefore("-").split(".")
        return (listOf(items[0]) + items.subList(1, 3).map { it.padStart(3, '0') }).joinToString("")
    }

    private fun Project.updateFile(filePath: String, beforeReg: String, replaceReg: String, afterReg: String, newText: String) {
        val file = file(filePath)
        check(file.exists()) { "Not found file ${file.canonicalPath}" }

        val regex = Regex(
            "($beforeReg)$replaceReg($afterReg)",
            setOf(RegexOption.MULTILINE)
        )
        val text = file.readText()
        check(regex.findAll(text).toList().isNotEmpty()) { "Not found regex $regex in file $filePath" }
        file.writeText(text.replace(regex) { "${it.groupValues[1]}$newText${it.groupValues.last()}" })
    }

    private fun Project.updateVersion(filePath: String, beforeReg: String, afterReg: String, version: String) {
        updateFile(filePath, beforeReg, VERSION_REG.toString(), afterReg, version)
    }

    private fun Project.updateVersionCode(filePath: String, beforeReg: String, afterReg: String, version: String) {
        updateFile(filePath, beforeReg, "[0-9_]+", afterReg, version)
    }
}
