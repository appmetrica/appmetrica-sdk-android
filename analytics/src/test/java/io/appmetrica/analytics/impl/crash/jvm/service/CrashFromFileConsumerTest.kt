package io.appmetrica.analytics.impl.crash.jvm.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class CrashFromFileConsumerTest : CommonTest() {

    private val context: Context = mock()
    private val crashEventConsumer: CrashEventConsumer = mock()
    private val eventType: InternalEvents = mock()
    private val crashPredicate: ShouldSendCrashNowPredicate<JvmCrash> = mock()
    private val subtag = "Subtag"
    private val file: File = mock()

    private val executor: IHandlerExecutor = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val readAndReportRunnable: ReadAndReportRunnable<JvmCrash> = mock()

    @get:Rule
    val reportCrashRunnableProviderMockedConstructionRule = constructionRule<ReportCrashRunnableProvider> {
        on { get(file) } doReturn readAndReportRunnable
    }

    private lateinit var crashFromFileConsumer: CrashFromFileConsumer

    @Before
    fun setUp() {
        crashFromFileConsumer = CrashFromFileConsumer(
            context,
            crashEventConsumer,
            eventType,
            crashPredicate,
            executor,
            subtag
        )
    }

    @Test
    fun reportCrashRunnable() {
        assertThat(reportCrashRunnableProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reportCrashRunnableProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, crashEventConsumer, eventType, crashPredicate)
    }

    @Test
    fun consume() {
        crashFromFileConsumer.consume(file)
        verify(executor).execute(readAndReportRunnable)
    }

    @Test
    fun `consume for null file`() {
        crashFromFileConsumer.consume(null)
        verifyNoInteractions(executor)
    }
}
