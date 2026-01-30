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
     * Number of test methods after which JVM is restarted for Robolectric tests.
     *
     * Default: 10 (frequent restarts to prevent memory leaks)
     */
    abstract val robolectricForkEvery: Property<Int>

    /**
     * Maximum heap memory for Robolectric test JVM processes.
     *
     * Default: "6g"
     */
    abstract val robolectricMemory: Property<String>

    /**
     * Number of test methods after which JVM is restarted for standard tests.
     *
     * Default: 1000 (rare restarts)
     */
    abstract val standardForkEvery: Property<Int>

    init {
        enabled.convention(false) // Disabled by default, enable per-module
        robolectricForkEvery.convention(10) // Frequent forks for Robolectric
        robolectricMemory.convention("6g") // More memory for Robolectric
        standardForkEvery.convention(1000) // Less frequent forks for standard tests
    }
}
