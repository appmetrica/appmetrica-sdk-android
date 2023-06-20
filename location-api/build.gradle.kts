plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-location-api")
    name.set("AppMetrica SDK location API.")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.locationapi"
}

dependencies {
    implementation(project(":core-api"))
}
