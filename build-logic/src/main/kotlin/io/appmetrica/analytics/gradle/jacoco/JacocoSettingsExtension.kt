package io.appmetrica.analytics.gradle.jacoco

import org.gradle.api.provider.ListProperty

abstract class JacocoSettingsExtension {
    abstract val exclude: ListProperty<String>
}
