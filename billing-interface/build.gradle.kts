plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-billing-interface")
    name.set("AppMetrica SDK Billing API")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.billinginterface"
}

dependencies {
    implementation(project(":core-utils"))
}
