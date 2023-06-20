plugins {
    id("io.appmetrica.gradle.kotlin-library")
}

dependencies {
    implementation("junit:junit:4.13.2")
    implementation("org.assertj:assertj-core:3.15.0")
    implementation("org.mockito:mockito-inline:3.5.15")
    // https://github.com/robolectric/robolectric
    implementation("org.robolectric:robolectric:4.8.2")
}
