plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-ironsource-v9")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for IronSource v9")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.ironsource.v9"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly("com.unity3d.ads-mediation:mediation-sdk:9.0.0")
    testImplementation("com.unity3d.ads-mediation:mediation-sdk:9.0.0")
}
