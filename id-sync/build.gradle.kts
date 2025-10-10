plugins {
    id("appmetrica-module")
    id("appmetrica-proto")
}

publishingInfo {
    baseArtifactId.set("analytics-id-sync")
    name.set("AppMetrica SDK Identifiers Synchronization")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.idsync"
}

protobuf {
    packageName.set("io.appmetrica.analytics.idsync.impl.protobuf")
    protoFile(srcPath = "client/IdSyncProtobuf.proto", years = "2025")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
    implementation(project(":network"))
}
