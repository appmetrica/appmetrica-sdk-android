package io.appmetrica.analytics.location.impl.gpl

import android.Manifest
import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.location.impl.LocationListenerWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class GplLastKnownLocationExtractorProviderTest : CommonTest() {

    private val executor = mock<IHandlerExecutor>()
    private val permissionExtractor = mock<PermissionExtractor>()
    private val context = mock<Context>()
    private val identifier = "Some identifier"

    private val listener = mock<LocationListenerWrapper>()

    @get:Rule
    val singlePermissionStrategyMockedConstructionRule =
        MockedConstructionRule(SinglePermissionStrategy::class.java)

    @get:Rule
    val gplLastKnownLocationExtractorMockedRule = MockedConstructionRule(GplLastKnownLocationExtractor::class.java)

    private lateinit var provider: GplLastKnownLocationExtractorProvider

    @Before
    fun setUp() {
        provider = GplLastKnownLocationExtractorProvider(identifier)
    }

    @Test
    fun getExtractor() {
        assertThat(provider.getExtractor(context, permissionExtractor, executor, listener))
            .isEqualTo(gplLastKnownLocationExtractorMockedRule.constructionMock.constructed()[0])
        assertThat(gplLastKnownLocationExtractorMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(gplLastKnownLocationExtractorMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(context, locationPermissionResolutionStrategy(), listener, executor)
    }

    private fun locationPermissionResolutionStrategy(): SinglePermissionStrategy {
        assertThat(singlePermissionStrategyMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(singlePermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(permissionExtractor, Manifest.permission.ACCESS_COARSE_LOCATION)
        return singlePermissionStrategyMockedConstructionRule.constructionMock.constructed().first()
    }
}
