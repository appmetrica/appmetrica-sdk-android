plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-ironsource-v7")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for IronSource v7")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.ironsource.v7"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly("com.unity3d.ads-mediation:mediation-sdk:8.10.0")
    testImplementation("com.unity3d.ads-mediation:mediation-sdk:8.10.0")
}
