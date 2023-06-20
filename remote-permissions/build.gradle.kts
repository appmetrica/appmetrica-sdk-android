plugins {
    id("appmetrica-module")
    id("appmetrica-proto")
}

publishingInfo {
    baseArtifactId.set("analytics-remote-permissions")
    name.set("AppMetrica SDK Remote permissions")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.remotepermissions"
}

protobuf {
    packageName.set("io.appmetrica.analytics.remotepermissions.impl.protobuf")
    protoFile(srcPath = "client/remotePermissionsProtobuf.proto", years = "2023")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":modules-api"))
}
