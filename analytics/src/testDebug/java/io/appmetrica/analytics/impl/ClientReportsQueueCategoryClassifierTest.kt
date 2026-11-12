package io.appmetrica.analytics.impl

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class ClientReportsQueueCategoryClassifierTest(
    private val type: Int,
    private val expected: ClientReportsQueueCategory,
    @Suppress("unused") private val label: String,
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun data(): Collection<Array<Any>> = buildList {
            fun control(event: InternalEvents) {
                add(arrayOf(event.typeId, ClientReportsQueueCategory.CONTROL, event.name))
            }

            fun dataSample(event: InternalEvents) {
                add(arrayOf(event.typeId, ClientReportsQueueCategory.DATA, event.name))
            }

            // CONTROL allowlist (CRS-enqueued typed reports)
            control(InternalEvents.EVENT_TYPE_ACTIVATION)
            control(InternalEvents.EVENT_TYPE_STARTUP)
            control(InternalEvents.EVENT_TYPE_REQUEST_REFERRER)
            control(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID)
            control(InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED)
            control(InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED)
            control(InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA)
            control(InternalEvents.EVENT_TYPE_START)
            control(InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME)
            control(InternalEvents.EVENT_TYPE_PURGE_BUFFER)
            control(InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG)
            control(InternalEvents.EVENT_TYPE_WEBVIEW_SYNC)

            // DATA samples (including service-only types if they ever hit CRS typed path)
            dataSample(InternalEvents.EVENT_TYPE_REGULAR)
            dataSample(InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION)
            dataSample(InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF)
            dataSample(InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF)
            dataSample(InternalEvents.EVENT_TYPE_ANR)
            dataSample(InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT)
            dataSample(InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT)
            dataSample(InternalEvents.EVENT_TYPE_INIT)
            dataSample(InternalEvents.EVENT_TYPE_FIRST_ACTIVATION)
            dataSample(InternalEvents.EVENT_TYPE_ALIVE)
            dataSample(InternalEvents.EVENT_TYPE_PERMISSIONS)
            dataSample(InternalEvents.EVENT_TYPE_APP_FEATURES)
            dataSample(InternalEvents.EVENT_TYPE_CLEANUP)
            dataSample(InternalEvents.EVENT_TYPE_SEND_REFERRER)
        }
    }

    @Test
    fun ofEventType() {
        assertThat(ClientReportsQueueCategoryClassifier.ofEventType(type)).isEqualTo(expected)
    }

    @Test
    fun ofEventTypeNeverReturnsFatal() {
        assertThat(ClientReportsQueueCategoryClassifier.ofEventType(type))
            .isNotEqualTo(ClientReportsQueueCategory.FATAL)
    }
}
