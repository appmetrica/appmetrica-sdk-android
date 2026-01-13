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

    compileOnly("com.android.billingclient:billing:8.0.0")

    testImplementation("com.android.billingclient:billing:8.0.0")
}
