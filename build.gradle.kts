import io.appmetrica.analytics.gradle.AppMetricaModulePlugin

plugins {
    id("appmetrica-update-version")
}

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
    return "${prefix}${projectToFlavorMapping.getValue(flavor).capitalize()}${buildType.capitalize()}${suffix.capitalize()}"
}

buildTypes.forEach { buildType ->
    tasks.register("assemble${buildType.capitalize()}") {
        dependsOn(modules.map { it.tasks.named(createTaskName("assemble", it.name, buildType)) })
    }
    tasks.register("publish${buildType.capitalize()}PublicationToMavenLocal") {
        dependsOn(modules.map { it.tasks.named(createTaskName("publish", it.name, buildType, "publicationToMavenLocal")) })
    }
    tasks.register("test${buildType.capitalize()}UnitTest") {
        dependsOn(modules.map { it.tasks.named(createTaskName("test", it.name, buildType, "unitTest")) })
    }
    tasks.register("generate${buildType.capitalize()}JacocoReport") {
        dependsOn(modules.map { it.tasks.named(createTaskName("generate", it.name, buildType, "jacocoReport")) })
    }
}

tasks.register("aarCheck") {
    dependsOn(modules.map { it.tasks.named(createTaskName("aarCheck")) })
}
tasks.register("aarDump") {
    dependsOn(modules.map { it.tasks.named(createTaskName("aarDump")) })
}
