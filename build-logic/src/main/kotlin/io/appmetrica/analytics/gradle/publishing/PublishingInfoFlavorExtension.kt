package io.appmetrica.analytics.gradle.publishing

import org.gradle.api.provider.Property

abstract class PublishingInfoFlavorExtension {
    abstract val artifactIdSuffix: Property<String>
    abstract val withSources: Property<Boolean>
}
