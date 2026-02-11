@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369 fixed in gradle 8.1
plugins {
    alias(appMetricaLibs.plugins.appMetricaKotlinLibrary)
}

group = "io.appmetrica.analytics"

dependencies {
    implementation(appMetricaLibs.assertj)
    implementation(appMetricaLibs.kotlinReflect)
}
