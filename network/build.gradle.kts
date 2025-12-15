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
    api(project(":network-api"))

    implementation(project(":core-utils"))
    implementation(project(":logger"))

    runtimeOnly(project(":network-okhttp"))
}
