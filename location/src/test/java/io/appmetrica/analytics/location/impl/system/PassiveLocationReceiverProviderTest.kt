package io.appmetrica.analytics.location.impl.system

import android.Manifest
import android.content.Context
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.location.impl.LocationListenerWrapper
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class PassiveLocationReceiverProviderTest {

    private val looper = mock<Looper>()
    private val executor = mock<IHandlerExecutor> {
        on { looper } doReturn looper
    }
    private val context = mock<Context>()
    private val locationListener = mock<LocationListenerWrapper>()
    private val permissionExtractor = mock<PermissionExtractor>()

    private lateinit var passiveLocationReceiverProvider: PassiveLocationReceiverProvider

    @get:Rule
    val passiveLocationMockedRule = MockedConstructionRule(PassiveProviderLocationReceiver::class.java)

    @get:Rule
    val singlePermissionStrategyMockedConstructionRule = MockedConstructionRule(SinglePermissionStrategy::class.java)

    @Before
    fun setUp() {
        passiveLocationReceiverProvider = PassiveLocationReceiverProvider()
    }

    @Test
    fun `getLocationReceiver and getExtractor`() {
        val receiver = passiveLocationReceiverProvider.getLocationReceiver(
            context,
            permissionExtractor,
            executor,
            locationListener
        )

        val extractor = passiveLocationReceiverProvider.getExtractor(
            context,
            permissionExtractor,
            executor,
            locationListener
        )

        assertThat(receiver).isEqualTo(extractor)

        assertThat(passiveLocationMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(receiver).isEqualTo(passiveLocationMockedRule.constructionMock.constructed().first())
        assertThat(extractor).isEqualTo(passiveLocationMockedRule.constructionMock.constructed().first())
        assertThat(passiveLocationMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                looper,
                fineLocationPermissionResolutionStrategy(),
                locationListener
            )
    }

    private fun fineLocationPermissionResolutionStrategy(): SinglePermissionStrategy {
        assertThat(singlePermissionStrategyMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(singlePermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(permissionExtractor, Manifest.permission.ACCESS_FINE_LOCATION)
        return singlePermissionStrategyMockedConstructionRule.constructionMock.constructed().first()
    }
}
