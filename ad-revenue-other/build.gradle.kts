plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-other")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for Other Networks")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.other"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly(appMetricaLibs.facebookAudienceNetwork)
    testImplementation(appMetricaLibs.facebookAudienceNetwork)
}
