plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-logger")
    name.set("AppMetrica SDK Logger")
}

android {
    namespace = "io.appmetrica.analytics.logger.appmetrica"
}

dependencies {
    api(project(":common-logger"))
}
