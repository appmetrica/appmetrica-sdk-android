package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class PhoneUtilsGetConnectionTypeInServerFormatTest(
    private val sdkType: NetworkType?,
    private val serverType: Int
) : CommonTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "for type {0} server type is {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(NetworkType.CELL, EventProto.ReportMessage.Session.CONNECTION_CELL),
            arrayOf(NetworkType.WIFI, EventProto.ReportMessage.Session.CONNECTION_WIFI),
            arrayOf(NetworkType.BLUETOOTH, EventProto.ReportMessage.Session.CONNECTION_BLUETOOTH),
            arrayOf(NetworkType.ETHERNET, EventProto.ReportMessage.Session.CONNECTION_ETHERNET),
            arrayOf(NetworkType.MOBILE_DUN, EventProto.ReportMessage.Session.CONNECTION_MOBILE_DUN),
            arrayOf(NetworkType.MOBILE_HIPRI, EventProto.ReportMessage.Session.CONNECTION_MOBILE_HIPRI),
            arrayOf(NetworkType.MOBILE_MMS, EventProto.ReportMessage.Session.CONNECTION_MOBILE_MMS),
            arrayOf(NetworkType.MOBILE_SUPL, EventProto.ReportMessage.Session.CONNECTION_MOBILE_SUPL),
            arrayOf(NetworkType.VPN, EventProto.ReportMessage.Session.CONNECTION_VPN),
            arrayOf(NetworkType.WIMAX, EventProto.ReportMessage.Session.CONNECTION_WIMAX),
            arrayOf(NetworkType.LOWPAN, EventProto.ReportMessage.Session.CONNECTION_LOWPAN),
            arrayOf(NetworkType.WIFI_AWARE, EventProto.ReportMessage.Session.CONNECTION_WIFI_AWARE),
            arrayOf(null, EventProto.ReportMessage.Session.CONNECTION_UNDEFINED)
        )
    }

    @Test
    fun getConnectionInServerType() {
        assertThat(PhoneUtils.getConnectionTypeInServerFormat(sdkType)).isEqualTo(serverType)
    }
}
