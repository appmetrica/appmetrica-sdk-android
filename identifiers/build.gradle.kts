plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-identifiers")
    name.set("AppMetrica SDK Identifiers")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.identifiers"
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation("com.google.android.gms:play-services-ads-identifier:18.1.0")
}
