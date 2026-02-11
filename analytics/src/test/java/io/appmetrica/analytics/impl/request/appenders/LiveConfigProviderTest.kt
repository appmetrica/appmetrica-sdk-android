package io.appmetrica.analytics.impl.request.appenders

import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class LiveConfigProviderTest : CommonTest() {

    private val advertisingIdsHolder: AdvertisingIdsHolder = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val liveConfigProvider by setUp { LiveConfigProvider() }

    @Test
    fun advertisingIdentifiers() {
        whenever(GlobalServiceLocator.getInstance().advertisingIdGetter.identifiers)
            .thenReturn(advertisingIdsHolder)

        assertThat(liveConfigProvider.advertisingIdentifiers).isEqualTo(advertisingIdsHolder)
    }
}
