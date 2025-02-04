@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369 fixed in gradle 8.1
plugins {
    alias(libs.plugins.appMetricaKotlinLibrary)
}

group = "io.appmetrica.analytics"

dependencies {
    implementation("org.assertj:assertj-core:3.26.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}
