package io.appmetrica.analytics.gradle.test

import org.gradle.api.provider.Property

/**
 * Extension for configuring test splitting between Robolectric and standard JUnit tests.
 *
 * This extension is created by TestSplitPlugin.
 */
abstract class TestSplitExtension {
    /**
     * Enable or disable test splitting.
     *
     * Default: false (disabled)
     */
    abstract val enabled: Property<Boolean>

    /**
     * Maximum heap memory for Robolectric test JVM processes.
     *
     * Default: "6g"
     */
    abstract val robolectricMemory: Property<String>

    init {
        enabled.convention(false) // Disabled by default, enable per-module
        robolectricMemory.convention("6g") // More memory for Robolectric
    }
}
