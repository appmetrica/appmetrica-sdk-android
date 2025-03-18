plugins {
    id("appmetrica-module")
    id("appmetrica-proto")
}

publishingInfo {
    baseArtifactId.set("analytics-screenshot")
    name.set("AppMetrica SDK Screenshot listener")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.screenshot"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

protobuf {
    packageName.set("io.appmetrica.analytics.screenshot.impl.protobuf")
    protoFile(srcPath = "client/remoteScreenshotConfigProtobuf.proto", years = "2024")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
}
