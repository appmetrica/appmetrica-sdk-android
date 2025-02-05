plugins {
    id("appmetrica-module")
    id("appmetrica-proto")
}

publishingInfo {
    baseArtifactId.set("analytics-apphud")
    name.set("AppMetrica SDK Apphud integration module")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.apphud"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

protobuf {
    packageName.set("io.appmetrica.analytics.apphud.impl.protobuf")
    protoFile(srcPath = "client/remoteApphudConfigProtobuf.proto", years = "2024")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
    implementation(project(":proto"))

    implementation("com.apphud:ApphudSDK-Android:2.8.4")
}
