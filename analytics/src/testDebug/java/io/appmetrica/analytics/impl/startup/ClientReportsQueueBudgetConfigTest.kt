package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.startup.parsing.JsonResponseKey
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test

internal class ClientReportsQueueBudgetConfigTest : CommonTest() {

    @Test
    fun createValid() {
        val config = ClientReportsQueueBudgetConfig.create(8L, 0.25)
        assertThat(config).isEqualTo(
            ClientReportsQueueBudgetConfig.create(maxSizeMiB = 8L, heapFraction = 0.25)
        )
        assertThat(config!!.maxSizeMiB).isEqualTo(8L)
        assertThat(config.heapFraction).isEqualTo(0.25)
    }

    @Test
    fun createAcceptsBoundaryValues() {
        assertThat(ClientReportsQueueBudgetConfig.create(0L, 0.0)).isNotNull
        assertThat(ClientReportsQueueBudgetConfig.create(1L, 1.0)).isNotNull
    }

    @Test
    fun createRejectsNegativeMaxSize() {
        assertThat(ClientReportsQueueBudgetConfig.create(-1L, 0.25)).isNull()
    }

    @Test
    fun createRejectsHeapFractionOutOfRange() {
        assertThat(ClientReportsQueueBudgetConfig.create(8L, -0.1)).isNull()
        assertThat(ClientReportsQueueBudgetConfig.create(8L, 1.1)).isNull()
        assertThat(ClientReportsQueueBudgetConfig.create(8L, Double.NaN)).isNull()
        assertThat(ClientReportsQueueBudgetConfig.create(8L, Double.POSITIVE_INFINITY)).isNull()
    }

    @Test
    fun fromJsonNullReturnsNull() {
        assertThat(ClientReportsQueueBudgetConfig.fromJson(null)).isNull()
    }

    @Test
    fun fromJsonIncompleteReturnsNull() {
        assertThat(
            ClientReportsQueueBudgetConfig.fromJson(
                JSONObject().put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 8)
            )
        ).isNull()
        assertThat(
            ClientReportsQueueBudgetConfig.fromJson(
                JSONObject().put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.25)
            )
        ).isNull()
    }

    @Test
    fun fromJsonValid() {
        val json = JSONObject()
            .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, 8)
            .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.25)
        assertThat(ClientReportsQueueBudgetConfig.fromJson(json))
            .isEqualTo(ClientReportsQueueBudgetConfig.create(8L, 0.25))
    }

    @Test
    fun fromJsonInvalidReturnsNull() {
        val json = JSONObject()
            .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, -1)
            .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, 0.25)
        assertThat(ClientReportsQueueBudgetConfig.fromJson(json)).isNull()
    }

    @Test
    fun fromJsonStringNullOrEmptyReturnsNull() {
        assertThat(ClientReportsQueueBudgetConfig.fromJsonString(null)).isNull()
        assertThat(ClientReportsQueueBudgetConfig.fromJsonString("")).isNull()
    }

    @Test
    fun fromJsonStringCorruptReturnsNull() {
        assertThat(ClientReportsQueueBudgetConfig.fromJsonString("not-json")).isNull()
    }

    @Test
    fun toJsonStringRoundTrip() {
        val config = ClientReportsQueueBudgetConfig.create(8L, 0.25)!!
        assertThat(ClientReportsQueueBudgetConfig.fromJsonString(config.toJsonString()))
            .isEqualTo(config)
    }
}
