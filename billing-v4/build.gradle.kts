plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-billing-v4")
    name.set("AppMetrica SDK Google Play Services Billing v4 library wrapper")
}

android {
    namespace = "io.appmetrica.analytics.billingv4"
    lint {
        disable += "LongLogTag"
    }
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":billing-interface"))

    compileOnly("com.android.billingclient:billing:4.0.0")

    testImplementation("com.android.billingclient:billing:4.0.0")
}
