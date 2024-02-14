import io.appmetrica.analytics.gradle.Constants

plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-network-tasks")
    name.set("AppMetrica SDK Network Tasks")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.networktasks"
    defaultConfig {
        buildConfigField("String", "BUILD_NUMBER", "\"${Constants.Library.buildNumber}\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "VERSION_NAME", "\"${Constants.Library.versionName}\"")
        }
        release {
            buildConfigField("String", "VERSION_NAME", "\"${Constants.Library.versionName}\"")
        }
        named("snapshot") {
            buildConfigField("String", "VERSION_NAME", "\"${Constants.Library.versionName}-SNAPSHOT\"")
        }
    }
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":network"))
}
