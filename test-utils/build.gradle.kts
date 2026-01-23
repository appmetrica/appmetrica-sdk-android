import io.appmetrica.analytics.gradle.Deps
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.appmetrica.gradle.kotlin-library")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

dependencies {
    api("junit:junit:4.13.2")
    api("org.assertj:assertj-core:3.26.3")
    api("org.mockito:mockito-inline:5.2.0")
    api("org.mockito.kotlin:mockito-kotlin:4.1.0")
    // https://github.com/robolectric/robolectric
    api("org.robolectric:robolectric:4.12.1")
    compileOnly("androidx.annotation:annotation:${Deps.androidX}")

    implementation("com.pinterest.ktlint:ktlint-core:0.43.2")
}
