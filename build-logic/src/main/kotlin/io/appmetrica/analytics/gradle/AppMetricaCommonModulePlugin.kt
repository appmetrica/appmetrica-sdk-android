package io.appmetrica.analytics.gradle

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BuildType
import io.appmetrica.analytics.gradle.codequality.CodeQualityPlugin
import io.appmetrica.analytics.gradle.jacoco.JacocoPlugin
import io.appmetrica.analytics.gradle.publishing.PublishingInfoBuildTypeExtension
import io.appmetrica.analytics.gradle.publishing.PublishingPlugin
import io.appmetrica.analytics.gradle.test.TestJvmArgsConfigurator
import io.appmetrica.analytics.gradle.test.TestSettingsExtension
import io.appmetrica.analytics.gradle.test.TestSplitPlugin
import io.appmetrica.gradle.aarcheck.AarCheckExtension
import io.appmetrica.gradle.aarcheck.AarCheckPlugin
import io.appmetrica.gradle.aarcheck.agp.aarCheck
import io.appmetrica.gradle.android.plugins.AndroidLibrary
import io.appmetrica.gradle.nologs.NoLogsExtension
import io.appmetrica.gradle.nologs.NoLogsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

class AppMetricaCommonModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply<AndroidLibrary>() // id("io.appmetrica.gradle.android-library")
        project.apply<CodeQualityPlugin>() // id("appmetrica-codequality")
        project.apply<JacocoPlugin>() // id("appmetrica-jacoco")
        project.apply<PublishingPlugin>() // id("appmetrica-publish")
        project.apply<AarCheckPlugin>() // id("io.appmetrica.gradle.aar-check")
        project.apply<NoLogsPlugin>() // id("io.appmetrica.gradle.no-logs")

        project.group = Constants.Library.group

        // Create testSettings extension for test configuration
        val testSettings = project.extensions.create("testSettings", TestSettingsExtension::class.java)
        testSettings.maxParallelForks.convention(4) // default value for all test tasks

        // Apply TestSplitPlugin (uses testSettings.split nested extension)
        project.apply<TestSplitPlugin>() // id("appmetrica-test-split")

        project.configureAndroid()
        project.configureKotlin()
        project.configureAarCheck()
        project.configureNoLogs()
        project.configureTests(testSettings)

        project.dependencies {
            val implementation by project.configurations.getting
            val compileOnly by project.configurations.getting

            implementation("org.jetbrains.kotlin:kotlin-stdlib") // version is equal to plugin version
            compileOnly("androidx.annotation:annotation:${Deps.androidX}")
        }

        project.tasks.withType<KotlinCompile> {
            // https://nda.ya.ru/t/htF7GLnh6cCW4c
            kotlinOptions.suppressWarnings = true
        }
    }

    private fun Project.configureAndroid() {
        configure<LibraryExtension> {
            defaultConfig {
                proguardFiles("proguard/proguard-rules.pro")
                consumerProguardFiles("proguard/consumer-rules.pro")
            }

            buildFeatures {
                buildConfig = true
                aidl = true
            }

            buildTypes {
                debug {
                    isMinifyEnabled = false
                    configure<PublishingInfoBuildTypeExtension> {
                        artifactIdSuffix.set("-debug")
                    }
                }
                release {
                    isMinifyEnabled = true
                    aarCheck.enabled = true
                }
                create("snapshot") {
                    isMinifyEnabled = true
                    (this as BuildType).versionNameSuffix = "-SNAPSHOT"
                }
            }
        }
    }

    private fun Project.configureKotlin() {
        tasks.withType<KotlinCompile> {
            if (name.toLowerCase(Locale.ROOT).contains("releasekotlin")) {
                kotlinOptions {
                    freeCompilerArgs += listOf(
                        "-Xno-call-assertions",
                        "-Xno-receiver-assertions",
                        "-Xno-param-assertions",
                    )
                }
            }
        }
    }

    private fun Project.configureAarCheck() {
        configure<AarCheckExtension> {
            checkDependencies = true
            checkManifest = true
            checkModule = true
            checkPom = true
            checkProguard = true
            forbiddenImports = listOf(
                "io.appmetrica.analytics.coreutils.asserts.DebugAssert",
                "io.appmetrica.analytics.impl.utils.DebugAssert",
                "io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger",
                "io.appmetrica.analytics.ndkcrashes.impl.NativeCrashLogger",
            )
            forbiddenMethods = mapOf(
                "kotlin.jvm.internal.Intrinsics" to listOf(
                    "checkNotNullParameter",
                    "checkNotNullExpressionValue",
                    "checkReturnedValueIsNotNull",
                    "checkExpressionValueIsNotNull",
                    "checkFieldIsNotNull",
                    "checkParameterIsNotNull",
                )
            )
        }
    }

    private fun Project.configureNoLogs() {
        configure<NoLogsExtension> {
            loggerClasses = listOf(
                "DebugAssert",
                "DebugLogger",
                "DebugLogger.INSTANCE",
                "NativeCrashLogger",
            )
            shouldRemoveLogs = { it.buildType.name == "release" }
        }
    }

    private fun Project.configureTests(testSettings: TestSettingsExtension) {
        configure<LibraryExtension> {
            @Suppress("UnstableApiUsage")
            testOptions {
                unitTests {
                    isReturnDefaultValues = true
                    isIncludeAndroidResources = true
                    all {
                        TestJvmArgsConfigurator.configureBaseJvmArgs(it)
                    }
                }
            }

            @Suppress("UnstableApiUsage")
            useLibrary("org.apache.http.legacy")
        }

        dependencies {
            val testCompileOnly by configurations.getting
            val testImplementation by configurations.getting

            testCompileOnly("androidx.annotation:annotation:${Deps.androidX}")

            testImplementation("nl.jqno.equalsverifier:equalsverifier:3.15.2")
            testImplementation("org.skyscreamer:jsonassert:1.5.0")
            testImplementation("io.appmetrica.analytics:common_assertions")
            testImplementation(findProject(":test-utils") ?: "io.appmetrica.analytics:test-utils")
        }

        // Configure Test tasks after evaluation
        // afterEvaluate is necessary because testSettings may be configured
        // by user after plugin application (e.g., in module's build.gradle.kts)
        afterEvaluate {
            tasks.withType<org.gradle.api.tasks.testing.Test> {
                // Apply maxParallelForks from testSettings extension
                if (testSettings.maxParallelForks.isPresent) {
                    maxParallelForks = testSettings.maxParallelForks.get()
                }
            }
        }
    }
}
