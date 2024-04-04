plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-ad-revenue")
    name.set("AppMetrica SDK Ad Revenue Auto Collection")
    withJavadoc.set(false)
}

android {
    namespace = "io.appmetrica.analytics.adrevenue.common"
}

dependencies {
    runtimeOnly(project(":ad-revenue-fyber-v3"))
    runtimeOnly(project(":ad-revenue-ironsource-v7"))
}
