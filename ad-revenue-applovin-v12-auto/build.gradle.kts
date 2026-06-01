plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-applovin-v12-auto")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for AppLovin MAX v12 (ILRD)")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.applovin.v12.auto"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly(appMetricaLibs.applovin)
    testImplementation(appMetricaLibs.applovin)
}
