plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-proto")
    name.set("AppMetrica SDK Protocol Buffer JavaNano API")
    withJavadoc.set(false)
    checkAllJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.protobuf.nano"
}
