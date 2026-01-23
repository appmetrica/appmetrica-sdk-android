import io.appmetrica.analytics.gradle.AppMetricaModulePlugin
import io.appmetrica.analytics.gradle.Constants

plugins {
    id("appmetrica-update-version")
    id("appmetrica-public-publish")
    alias(libs.plugins.appMetricaCheckNamespace)
}

group = Constants.Library.group
version = Constants.Library.versionName

val modules by lazy { subprojects.filter { it.plugins.hasPlugin(AppMetricaModulePlugin::class.java) } }
val buildTypes = listOf("release", "snapshot", "debug")
val projectToFlavorMapping = mapOf(
    "analytics" to "binaryProd",
).withDefault { "" }

fun createTaskName(
    prefix: String,
    flavor: String = "",
    buildType: String = "",
    suffix: String = ""
): String {
    return "${prefix}${projectToFlavorMapping.getValue(flavor).replaceFirstChar { it.uppercase() }}${buildType.replaceFirstChar { it.uppercase() }}${suffix.replaceFirstChar { it.uppercase() }}"
}

buildTypes.forEach { buildType ->
    tasks.register("assemble${buildType.replaceFirstChar { it.uppercase() }}") {
        dependsOn(modules.map { it.tasks.named(createTaskName("assemble", it.name, buildType)) })
    }
    tasks.register("publish${buildType.replaceFirstChar { it.uppercase() }}PublicationToMavenLocal") {
        dependsOn(modules.map { it.tasks.named(createTaskName("publish", it.name, buildType, "publicationToMavenLocal")) })
    }
    tasks.register("test${buildType.replaceFirstChar { it.uppercase() }}UnitTest") {
        dependsOn(modules.map { it.tasks.named(createTaskName("test", it.name, buildType, "unitTest")) })
    }
    tasks.register("generate${buildType.replaceFirstChar { it.uppercase() }}JacocoReport") {
        dependsOn(modules.map { it.tasks.named(createTaskName("generate", it.name, buildType, "jacocoReport")) })
    }
}

tasks.register("aarCheck") {
    dependsOn(modules.map { it.tasks.named(createTaskName("aarCheck")) })
}
tasks.register("aarDump") {
    dependsOn(modules.map { it.tasks.named(createTaskName("aarDump")) })
}
