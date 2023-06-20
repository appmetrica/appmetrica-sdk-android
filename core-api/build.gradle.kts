plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-core-api")
    name.set("AppMetrica SDK Core API")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.coreapi"
}

dependencies {
    implementation(project(":proto"))
}
