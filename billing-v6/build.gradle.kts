plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-billing-v6")
    name.set("AppMetrica SDK Google Play Services Billing v6 library wrapper")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.billingv6"
}

dependencies {
    implementation(project(":billing-interface"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))

    compileOnly(appMetricaLibs.billingV6)

    testImplementation(appMetricaLibs.billingV6)
}
