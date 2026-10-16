plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-certificates")
    name.set("AppMetrica SDK Certificates")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.certificates"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
}
