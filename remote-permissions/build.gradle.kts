plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-remote-permissions")
    name.set("AppMetrica SDK Remote permissions")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.remotepermissions"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
}
