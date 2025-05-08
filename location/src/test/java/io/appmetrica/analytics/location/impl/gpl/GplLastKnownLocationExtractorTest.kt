package io.appmetrica.analytics.location.impl.gpl

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.gpllibrary.internal.IGplLibraryWrapper
import io.appmetrica.analytics.location.impl.LocationListenerWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class GplLastKnownLocationExtractorTest : CommonTest() {

    @get:Rule
    val gplWrapperFactoryMockedRule = MockedConstructionRule(GplWrapperFactory::class.java) { mock, mockedContext ->
        whenever(mock.create(context, locationListener, executor)).thenReturn(gplWrapper)
    }

    private val context = mock<Context>()

    private val permissionResolutionStrategy = mock<PermissionResolutionStrategy> {
        on { hasNecessaryPermissions(context) } doReturn true
    }

    private val locationListener = mock<LocationListenerWrapper>()
    private val executor = mock<IHandlerExecutor>()
    private val gplWrapper = mock<IGplLibraryWrapper>()

    private lateinit var gplLastKnownLocationExtractor: GplLastKnownLocationExtractor

    @Before
    fun setUp() {
        gplLastKnownLocationExtractor =
            GplLastKnownLocationExtractor(context, permissionResolutionStrategy, locationListener, executor)
    }

    @Test
    fun updateLastKnownLocation() {
        gplLastKnownLocationExtractor.updateLastKnownLocation()
        verify(gplWrapper).updateLastKnownLocation()
        wrapperFactory()
    }

    @Test
    fun `updateLastKnownLocation() if no permission`() {
        whenever(permissionResolutionStrategy.hasNecessaryPermissions(context)).thenReturn(false)
        gplLastKnownLocationExtractor.updateLastKnownLocation()
        verifyNoMoreInteractions(wrapperFactory(), gplWrapper)
    }

    @Test
    fun `updateLastKnownLocation() if throw`() {
        whenever(wrapperFactory().create(any(), any(), any())).thenThrow(RuntimeException())
    }

    private fun wrapperFactory(): GplWrapperFactory {
        assertThat(gplWrapperFactoryMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(gplWrapperFactoryMockedRule.argumentInterceptor.flatArguments()).isEmpty()
        return gplWrapperFactoryMockedRule.constructionMock.constructed().first()
    }
}
