package io.appmetrica.analytics.adrevenue.other.impl.fb

import android.os.Bundle
import com.facebook.ads.AdSDKNotificationListener
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade
import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class FBAdRevenueDataListenerTest : CommonTest() {

    private val clientContext: ClientContext = mock()
    private val internalClientModuleFacade: InternalClientModuleFacade = mock()
    private val adRevenue: ModuleAdRevenue = mock()
    private val converter: FBAdRevenueConverter = mock()
    private val bundle = Bundle()

    private lateinit var listener: FBAdRevenueDataListener

    @Before
    fun setUp() {
        whenever(clientContext.internalClientModuleFacade).thenReturn(internalClientModuleFacade)
        whenever(converter.convert(bundle)).thenReturn(adRevenue)
        listener = FBAdRevenueDataListener(clientContext, converter)
    }

    @Test
    fun onAdEventImpressionEvent() {
        listener.onAdEvent(AdSDKNotificationListener.IMPRESSION_EVENT, bundle)
        verify(internalClientModuleFacade).reportAdRevenue(adRevenue)
    }

    @Test
    fun onAdEventOtherEvent() {
        listener.onAdEvent("OTHER_EVENT", bundle)
        verifyNoInteractions(internalClientModuleFacade)
    }
}
