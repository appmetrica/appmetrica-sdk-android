plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-network-legacy")
    name.set("AppMetrica SDK Network Legacy")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.networklegacy"
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":network-api"))
}
