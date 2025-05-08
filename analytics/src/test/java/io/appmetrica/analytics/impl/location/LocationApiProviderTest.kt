package io.appmetrica.analytics.impl.location

import android.content.Context
import io.appmetrica.analytics.locationapi.internal.LocationClient
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class LocationApiProviderTest : CommonTest() {

    private val context = mock<Context>()
    private val locationClient = mock<LocationClient>()

    @get:Rule
    val locationControllerImplMockedConstructionRule = MockedConstructionRule(LocationControllerImpl::class.java)

    @get:Rule
    val locationApiImplMockedConstructionRule = MockedConstructionRule(LocationApiImpl::class.java)

    @get:Rule
    val locationClientProviderMockedConstructionRule =
        MockedConstructionRule(LocationClientProvider::class.java) { mock, _ ->
            whenever(mock.getLocationClient()).thenReturn(locationClient)
        }

    private lateinit var locationApiProvider: LocationApiProvider

    @Before
    fun setUp() {
        locationApiProvider = LocationApiProvider()
    }

    @Test
    fun getLocationApi() {
        assertThat(locationApiProvider.getLocationApi(context))
            .isEqualTo(locationApiImplMockedConstructionRule.constructionMock.constructed().first())
        assertThat(locationApiImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                locationControllerImplMockedConstructionRule.constructionMock.constructed().first(),
                locationClient
            )
        assertThat(locationControllerImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun checkLocationClientProvider() {
        assertThat(locationClientProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(locationClientProviderMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
