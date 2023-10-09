package io.appmetrica.analytics.gradle.jacoco

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import io.appmetrica.analytics.gradle.teamcity.TeamCityPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

class JacocoPlugin : Plugin<Project> {

    companion object {
        const val JACOCO_TASK_GROUP = "jacoco"
    }

    override fun apply(project: Project) {
        project.apply<JacocoPlugin>() // id("jacoco")
        project.apply<TeamCityPlugin>() // id("appmetrica-teamcity")

        val extension = project.extensions.create<JacocoSettingsExtension>("jacocoSettings")

        project.configure<JacocoPluginExtension> {
            toolVersion = "0.8.7" // https://github.com/jacoco/jacoco/releases
        }

        project.plugins.withId("com.android.library") {
            project.the<LibraryExtension>().libraryVariants.configureEach {
                project.registerJacocoTaskForVariant(extension, this)
            }
        }

        project.tasks.withType<Test> {
            reports {
                junitXml.required.set(true)
            }
            configure<JacocoTaskExtension> {
                // without this coverage does not work
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }

    fun Project.registerJacocoTaskForVariant(
        extension: JacocoSettingsExtension,
        variant: LibraryVariant
    ) {
        tasks.register<JacocoReport>("generate${variant.name.capitalize(Locale.ROOT)}JacocoReport") {
            val testTask = tasks.named<Test>("test${variant.name.capitalize(Locale.ROOT)}UnitTest").get()
            val kotlinTask = tasks.named<KotlinCompile>("compile${variant.name.capitalize(Locale.ROOT)}Kotlin").get()

            group = JACOCO_TASK_GROUP
            description = "Generate Jacoco coverage reports after running tests for ${variant.name}"

            // jacocoRootReport doesn"t work if some subprojects don"t have any tests at all
            // because this causes the onlyIf of JacocoReport to be false.
            onlyIf { true }

            reports {
                xml.required.set(true)
                html.required.set(true)
            }

            classDirectories.from(fileTree(variant.javaCompileProvider.get().destinationDirectory.get()) {
                exclude(extension.exclude.get())
            })
            classDirectories.from(fileTree(kotlinTask.destinationDir) {
                exclude(extension.exclude.get())
            })

            sourceDirectories.from(variant.sourceSets.map { it.javaDirectories })
            executionData.from(testTask.the<JacocoTaskExtension>().destinationFile)

            dependsOn(testTask)
        }
    }
}
