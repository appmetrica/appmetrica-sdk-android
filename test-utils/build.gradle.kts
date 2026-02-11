plugins {
    alias(appMetricaLibs.plugins.appMetricaKotlinLibrary)
}

dependencies {
    api(appMetricaLibs.junit)
    api(appMetricaLibs.assertj)
    api(appMetricaLibs.mockitoInline)
    api(appMetricaLibs.mockitoKotlin)
    // https://github.com/robolectric/robolectric
    api(appMetricaLibs.robolectric)
    compileOnly(appMetricaLibs.androidxAnnotation)
    implementation(kotlin("reflect"))
}
