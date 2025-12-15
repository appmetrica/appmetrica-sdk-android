plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-network-api")
    name.set("AppMetrica SDK Network API")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.networkapi"
}
