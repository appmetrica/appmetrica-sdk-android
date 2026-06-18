package io.appmetrica.analytics.gradle.publishing

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import io.appmetrica.analytics.gradle.AppMetricaModulePlugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.util.Locale

fun Project.registerAggregateJavadocTask(
    buildType: String,
    projectToFlavor: Map<String, String> = emptyMap(),
): TaskProvider<Javadoc> {
    val capBuildType = buildType.replaceFirstChar { it.uppercase(Locale.ROOT) }
    val task = tasks.register<Javadoc>("aggregate${capBuildType}Javadoc") {
        group = "documentation"
        description = "Aggregate Javadoc across all SDK modules for $buildType"
        title = "AppMetrica SDK $capBuildType API"
        options.encoding = "UTF-8"
        setDestinationDir(
            layout.buildDirectory
                .dir("aggregate-javadoc/$buildType")
                .get()
                .asFile
        )
        exclude("**/impl/**")
        exclude("**/internal/**")
    }

    subprojects {
        plugins.withType<AppMetricaModulePlugin> {
            afterEvaluate {
                val publishingInfo = extensions.findByType<PublishingInfoExtension>() ?: return@afterEvaluate
                if (!publishingInfo.withJavadoc.get()) return@afterEvaluate

                val android = the<LibraryExtension>()
                val flavor = projectToFlavor[name].orEmpty()
                val variantName = if (flavor.isEmpty()) {
                    buildType
                } else {
                    "$flavor$capBuildType"
                }
                val variant = android.libraryVariants.firstOrNull { it.name == variantName }
                    ?: return@afterEvaluate
                task.configure { contributeVariant(variant, android) }
            }
        }
    }

    return task
}

private fun Javadoc.contributeVariant(variant: LibraryVariant, android: LibraryExtension) {
    source(
        project.files(variant.sourceSets.flatMap { it.javaDirectories }).asFileTree.matching {
            include("**/*.java")
        }
    )
    classpath = classpath +
        project.files(variant.javaCompile.outputs.files) +
        project.files("${android.sdkDirectory.path}/platforms/${android.compileSdkVersion}/android.jar") +
        variant.getCompileClasspath(null)
    dependsOn(variant.javaCompileProvider)
}
