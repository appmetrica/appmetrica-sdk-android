plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-billing-v8")
    name.set("AppMetrica SDK Google Play Services Billing v8 library wrapper")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.billingv8"
}

dependencies {
    implementation(project(":billing-interface"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))

    compileOnly(appMetricaLibs.billingV8)

    testImplementation(appMetricaLibs.billingV8)
}
