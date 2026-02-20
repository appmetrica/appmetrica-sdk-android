package io.appmetrica.analytics.impl.client

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Process
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.impl.DataResultReceiver
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

// Robolectric needs for parcelable
@SuppressLint("RobolectricUsage")
@RunWith(RobolectricTestRunner::class)
internal class ProcessConfigurationParcelableTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()

    private lateinit var processConfiguration: ProcessConfiguration

    @Before
    fun setUp() {
        val receiver = DataResultReceiver(
            Handler(Looper.getMainLooper()),
            mock<DataResultReceiver.Receiver>()
        )
        processConfiguration = ProcessConfiguration(contextRule.context, receiver)
    }

    @Test
    fun parcelable() {
        val clids = mapOf(
            "clid key 1" to "clid value 1",
            "clid key 2" to "clid value 2"
        )
        val hosts = listOf("host1", "host2")
        val referrer = "referrer"
        val installReferrerSource = "gpl"

        processConfiguration.clientClids = clids
        processConfiguration.customHosts = hosts
        processConfiguration.distributionReferrer = referrer
        processConfiguration.installReferrerSource = installReferrerSource

        val parcel = Parcel.obtain()
        try {
            processConfiguration.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            val fromParcel = ProcessConfiguration.CREATOR.createFromParcel(parcel)

            SoftAssertions().apply {
                assertThat(fromParcel.processID).isEqualTo(Process.myPid())
                assertThat(fromParcel.processSessionID).isEqualTo(ProcessConfiguration.PROCESS_SESSION_ID)
                assertThat(fromParcel.sdkApiLevel).isEqualTo(BuildConfig.API_LEVEL)
                assertThat(fromParcel.packageName).isEqualTo(contextRule.context.packageName)
                assertThat(fromParcel.customHosts).containsExactlyInAnyOrderElementsOf(hosts)
                assertThat(fromParcel.clientClids).containsAllEntriesOf(clids).hasSameSizeAs(clids)
                assertThat(fromParcel.distributionReferrer).isEqualTo(referrer)
                assertThat(fromParcel.installReferrerSource).isEqualTo(installReferrerSource)
                assertThat(fromParcel.dataResultReceiver).isNotNull()
                assertAll()
            }
        } finally {
            parcel.recycle()
        }
    }
}
