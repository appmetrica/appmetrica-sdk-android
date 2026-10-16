plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-apphud")
    name.set("AppMetrica SDK Apphud integration module")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.apphud"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
    implementation(project(":proto"))
}
