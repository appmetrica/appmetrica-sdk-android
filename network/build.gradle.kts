plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-network")
    name.set("AppMetrica SDK Network")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.network"
}

dependencies {
    implementation(project(":core-utils"))
}
