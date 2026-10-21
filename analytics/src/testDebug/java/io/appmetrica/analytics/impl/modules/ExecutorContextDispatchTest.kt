package io.appmetrica.analytics.impl.modules

import android.annotation.SuppressLint
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.modules.client.ClientStorageProviderImpl
import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.impl.modules.client.context.ClientContextImpl
import io.appmetrica.analytics.impl.modules.client.context.CoreModuleAdRevenueContextImpl
import io.appmetrica.analytics.impl.proxy.InternalClientModuleProxy
import io.appmetrica.analytics.impl.utils.executors.ExecutorWrapper
import io.appmetrica.analytics.impl.utils.executors.InterruptionSafeHandlerThread
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@SuppressLint("RobolectricUsage") // Real Looper dispatch through the module context bindings.
@RunWith(RobolectricTestRunner::class)
internal class ExecutorContextDispatchTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val networkContextRule = constructionRule<NetworkContextImpl>()

    @get:Rule
    val selfReporterRule = constructionRule<ModuleSelfReporterImpl>()

    @get:Rule
    val wakeLockRule = constructionRule<ServiceWakeLockImpl>()

    @get:Rule
    val wakeLockBinderRule = constructionRule<ServiceWakeLockBinder>()

    @get:Rule
    val intentProviderRule = constructionRule<AppMetricaServiceWakeLockIntentProvider>()

    @get:Rule
    val serviceStorageRule = constructionRule<ServiceStorageProviderImpl>()

    @get:Rule
    val adRevenueProcessorRule = constructionRule<CompositeModuleAdRevenueProcessor>()

    @get:Rule
    val adRevenueContextRule = constructionRule<CoreModuleAdRevenueContextImpl>()

    @get:Rule
    val clientStorageRule = constructionRule<ClientStorageProviderImpl>()

    @get:Rule
    val clientFacadeRule = constructionRule<InternalClientModuleProxy>()

    @Test
    fun `service context exposes conditional executors and a queued report executor`() = withExecutor { executor ->
        val backingProvider = globalServiceLocatorRule.serviceExecutorProviderMock
        whenever(backingProvider.defaultExecutor).thenReturn(executor)
        whenever(backingProvider.supportIOExecutor).thenReturn(executor)
        whenever(backingProvider.moduleExecutor).thenReturn(executor)
        whenever(backingProvider.reportRunnableExecutor).thenReturn(executor)
        val context = ServiceContextFacade(mock())
        val provider = context.executorProvider

        listOf(provider.getDefaultExecutor(), provider.getSupportIOExecutor(), provider.moduleExecutor).forEach {
            assertDispatch(it, executor, provider.getReportRunnableExecutor())
        }
    }

    @Test
    fun `client context exposes its conditional executor`() = withExecutor { executor ->
        whenever(clientServiceLocatorRule.clientExecutorProvider.defaultExecutor).thenReturn(executor)
        val context = ClientContextImpl(mock())

        assertDispatch(context.clientExecutorProvider.defaultExecutor, executor)
    }

    private fun assertDispatch(
        contextExecutor: IHandlerExecutor,
        backingExecutor: ExecutorWrapper,
        queuedReportExecutor: Executor? = null
    ) {
        assertThat(contextExecutor).isSameAs(backingExecutor)
        val order = Collections.synchronizedList(mutableListOf<String>())
        backingExecutor.submit(
            Callable {
                contextExecutor.post { order += "post" }
                contextExecutor.execute {
                    assertThat(Thread.currentThread()).isSameAs(backingExecutor.getLooper().thread)
                    order += "inline"
                }
                queuedReportExecutor?.execute { order += "report" }
                order += "outer-end"
                assertThat(order).containsExactly("inline", "outer-end")
            }
        ).get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        val completedOrder = backingExecutor.submit(Callable { order.toList() })
            .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        assertThat(completedOrder).containsExactlyElementsOf(
            if (queuedReportExecutor == null) {
                listOf("inline", "outer-end", "post")
            } else {
                listOf("inline", "outer-end", "post", "report")
            }
        )
    }

    private fun withExecutor(block: (ExecutorWrapper) -> Unit) {
        val thread = InterruptionSafeHandlerThread("ContextDispatchTest").apply { start() }
        try {
            block(ExecutorWrapper(thread))
        } finally {
            thread.quitSafely()
            thread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
            assertThat(thread.isAlive).isFalse()
        }
    }

    companion object {
        private const val TIMEOUT_SECONDS = 5L
    }
}
