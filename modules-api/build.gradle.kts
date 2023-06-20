plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-modules-api")
    name.set("AppMetrica SDK Modules API")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.modulesapi"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":location-api"))
    implementation(project(":proto"))
}
