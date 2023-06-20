plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-billing-v3")
    name.set("AppMetrica SDK Google Play Services Billing v3 library wrapper")
}

android {
    namespace = "io.appmetrica.analytics.billingv3"
    lint {
        disable += "LongLogTag"
    }
}

dependencies {
    implementation(project(":billing-interface"))
    implementation(project(":core-utils"))

    compileOnly("com.android.billingclient:billing:3.0.2")

    testImplementation("com.android.billingclient:billing:3.0.2")
}
