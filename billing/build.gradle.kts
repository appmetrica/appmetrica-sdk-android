plugins {
    id("appmetrica-module")
    id("appmetrica-proto")
}

publishingInfo {
    baseArtifactId.set("analytics-billing")
    name.set("AppMetrica SDK Billing collecting")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.billing"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

protobuf {
    packageName.set("io.appmetrica.analytics.billing.impl.protobuf")
    protoFile(srcPath = "backend/revenue.proto", years = "2025")
    protoFile(srcPath = "client/autoInappCollectingInfoProto.proto", years = "2025")
    protoFile(srcPath = "client/remoteBillingConfigProtobuf.proto", years = "2025")
}

dependencies {
    implementation(project(":billing-interface"))
    implementation(project(":billing-v6"))
    implementation(project(":billing-v8"))

    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
}
