package io.appmetrica.analytics.impl.proxy

import android.content.Context
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppMetricaLibraryAdapterProxyTest : CommonTest() {

    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }
    private val facade: AppMetricaFacade = mock()
    private val executor: IHandlerExecutor = mock()

    private val runnableArgumentCaptor = argumentCaptor<Runnable>()

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
        verify(executor).execute(runnableArgumentCaptor.capture())

        runnableArgumentCaptor.firstValue.run()

        verify(facade).activateFull()
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
        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportEvent(moduleEvent)
        }
    }
}
