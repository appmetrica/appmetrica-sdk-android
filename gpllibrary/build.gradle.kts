import io.appmetrica.analytics.gradle.Deps

plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-gpllibrary")
    name.set("AppMetrica SDK Google Play Location Library wrapper")
}

android {
    namespace = "io.appmetrica.analytics.gpllibrary"
}

dependencies {
    compileOnly("com.google.android.gms:play-services-location:${Deps.gmsLocationVersion}")
    testImplementation("com.google.android.gms:play-services-location:${Deps.gmsLocationVersion}")

    implementation(project(":logger"))
}
