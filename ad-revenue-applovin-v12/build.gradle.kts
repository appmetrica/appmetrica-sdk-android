plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-applovin-v12")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for Applovin MAX v12")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.applovin.v12"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly("com.applovin:applovin-sdk:12.4.2")
    testImplementation("com.applovin:applovin-sdk:12.4.2")
}
