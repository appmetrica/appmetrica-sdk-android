import io.appmetrica.analytics.gradle.Deps

plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-appsetid")
    name.set("AppMetrica SDK AppSetID")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.appsetid"
}

dependencies {
    implementation(project(":core-api"))
    implementation("com.google.android.gms:play-services-appset:${Deps.appSetIdVersion}")
}
