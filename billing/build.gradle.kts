plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
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

dependencies {
    implementation(project(":billing-interface"))
    implementation(project(":billing-v6"))
    implementation(project(":billing-v8"))

    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
}
