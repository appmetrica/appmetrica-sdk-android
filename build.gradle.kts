import com.android.build.api.dsl.LibraryExtension
import io.appmetrica.analytics.gradle.AppMetricaModulePlugin
import io.appmetrica.analytics.gradle.Constants
import io.appmetrica.analytics.gradle.publishing.registerAggregateJavadocTask
import io.appmetrica.gradle.publishing.MavenCentralPublishExtension

plugins {
    id("appmetrica-update-version")
    id("appmetrica-public-publish")
    alias(appMetricaLibs.plugins.appMetricaCheckNamespace)
    alias(appMetricaLibs.plugins.appMetricaNoStringDependencies)
}

group = Constants.Library.group
version = Constants.Library.versionName

val modules by lazy {
    subprojects
        .filter { it.plugins.hasPlugin(AppMetricaModulePlugin::class.java) }
        .also { require(it.isNotEmpty()) { "Modules not found" } }
}
val buildTypes = listOf("release", "snapshot", "debug")
val defaultFlavors by lazy { modules.associateWith { it.defaultFlavor() } }

evaluationDependsOnChildren()

registerAggregateTask("ktlintCheck")
registerAggregateTask("detekt")
registerAggregateTask("pmd")

registerAggregateTask("aarCheck")
registerAggregateTask("aarDump")

buildTypes.forEach { buildType ->
    registerAggregateJavadocTask(buildType, defaultFlavors)

    registerAggregateTask("assemble", buildType)
    registerAggregateTask("lint", buildType)
    registerAggregateTask("checkstyle", buildType)
    registerAggregateTask("test", buildType, "UnitTest")
    registerAggregateTask("generate", buildType, "JacocoReport")

    registerAggregateTask("publish", buildType, "PublicationToMavenLocal")
}

configure<MavenCentralPublishExtension> {
    publishToMavenLocalTask.set(tasks.named("publishReleasePublicationToMavenLocal"))
}

fun registerAggregateTask(task: String): TaskProvider<Task> {
    return tasks.register(task) {
        description = "Runs $task for all modules"
        dependsOn(modules.map { it.tasks.named(task) })
    }
}

fun registerAggregateTask(task: String, buildType: String, suffix: String = ""): TaskProvider<Task> {
    val taskName = buildName(task, buildType, suffix)
    return tasks.register(taskName) {
        description = "Runs $taskName for all modules"
        dependsOn(modules.map { it.tasks.named(buildName(task, defaultFlavors[it], buildType, suffix)) })
    }
}

fun Project.defaultFlavor(): String {
    val android = the<LibraryExtension>()
    val defaultFlavors = android.productFlavors.filter { it.isDefault }.groupBy { it.dimension }
    val names = android.flavorDimensions.map { dimension ->
        val defaults = defaultFlavors[dimension].orEmpty()
        require(defaults.size == 1) {
            val listed = defaults.joinToString { it.name }
            "$path has ${defaults.size} default product flavors in dimension '$dimension'" +
                if (listed.isEmpty()) "" else ": $listed"
        }
        defaults.single().name
    }
    return buildName(names)
}

fun buildName(vararg names: String?) = names.joinToString("") { it?.capitalize() ?: "" }.decapitalize()
fun buildName(names: Iterable<String?>) = names.joinToString("") { it?.capitalize() ?: "" }.decapitalize()
fun String.capitalize() = replaceFirstChar { it.uppercase() }
fun String.decapitalize() = replaceFirstChar { it.lowercase() }
