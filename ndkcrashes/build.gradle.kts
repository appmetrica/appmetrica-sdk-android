import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.DefaultConfig
import io.appmetrica.analytics.gradle.Constants
import io.appmetrica.analytics.gradle.configureNdkCrashes

plugins {
    id("com.android.library")
    id("appmetrica-publish")
}

group = Constants.NdkCrashes.group

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
    }

    buildTypes {
        debug {}
        release {}
        create("snapshot") {
            this as BuildType
            versionNameSuffix = "-SNAPSHOT"
        }
    }
}

if (project.property("ndkcrashes.native.enabled").toString().toBoolean()) {
    configureNdkCrashes()
} else {
    tasks.configureEach {
        if (name.startsWith("assemble")) {
            doFirst {
                throw GradleException("ndkcrashes.native.enabled is disabled. '.so' files will not be built")
            }
        }
    }
}
