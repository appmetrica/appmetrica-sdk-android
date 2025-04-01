plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-admob-v23")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for AdMob v23")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.admob.v23"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly("com.google.android.gms:play-services-ads:23.6.0")
    testImplementation("com.google.android.gms:play-services-ads:23.6.0")
    // also supports 24.1.0
    // not added since minSdk = 23 for this version
}
