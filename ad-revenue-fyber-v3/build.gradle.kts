plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue-fyber-v3")
    name.set("AppMetrica SDK Ad Revenue Auto Collection Adapter for Fyber v3")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.fyber.v3"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":core-utils"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))

    compileOnly("com.fyber:fairbid-sdk:3.48.0")
    testImplementation("com.fyber:fairbid-sdk:3.48.0")
}
