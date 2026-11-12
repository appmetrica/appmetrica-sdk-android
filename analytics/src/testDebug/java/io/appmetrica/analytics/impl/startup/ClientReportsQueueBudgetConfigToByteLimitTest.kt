package io.appmetrica.analytics.impl.startup

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ClientReportsQueueBudgetConfigToByteLimitTest : CommonTest() {

    private val heap256MiB = 256L * 1024 * 1024

    @Test
    fun defaultConfigLimit() {
        // min(16 MiB, floor(256 MiB * 0.125) = 32 MiB) = 16 MiB
        assertThat(ClientReportsQueueBudgetConfig.DEFAULT.toByteLimit(heap256MiB))
            .isEqualTo(16L * 1024 * 1024)
    }

    @Test
    fun heapFractionCapsBelowMaxSize() {
        val config = ClientReportsQueueBudgetConfig.create(maxSizeMiB = 16, heapFraction = 0.125)!!
        val heap64MiB = 64L * 1024 * 1024
        // min(16 MiB, floor(64 MiB * 0.125) = 8 MiB) = 8 MiB
        assertThat(config.toByteLimit(heap64MiB)).isEqualTo(8L * 1024 * 1024)
    }

    @Test
    fun maxSizeCapsBelowHeapFraction() {
        val config = ClientReportsQueueBudgetConfig.create(maxSizeMiB = 4, heapFraction = 1.0)!!
        val heap64MiB = 64L * 1024 * 1024
        // min(4 MiB, floor(64 MiB * 1.0) = 64 MiB) = 4 MiB
        assertThat(config.toByteLimit(heap64MiB)).isEqualTo(4L * 1024 * 1024)
    }

    @Test
    fun disabledWhenMaxSizeZero() {
        val config = ClientReportsQueueBudgetConfig.create(maxSizeMiB = 0, heapFraction = 0.125)!!
        assertThat(config.toByteLimit(heap256MiB)).isNull()
    }

    @Test
    fun disabledWhenHeapFractionZero() {
        val config = ClientReportsQueueBudgetConfig.create(maxSizeMiB = 16, heapFraction = 0.0)!!
        assertThat(config.toByteLimit(heap256MiB)).isNull()
    }

    @Test
    fun oversizedMaxSizeMiBIsCappedByHeapBeforeMultiply() {
        val heap64MiB = 64L * 1024 * 1024
        // Would overflow Long if multiplied as-is; must clamp to heap then apply fraction.
        val config = ClientReportsQueueBudgetConfig.create(
            maxSizeMiB = Long.MAX_VALUE / 1024,
            heapFraction = 1.0,
        )!!
        assertThat(config.toByteLimit(heap64MiB)).isEqualTo(heap64MiB)
    }
}
