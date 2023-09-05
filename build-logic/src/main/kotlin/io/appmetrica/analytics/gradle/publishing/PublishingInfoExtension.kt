package io.appmetrica.analytics.gradle.publishing

import org.gradle.api.provider.Property

abstract class PublishingInfoExtension {
    abstract val name: Property<String>
    abstract val description: Property<String>

    abstract val baseArtifactId: Property<String>
    abstract val withJavadoc: Property<Boolean>
    abstract val checkAllJavadoc: Property<Boolean>
}
