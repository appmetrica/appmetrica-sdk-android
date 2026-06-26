plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-product-flow")
    name.set("AppMetrica SDK Product Flow")
    withJavadoc.set(true)
}

android {
    namespace = "io.appmetrica.analytics.productflow"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
}
