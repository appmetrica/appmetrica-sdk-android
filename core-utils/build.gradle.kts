plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-core-utils")
    name.set("AppMetrica SDK Core utils")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.coreutils"
    lint {
        disable += "GradleDependency"
    }
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":location-api"))
    implementation(project(":logger"))
    implementation(project(":proto"))
}
