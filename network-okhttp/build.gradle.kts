plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-network-okhttp")
    name.set("AppMetrica SDK Network OkHttp")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.networkokhttp"
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":network-api"))

    implementation(appMetricaLibs.okhttp)

    testImplementation(appMetricaLibs.okhttpMockwebserver)
}
