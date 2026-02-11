plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-apphud-v3")
    name.set("AppMetrica SDK Apphud v3 integration module")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.apphudv3"

    defaultConfig {
        missingDimensionStrategy("tier", "binaryProd")
    }
}

dependencies {
    implementation(project(":apphud"))
    implementation(project(":logger"))

    implementation(appMetricaLibs.apphudV3)
}
