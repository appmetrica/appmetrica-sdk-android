package io.appmetrica.analytics.gradle

import com.android.build.api.dsl.BuildType
import com.android.build.gradle.LibraryExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import java.util.Locale

private val nativeBuildTypes = mapOf(
    "debug" to "debug",
    "release" to "release",
    "snapshot" to "release",
)
private val crashpadArchs = mapOf(
    "armeabi-v7a" to "arm",
    "arm64-v8a" to "arm64",
    "x86" to "x86",
    "x86_64" to "x64",
)

private val Project.crashpadSourceDir: Directory
    get() = project.layout.projectDirectory.dir("native/crashpad/crashpad")
private val Project.crashpadRootBuildDir: Directory
    get() = project.layout.buildDirectory.dir("crashpad").get()

var BuildType.enableLogs: Boolean
    get() = extra["appmetrica.enable.logs"] as? Boolean ?: false
    set(value) {
        extra["appmetrica.enable.logs"] = value
    }

fun Project.configureNdkCrashes() {
    configureNative()
    configureCrashpad()
}

private fun Project.crashpadNinjaDir(nativeBuildType: String): Directory =
    crashpadRootBuildDir.dir("ninja").dir(nativeBuildType)

private fun Project.crashpadOutDir(nativeBuildType: String): Directory =
    crashpadRootBuildDir.dir("out").dir(nativeBuildType)

private fun Project.configureNative() {
    configure<LibraryExtension> {
        ndkVersion = "28.2.13676358"
        externalNativeBuild {
            // https://developer.android.com/ndk/guides/cmake
            cmake {
                version = "3.31.6"
                path = file("src/main/cpp/CMakeLists.txt")
            }
        }

        buildTypes.configureEach {
            val nativeBuildType = nativeBuildTypes[name]
            requireNotNull(nativeBuildType) { "Unknown native build type for $name" }

            sourceSets.named(name) {
                jniLibs.srcDir(crashpadOutDir(nativeBuildType))
            }
            externalNativeBuild {
                cmake {
                    arguments("-DCRASHPAD_SOURCE_DIR:STRING=${crashpadSourceDir}")
                    arguments("-DCRASHPAD_BUILD_DIR:STRING=${crashpadNinjaDir(nativeBuildType)}")
                    if (enableLogs) {
                        cppFlags("-DAPPMETRICA_DEBUG")
                    }
                }
            }
        }

        afterEvaluate {
            buildTypes.forEach { buildType ->
                val nativeBuildType = nativeBuildTypes[buildType.name]
                requireNotNull(nativeBuildType) { "Unknown native build type for ${buildType.name}" }

                tasks.named("merge${buildType.name.capitalized()}JniLibFolders").configure {
                    crashpadArchs.values.forEach {
                        dependsOn(tasks.named("build${nativeBuildType.capitalized()}${it.capitalized()}Crashpad"))
                    }
                }
            }
        }
    }
}

private fun isPrimitive(obj: Any): Boolean {
    return obj::class.javaPrimitiveType != null
}

private fun Project.configureCrashpad() {
    val crashpadTaskGroup = "crashpad"
    val prepareGn = prepareBin("gn", project.layout.projectDirectory.dir("native/crashpad/buildtools/linux64"))
    val prepareNinja = prepareBin("ninja", project.layout.projectDirectory.dir("native/crashpad/crashpad/third_party/ninja/linux"))
    nativeBuildTypes.values.distinct().forEach { nativeBuildType ->
        val nativeBuildTypeCapitalized = nativeBuildType.capitalize(Locale.ROOT)
        val generateAllNinja = tasks.register("generate${nativeBuildTypeCapitalized}Ninja") {
            group = crashpadTaskGroup
            description = "Generate ninja configs for $nativeBuildType for all arch"
        }
        val buildAllNinja = tasks.register("build${nativeBuildTypeCapitalized}Ninja") {
            group = crashpadTaskGroup
            description = "Build ninja for $nativeBuildType for all arch"
        }
        val buildAllCrashpad = tasks.register("build${nativeBuildTypeCapitalized}Crashpad") {
            group = crashpadTaskGroup
            description = "Build crashpad for $nativeBuildType for all arch"
        }
        // TODO: exclude filter apis
        crashpadArchs.forEach { (ndkArch, crashpadArch) ->
            val variantName = "${nativeBuildType}${crashpadArch.capitalize(Locale.ROOT)}"
            val variantNameCapitalized = variantName.capitalize(Locale.ROOT)
            val ninjaDir = crashpadNinjaDir(nativeBuildType).dir(ndkArch)
            val generateNinja = tasks.register<Exec>("generate${variantNameCapitalized}Ninja") {
                group = crashpadTaskGroup
                description = "Generate ninja configs for $nativeBuildType with $ndkArch arch"
                workingDir = crashpadSourceDir.asFile
                val args = mapOf(
                    "android_api_level" to 21,
                    "android_ndk_root" to project.the<LibraryExtension>().ndkDirectory.canonicalPath,
                    "is_debug" to (nativeBuildType == "debug"),
                    "target_cpu" to crashpadArch,
                    "target_os" to "android",
                ).map { "${it.key}=${if (isPrimitive(it.value)) it.value else "\"${it.value}\""}" }
                commandLine("gn", "gen", ninjaDir, "--args=${args.joinToString(" ")}")
                dependsOn(prepareGn)
            }
            generateAllNinja.configure { dependsOn(generateNinja) }
            val buildNinja = tasks.register<Exec>("build${variantNameCapitalized}Ninja") {
                group = crashpadTaskGroup
                description = "Build ninja for ${nativeBuildType} with ${ndkArch} arch"
                workingDir = crashpadSourceDir.asFile
                commandLine("ninja", "-C", ninjaDir, "crashpad_handler_named_as_so")
                dependsOn(prepareNinja, generateNinja)
            }
            buildAllNinja.configure { dependsOn(buildNinja) }
            val buildCrashpad = tasks.register<Copy>("build${variantNameCapitalized}Crashpad") {
                group = crashpadTaskGroup
                description = "Build crashpad for ${nativeBuildType} with ${ndkArch} arch"
                into(crashpadOutDir(nativeBuildType).dir(ndkArch))
                from(ninjaDir.file("libcrashpad_handler.so"))
                rename("libcrashpad_handler.so", "libappmetrica_crashpad_handler.so")
                dependsOn(buildNinja)
            }
            buildAllCrashpad.configure { dependsOn(buildCrashpad) }
        }
    }
    // auto build crashpad before build self so libs
    the<LibraryExtension>().libraryVariants.configureEach {
        val variant = this
        val nativeBuildType = nativeBuildTypes[variant.buildType.name]
        requireNotNull(nativeBuildType) { "Unknown native build type for ${buildType.name}" }
        val taskName = "buildCMake${if (nativeBuildType == "debug") "Debug" else "RelWithDebInfo"}"
        crashpadArchs.forEach { (ndkArch, crashpadArch) ->
            tasks.configureEach {
                if (name.matches("${taskName}\\[${ndkArch}].*".toRegex())) {
                    dependsOn(tasks.named(
                        "build${nativeBuildType.capitalize(Locale.ROOT)}${crashpadArch.capitalize(Locale.ROOT)}Crashpad"
                    ))
                }
            }
        }
    }
}

private fun Project.prepareBin(name: String, dir: Directory): TaskProvider<Copy> {
    return tasks.register<Copy>("prepare${name.replaceFirstChar { it.uppercaseChar() }}") {
        onlyIf { OperatingSystem.current().isLinux }
        into(dir)
        from(when {
            Os.isArch("arm64") -> dir.file("$name-arm64")
            Os.isArch("amd64") -> dir.file("$name-amd64")
            else -> error("Unknown arch ${System.getProperty("os.arch")}")
        })
        rename { name }
        doFirst {
            logger.info("Detect os ${System.getProperty("os.name")}")
            logger.info("Detect arc ${System.getProperty("os.arc")}")
        }
    }
}
