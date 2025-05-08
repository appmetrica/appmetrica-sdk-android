package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class StartupStateHolderTest : CommonTest() {

    private val context = mock<Context>()
    private val startupStateFromStorage = mock<StartupState>()
    private val startupState = mock<StartupState>()
    private val firstObserver = mock<StartupStateObserver>()
    private val secondObserver = mock<StartupStateObserver>()

    @get:Rule
    val storageMockedRule = MockedConstructionRule(StartupState.Storage::class.java) { mock, mockContext ->
        whenever(mock.read()).thenReturn(startupStateFromStorage)
    }

    private lateinit var startupStateHolder: StartupStateHolder

    @Before
    fun setUp() {
        startupStateHolder = StartupStateHolder()
    }

    @Test
    fun storageCreation() {
        startupStateHolder.init(context)
        assertThat(storageMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(storageMockedRule.argumentInterceptor.flatArguments()).containsExactly(context)
    }

    @Test
    fun `init without observers`() {
        startupStateHolder.init(context)
        verifyNoMoreInteractions(firstObserver, secondObserver)
    }

    @Test
    fun `init with observer`() {
        startupStateHolder.registerObserver(firstObserver)
        verifyNoMoreInteractions(firstObserver)
        startupStateHolder.init(context)
        verify(firstObserver).onStartupStateChanged(startupStateFromStorage)
    }

    @Test
    fun `get after init`() {
        startupStateHolder.init(context)
        assertThat(startupStateHolder.getStartupState()).isEqualTo(startupStateFromStorage)
    }

    @Test
    fun `get after set`() {
        startupStateHolder.onStartupStateChanged(startupState)
        assertThat(startupStateHolder.getStartupState()).isEqualTo(startupState)
    }

    @Test
    fun `get after set after init`() {
        startupStateHolder.init(context)
        startupStateHolder.onStartupStateChanged(startupState)
        assertThat(startupStateHolder.getStartupState()).isEqualTo(startupState)
    }

    @Test
    fun `registerObserver after init`() {
        startupStateHolder.init(context)
        startupStateHolder.registerObserver(firstObserver)
        verify(firstObserver).onStartupStateChanged(startupStateFromStorage)
    }

    @Test
    fun `registerObserver after set`() {
        startupStateHolder.onStartupStateChanged(startupState)
        startupStateHolder.registerObserver(firstObserver)
        verify(firstObserver).onStartupStateChanged(startupState)
    }

    @Test
    fun `registerObserver after init and before set`() {
        startupStateHolder.init(context)
        startupStateHolder.registerObserver(firstObserver)
        startupStateHolder.onStartupStateChanged(startupState)
        startupStateHolder.registerObserver(secondObserver)

        inOrder(firstObserver, secondObserver) {
            verify(firstObserver).onStartupStateChanged(startupStateFromStorage)
            verify(firstObserver).onStartupStateChanged(startupState)
            verify(secondObserver).onStartupStateChanged(startupState)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun removeObserver() {
        startupStateHolder.init(context)
        startupStateHolder.registerObserver(firstObserver)
        startupStateHolder.registerObserver(secondObserver)
        startupStateHolder.removeObserver(firstObserver)
        startupStateHolder.onStartupStateChanged(startupState)

        inOrder(firstObserver, secondObserver) {
            verify(firstObserver).onStartupStateChanged(startupStateFromStorage)
            verify(secondObserver).onStartupStateChanged(startupStateFromStorage)
            verify(secondObserver).onStartupStateChanged(startupState)
            verifyNoMoreInteractions()
        }
    }
}
