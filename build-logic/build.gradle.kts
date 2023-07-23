import io.appmetrica.gradle.detectAgpVersion

@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369 fixed in gradle 8.1
plugins {
    alias(libs.plugins.appMetricaGradlePlugin)
}

group = "io.appmetrica.analytics.gradle"

val agpVersion: String = detectAgpVersion("7.2.2")

fun GradlePluginDevelopmentExtension.plugin(name: String, impl: String) {
    plugins.create(name.split('.', '-').joinToString("") { it.capitalize() }) {
        id = name
        implementationClass = impl
    }
}

gradlePlugin {
    plugin("appmetrica-codequality", "io.appmetrica.analytics.gradle.codequality.CodeQualityPlugin")
    plugin("appmetrica-common-module", "io.appmetrica.analytics.gradle.AppMetricaCommonModulePlugin")
    plugin("appmetrica-jacoco", "io.appmetrica.analytics.gradle.jacoco.JacocoPlugin")
    plugin("appmetrica-module", "io.appmetrica.analytics.gradle.AppMetricaModulePlugin")
    plugin("appmetrica-proto", "io.appmetrica.analytics.gradle.protobuf.ProtobufPlugin")
    plugin("appmetrica-publish", "io.appmetrica.analytics.gradle.publishing.PublishingPlugin")
    plugin("appmetrica-teamcity", "io.appmetrica.analytics.gradle.teamcity.TeamCityPlugin")
    plugin("appmetrica-update-version", "io.appmetrica.analytics.gradle.UpdateVersionPlugin")
}

dependencies {
    // https://developer.android.com/studio/releases/gradle-plugin
    implementation("com.android.tools.build:gradle:${agpVersion}")
    // https://kotlinlang.org/docs/gradle.html
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    // https://github.com/Kotlin/dokka/releases
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    // by source
    implementation(libs.appMetricaAarCheck)
    implementation(libs.appMetricaAndroid)
    implementation(libs.appMetricaNoLogs)
    implementation(libs.appMetricaRepositories)
}
