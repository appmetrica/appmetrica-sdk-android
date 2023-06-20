package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.utils.executors.NamedThreadFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExecutorProviderImplTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val namedThreadFactoryRule = MockedStaticRule(NamedThreadFactory::class.java)

    private val runnable = mock<Runnable>()
    private val thread = mock<InterruptionSafeThread>()

    private lateinit var executorProviderImpl: ExecutorProviderImpl

    @Before
    fun setUp() {
        executorProviderImpl = ExecutorProviderImpl()
    }

    @Test
    fun getDefaultExecutor() {
        assertThat(executorProviderImpl.getDefaultExecutor())
            .isEqualTo(GlobalServiceLocator.getInstance().serviceExecutorProvider.defaultExecutor)
    }

    @Test
    fun getSupportIOExecutor() {
        assertThat(executorProviderImpl.getSupportIOExecutor())
            .isEqualTo(GlobalServiceLocator.getInstance().serviceExecutorProvider.supportIOExecutor)
    }

    @Test
    fun getUiExecutor() {
        assertThat(executorProviderImpl.getUiExecutor())
            .isEqualTo(GlobalServiceLocator.getInstance().serviceExecutorProvider.uiExecutor)
    }

    @Test
    fun getInterruptionThread() {
        val moduleIdentifier = "moduleId"
        val threadNamePostfix = "threadPostfix"
        whenever(NamedThreadFactory.newThread("$moduleIdentifier-$threadNamePostfix", runnable))
            .thenReturn(thread)
        assertThat(executorProviderImpl.getInterruptionThread(moduleIdentifier, threadNamePostfix, runnable))
            .isEqualTo(thread)
    }
}
