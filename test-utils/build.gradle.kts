import io.appmetrica.analytics.gradle.Deps

plugins {
    alias(appMetricaLibs.plugins.appMetricaKotlinLibrary)
}

dependencies {
    api("junit:junit:4.13.2")
    api("org.assertj:assertj-core:3.26.3")
    api("org.mockito:mockito-inline:5.2.0")
    api("org.mockito.kotlin:mockito-kotlin:4.1.0")
    // https://github.com/robolectric/robolectric
    api("org.robolectric:robolectric:4.16")
    compileOnly("androidx.annotation:annotation:${Deps.androidX}")
    implementation(kotlin("reflect"))
}
