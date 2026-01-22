package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.events.LibraryEventConstructor
import io.appmetrica.analytics.impl.proxy.synchronous.LibraryAdapterSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.LibraryAdapterBarrier
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class AppMetricaLibraryAdapterProxyTest : CommonTest() {

    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }
    private val facade: AppMetricaFacade = mock()
    private val executor: IHandlerExecutor = mock()

    private val config: AppMetricaLibraryAdapterConfig = mock()

    @get:Rule
    var clientServiceLocatorRule: ClientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val barrierRule = constructionRule<LibraryAdapterBarrier>()

    @get:Rule
    val synchronousStageExecutorRule = constructionRule<LibraryAdapterSynchronousStageExecutor>()

    @get:Rule
    val libraryEventConstructorRule = constructionRule<LibraryEventConstructor>()

    @get:Rule
    val modulesFacadeRule = staticRule<ModulesFacade>()

    private val runnableCaptor = argumentCaptor<Runnable>()

    private lateinit var barrier: LibraryAdapterBarrier
    private lateinit var synchronousStageExecutor: LibraryAdapterSynchronousStageExecutor
    private lateinit var libraryEventConstructor: LibraryEventConstructor

    private lateinit var proxy: AppMetricaLibraryAdapterProxy

    @Before
    fun setUp() {
        whenever(
            ClientServiceLocator.getInstance()
                .appMetricaFacadeProvider
                .getInitializedImpl(applicationContext)
        ).thenReturn(facade)
        whenever(
            ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor
        ).thenReturn(executor)

        proxy = AppMetricaLibraryAdapterProxy()

        barrier = barrierRule.constructionMock.constructed().first()
        whenever(barrier.activate(any())).thenReturn(true)
        whenever(barrier.activate(any(), any())).thenReturn(true)
        whenever(barrier.reportEvent(any(), any(), any())).thenReturn(true)
        whenever(barrier.setAdvIdentifiersTracking(any())).thenReturn(true)
        synchronousStageExecutor =
            synchronousStageExecutorRule.constructionMock.constructed().first()
        libraryEventConstructor =
            libraryEventConstructorRule.constructionMock.constructed().first()
    }

    @Test
    fun activate() {
        proxy.activate(context)
        verify(barrier).activate(context)
        verify(synchronousStageExecutor).activate(applicationContext)
    }

    @Test
    fun `activate with config`() {
        proxy.activate(context, config)
        verify(barrier).activate(context, config)
        verify(synchronousStageExecutor).activate(applicationContext, config)
    }

    @Test
    fun `activate if not valid`() {
        whenever(barrier.activate(context)).thenReturn(false)
        proxy.activate(context)
        verifyNoInteractions(synchronousStageExecutor)
    }

    @Test
    fun `activate with config if not valid`() {
        whenever(barrier.activate(context, config)).thenReturn(false)
        proxy.activate(context, config)
        verifyNoInteractions(synchronousStageExecutor)
    }

    @Test
    fun setAdvIdentifiersTracking() {
        proxy.setAdvIdentifiersTracking(true)
        verify(barrier).setAdvIdentifiersTracking(true)
        verify(synchronousStageExecutor).setAdvIdentifiersTracking(true)
        modulesFacadeRule.staticMock.verify {
            ModulesFacade.setAdvIdentifiersTracking(true)
        }
    }

    @Test
    fun `setAdvIdentifiersTracking if not valid`() {
        whenever(barrier.setAdvIdentifiersTracking(true)).thenReturn(false)
        proxy.setAdvIdentifiersTracking(true)
        verifyNoInteractions(synchronousStageExecutor, executor)
        modulesFacadeRule.staticMock.verify({ ModulesFacade.setAdvIdentifiersTracking(any()) }, never())
    }

    @Test
    fun reportEvent() {
        val sender = "sender_value"
        val event = "event_value"
        val payload = "payload_value"
        val moduleEvent: ModuleEvent = mock()

        whenever(libraryEventConstructor.constructEvent(sender, event, payload))
            .thenReturn(moduleEvent)

        proxy.reportEvent(sender, event, payload)
        verify(barrier).reportEvent(sender, event, payload)
        verify(synchronousStageExecutor).reportEvent(sender, event, payload)

        verify(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor)
            .execute(runnableCaptor.capture())

        runnableCaptor.firstValue.run()

        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportEvent(moduleEvent)
        }
    }

    @Test
    fun `reportEvent if not valid`() {
        val sender = "sender_value"
        val event = "event_value"
        val payload = "payload_value"

        whenever(barrier.reportEvent(sender, event, payload)).thenReturn(false)
        proxy.reportEvent(sender, event, payload)
        verifyNoInteractions(synchronousStageExecutor)
        modulesFacadeRule.staticMock.verify({ ModulesFacade.reportEvent(any()) }, never())
    }

    @Test
    fun subscribeForAutoCollectedData() {
        val apiKey = "apiKey"
        whenever(barrier.subscribeForAutoCollectedData(context, apiKey)).thenReturn(true)
        proxy.subscribeForAutoCollectedData(context, apiKey)
        verify(synchronousStageExecutor).subscribeForAutoCollectedData(context, apiKey)
        modulesFacadeRule.staticMock.verify {
            ModulesFacade.subscribeForAutoCollectedData(context, apiKey)
        }
    }

    @Test
    fun `subscribeForAutoCollectedData if not valid`() {
        val apiKey = "apiKey"
        whenever(barrier.subscribeForAutoCollectedData(context, apiKey)).thenReturn(false)
        proxy.subscribeForAutoCollectedData(context, apiKey)
        verifyNoInteractions(synchronousStageExecutor)
        modulesFacadeRule.staticMock.verify({ ModulesFacade.subscribeForAutoCollectedData(any(), any()) }, never())
    }
}
