package io.appmetrica.analytics.gradle.codequality

import com.android.build.gradle.LibraryExtension
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Bundling
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import java.util.Locale

class CodeQualityPlugin : Plugin<Project> {

    companion object {
        const val CODEQUALITY_TASK_GROUP = "verification"
        val DEFAULT_EXCLUDE_CLASSES = listOf("**/R.java", "**/BuildConfig.java")
    }

    override fun apply(project: Project) {
        project.apply<CheckstylePlugin>() // id("checkstyle")
        project.apply<DetektPlugin>() // id("io.gitlab.arturbosch.detekt")

        val extension = project.extensions.create<CodeQualityExtension>("codequality")
        extension.configDir.convention(project.rootProject.layout.projectDirectory.dir("codequality"))

        val codequalityTasks = mutableListOf(
            configureCheckstyleAndGetRootTask(project, extension),
            configureLintTaskAndGetRootTask(project),
            configureDetektAndGetRootTask(project, extension),
        )
        project.plugins.withType<KotlinBasePluginWrapper> {
            codequalityTasks += configureKtLintTaskAndGetRootTask(project)
        }

        project.tasks.named("check").configure {
            dependsOn(codequalityTasks)
        }
    }

    private fun configureDetektAndGetRootTask(
        project: Project,
        extension: CodeQualityExtension
    ): TaskProvider<out Task> {
        project.extensions.getByType(DetektExtension::class.java).apply {
            this.buildUponDefaultConfig = false
            this.config.setFrom(
                extension.configDir.file("detekt.yml")
            )
        }

        return project.tasks.named("detekt")
    }

    private fun configureCheckstyleAndGetRootTask(
        project: Project,
        extension: CodeQualityExtension
    ): TaskProvider<out Task> {
        project.configure<CheckstyleExtension> {
            toolVersion = "8.11" // https://github.com/checkstyle/checkstyle/releases
            configDirectory.set(extension.configDir)
            isShowViolations = true
            isIgnoreFailures = false
        }

        val checkstyleTask = project.tasks.register("checkstyle") {
            group = CODEQUALITY_TASK_GROUP
            description = "Run Checkstyle analysis for all classes"
        }

        project.plugins.withId("com.android.library") {
            project.the<LibraryExtension>().libraryVariants.configureEach {
                val variant = this
                val variants = listOfNotNull(variant, variant.testVariant, variant.unitTestVariant)
                val checkstyleVariantTask = project.tasks.register<Checkstyle>("checkstyle${name.capitalize(Locale.ROOT)}") {
                    group = CODEQUALITY_TASK_GROUP
                    description = "Run Checkstyle analysis for ${variant.name} classes"
                    classpath = project.files(variants.map { it.javaCompile.classpath })
                    source(variants.map { it.javaCompile.inputs.files })
                    exclude(DEFAULT_EXCLUDE_CLASSES)
                    exclude(extension.exclude.get())
                }

                checkstyleTask.configure { dependsOn(checkstyleVariantTask) }
            }
        }

        return checkstyleTask
    }

    private fun configureLintTaskAndGetRootTask(
        project: Project,
    ): TaskProvider<out Task> {
        val lintTask = project.tasks.named("lint") {
            group = CODEQUALITY_TASK_GROUP
            description = "Run Lint analysis for all classes"
        }

        return lintTask
    }

    private fun configureKtLintTaskAndGetRootTask(
        project: Project,
    ): TaskProvider<out Task> {
        val ktlint by project.configurations.creating

        project.dependencies {
            // https://github.com/pinterest/ktlint/releases
            ktlint("com.pinterest:ktlint:0.43.2") {
                attributes {
                    attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named<Bundling>(Bundling.EXTERNAL))
                }
            }
            ktlint(project.findProject(":test-utils") ?: "io.appmetrica.analytics:test-utils")
        }

        return project.tasks.register<JavaExec>("ktlint") {
            group = CODEQUALITY_TASK_GROUP
            description = "Check Kotlin code style"
            classpath = ktlint
            mainClass.set("com.pinterest.ktlint.Main")
            args(
                "src/**/*.kt",
                "--reporter=html,output=${project.buildDir}/reports/ktlint-report.html"
            )
        }
    }
}
