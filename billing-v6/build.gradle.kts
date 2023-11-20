plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-billing-v6")
    name.set("AppMetrica SDK Google Play Services Billing v6 library wrapper")
}

android {
    namespace = "io.appmetrica.analytics.billingv6"
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":billing-interface"))

    compileOnly("com.android.billingclient:billing:6.0.1")

    testImplementation("com.android.billingclient:billing:6.0.1")
}
