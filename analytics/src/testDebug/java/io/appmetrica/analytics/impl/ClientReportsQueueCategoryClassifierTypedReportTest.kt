package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ClientReportsQueueCategoryClassifierTypedReportTest : CommonTest() {

    @Test
    fun rtmUpdateConfigIsControl() {
        val bundle = mock<Bundle> {
            on {
                getString("action")
            } doReturn "io.appmetrica.analytics.rtm.UPDATE_RTM_CONFIGURATION"
        }
        assertThat(ClientReportsQueueCategoryClassifier.ofTypedReport(TRANSPORT_RTM, bundle))
            .isEqualTo(ClientReportsQueueCategory.CONTROL)
    }

    @Test
    fun rtmReportEventIsData() {
        val bundle = mock<Bundle> {
            on { getString("action") } doReturn "io.appmetrica.analytics.rtm.REPORT_EVENT"
        }
        assertThat(ClientReportsQueueCategoryClassifier.ofTypedReport(TRANSPORT_RTM, bundle))
            .isEqualTo(ClientReportsQueueCategory.DATA)
    }

    @Test
    fun rtmWithoutActionIsData() {
        val bundle = mock<Bundle> {
            on { getString("action") } doReturn null
        }
        assertThat(ClientReportsQueueCategoryClassifier.ofTypedReport(TRANSPORT_RTM, bundle))
            .isEqualTo(ClientReportsQueueCategory.DATA)
    }

    @Test
    fun internalEventsControlTypeIsControlRegardlessOfBundle() {
        val bundle = mock<Bundle>()
        assertThat(
            ClientReportsQueueCategoryClassifier.ofTypedReport(
                InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId,
                bundle,
            )
        ).isEqualTo(ClientReportsQueueCategory.CONTROL)
    }

    @Test
    fun unknownTransportTypeIsData() {
        val bundle = mock<Bundle>()
        assertThat(ClientReportsQueueCategoryClassifier.ofTypedReport(/* unknown */ 99, bundle))
            .isEqualTo(ClientReportsQueueCategory.DATA)
    }

    private companion object {
        const val TRANSPORT_RTM = 2
    }
}
