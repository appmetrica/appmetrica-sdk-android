plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-reporter-extension")
    name.set("AppMetrica SDK Reporter Extension")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.reporterextension"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
}
