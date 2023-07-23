import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.appmetrica.gradle.kotlin-library")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_6.toString()
}

dependencies {
    api("junit:junit:4.13.2")
    api("org.assertj:assertj-core:3.15.0")
    api("org.mockito:mockito-inline:3.12.4")
    api("org.mockito.kotlin:mockito-kotlin:3.2.0")
    // https://github.com/robolectric/robolectric
    api("org.robolectric:robolectric:4.9")
}
