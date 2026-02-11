@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369 fixed in gradle 8.1
plugins {
    alias(appMetricaLibs.plugins.appMetricaGradlePlugin)
}

group = "io.appmetrica.analytics.gradle"

fun GradlePluginDevelopmentExtension.plugin(name: String, impl: String) {
    plugins.create(name.split('.', '-').joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }) {
        id = name
        implementationClass = impl
    }
}

gradlePlugin {
    plugin("appmetrica-common-module", "io.appmetrica.analytics.gradle.AppMetricaCommonModulePlugin")
    plugin("appmetrica-jacoco", "io.appmetrica.analytics.gradle.jacoco.JacocoPlugin")
    plugin("appmetrica-module", "io.appmetrica.analytics.gradle.AppMetricaModulePlugin")
    plugin("appmetrica-proto", "io.appmetrica.analytics.gradle.protobuf.ProtobufPlugin")
    plugin("appmetrica-publish", "io.appmetrica.analytics.gradle.publishing.PublishingPlugin")
    plugin("appmetrica-public-publish", "io.appmetrica.analytics.gradle.publishing.PublicPublishPlugin")
    plugin(
        "appmetrica-ndkcrashes-public-publish",
        "io.appmetrica.analytics.gradle.publishing.NdkCrashesPublicPublishPlugin"
    )
    plugin("appmetrica-teamcity", "io.appmetrica.analytics.gradle.teamcity.TeamCityPlugin")
    plugin("appmetrica-test-split", "io.appmetrica.analytics.gradle.test.TestSplitPlugin")
    plugin("appmetrica-update-version", "io.appmetrica.analytics.gradle.UpdateVersionPlugin")
}

dependencies {
    // https://asm.ow2.io/
    implementation(appMetricaLibs.asm)
    // by source
    implementation(appMetricaLibs.appMetricaAarCheck)
    implementation(appMetricaLibs.appMetricaAndroidLibrary)
    implementation(appMetricaLibs.appMetricaMavenCentralPublish)
    implementation(appMetricaLibs.appMetricaNoLogs)
}
