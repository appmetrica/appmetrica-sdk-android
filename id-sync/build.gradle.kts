plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-id-sync")
    name.set("AppMetrica SDK Identifiers Synchronization")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.idsync"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
    implementation(project(":network"))
}
