plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-apphud-v2")
    name.set("AppMetrica SDK Apphud v2 integration module")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.apphudv2"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":apphud"))
    implementation(project(":logger"))

    implementation(appMetricaLibs.apphudV2)
}
