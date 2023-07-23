import com.android.build.gradle.internal.dsl.DefaultConfig
import io.appmetrica.analytics.gradle.Constants
import io.appmetrica.analytics.gradle.configureNdkCrashes
import io.appmetrica.analytics.gradle.enableLogs

plugins {
    id("appmetrica-common-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ndk-crashes")
    name.set("AppMetrica NDK Crashes")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.ndkcrashes"

    compileSdkVersion(Constants.Android.sdkVersion)
    buildToolsVersion(Constants.Android.buildToolsVersion)

    defaultConfig {
        minSdkVersion(Constants.Android.minSdkVersionNative)
        targetSdkVersion(Constants.Android.sdkVersion)

        this as DefaultConfig
        versionCode(Constants.NdkCrashes.versionCode)
        versionName(Constants.NdkCrashes.versionName)

        buildConfigField("String", "VERSION_NAME", "\"${Constants.NdkCrashes.versionName}\"")
    }

    buildTypes {
        debug {
            enableLogs = true
            buildConfigField("boolean", "APPMETRICA_DEBUG", "true")
        }
        release {
            enableLogs = false
            buildConfigField("boolean", "APPMETRICA_DEBUG", "false")
        }
        named("snapshot") {
            enableLogs = true
            buildConfigField("boolean", "APPMETRICA_DEBUG", "true")
        }
    }

    lint {
        disable += "LongLogTag"
    }
}

if (project.property("ndkcrashes.native.enabled").toString().toBoolean()) {
    configureNdkCrashes()
} else {
    tasks.configureEach {
        if (name.startsWith("assemble") || name.startsWith("publish")) {
            doFirst {
                throw GradleException("ndkcrashes.native.enabled is disabled. '.so' files will not be built")
            }
        }
    }
}

dependencies {
    compileOnly(project(":ndkcrashes-api"))
    testImplementation(project(":ndkcrashes-api"))
}
