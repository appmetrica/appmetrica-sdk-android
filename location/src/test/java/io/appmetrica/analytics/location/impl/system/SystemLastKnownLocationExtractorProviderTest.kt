package io.appmetrica.analytics.location.impl.system

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.location.impl.LocationListenerWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class SystemLastKnownLocationExtractorProviderTest : CommonTest() {

    private val context = mock<Context>()
    private val executor = mock<IHandlerExecutor>()
    private val locationListener = mock<LocationListenerWrapper>()
    private val permissionExtractor = mock<PermissionExtractor>()
    private val permissionResolutionStrategy = mock<PermissionResolutionStrategy>()
    private val permissionStrategyProvider = mock<PermissionStrategyProvider> {
        on { getPermissionResolutionStrategy(permissionExtractor) } doReturn permissionResolutionStrategy
    }
    private val provider = "Some test provider"
    private val identifier = "Some identifier"

    @get:Rule
    val systemLastKnownLocationExtractorMockedRule =
        MockedConstructionRule(SystemLastKnownLocationExtractor::class.java)

    private lateinit var systemLastKnownLocationExtractorProvider: SystemLastKnownLocationExtractorProvider

    @Before
    fun setUp() {
        systemLastKnownLocationExtractorProvider =
            SystemLastKnownLocationExtractorProvider(provider, permissionStrategyProvider, identifier)
    }

    @Test
    fun getExtractor() {
        val result = systemLastKnownLocationExtractorProvider.getExtractor(
            context,
            permissionExtractor,
            executor,
            locationListener
        )
        assertThat(systemLastKnownLocationExtractorMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(result).isEqualTo(systemLastKnownLocationExtractorMockedRule.constructionMock.constructed().first())
        assertThat(systemLastKnownLocationExtractorMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(context, permissionResolutionStrategy, locationListener, provider)
    }
}
