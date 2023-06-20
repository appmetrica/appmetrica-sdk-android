package io.appmetrica.analytics.gradle.codequality

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty

abstract class CodeQualityExtension {
    abstract val configDir: DirectoryProperty
    abstract val exclude: ListProperty<String>
}
