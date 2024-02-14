plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ndkcrashes-api")
    name.set("AppMetrica SDK NdkCrashes API")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.ndkcrashesapi"
}

dependencies {
    implementation(project(":logger"))
}
