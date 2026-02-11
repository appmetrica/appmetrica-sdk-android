plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-location")
    name.set("AppMetrica SDK location API implementation.")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.location"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":gpllibrary"))
    implementation(project(":location-api"))
    implementation(project(":logger"))

    testImplementation(appMetricaLibs.playServicesLocation)
}
