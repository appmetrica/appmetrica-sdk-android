plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-gpllibrary")
    name.set("AppMetrica SDK Google Play Location Library wrapper")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.gpllibrary"
}

dependencies {
    compileOnly(appMetricaLibs.playServicesLocation)
    testImplementation(appMetricaLibs.playServicesLocation)

    implementation(project(":logger"))
}
