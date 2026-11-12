package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.impl.startup.ClientReportsQueueBudgetConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test

internal class ClientReportsQueueBudgetConfigParserTest : CommonTest() {

    private val parser = ClientReportsQueueBudgetConfigParser()
    private val result = StartupResult()

    @Test
    fun noBlock() {
        parser.parse(result, JSONObject())
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun emptyBlockIsIgnored() {
        parser.parse(result, JSONObject().put(JsonResponseKey.CLIENT_REPORTS_QUEUE, JSONObject()))
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun partialBlockMissingHeapFractionIsIgnored() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject().put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 8)
            )
        )
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun partialBlockMissingMaxSizeIsIgnored() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject().put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.25)
            )
        )
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun heapFractionAboveOneIsIgnored() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject()
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 8)
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 1.5)
            )
        )
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun negativeMaxSizeIsIgnored() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject()
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, -1)
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.25)
            )
        )
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun negativeHeapFractionIsIgnored() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject()
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 8)
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, -0.1)
            )
        )
        assertThat(result.clientReportsQueueBudgetConfig).isNull()
    }

    @Test
    fun filledBlock() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject()
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 8)
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.25)
            )
        )
        ObjectPropertyAssertions(result.clientReportsQueueBudgetConfig).apply {
            checkField("maxSizeMiB", 8L)
            checkField("heapFraction", 0.25)
            checkAll()
        }
    }

    @Test
    fun disabledValues() {
        parser.parse(
            result,
            JSONObject().put(
                JsonResponseKey.CLIENT_REPORTS_QUEUE,
                JSONObject()
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 0)
                    .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.125)
            )
        )
        val config = result.clientReportsQueueBudgetConfig
        assertThat(config).isEqualTo(
            ClientReportsQueueBudgetConfig.create(maxSizeMiB = 0, heapFraction = 0.125)
        )
        assertThat(config!!.toByteLimit(256L * 1024 * 1024)).isNull()
    }
}
