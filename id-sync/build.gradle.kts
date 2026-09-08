plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-id-sync")
    name.set("AppMetrica SDK Identifiers Synchronization")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.idsync"
}
