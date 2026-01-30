package io.appmetrica.analytics.gradle.test

import org.gradle.api.provider.Property

/**
 * Extension for configuring general test execution settings.
 *
 * This extension is created by AppMetricaCommonModulePlugin and applies to all test tasks.
 */
abstract class TestSettingsExtension {
    /**
     * Maximum number of test worker processes to run in parallel.
     *
     * Default: 4
     */
    abstract val maxParallelForks: Property<Int>
}
