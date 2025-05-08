package io.appmetrica.analytics.impl.crash.ndk.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashConverter
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NativeCrashHandlerImplTest : CommonTest() {

    private val executor: IHandlerExecutor = mock()
    private val context: Context = mock()
    private val reportConsumer: ReportConsumer = mock()
    private val markCrashCompleted: (String) -> Unit = mock()
    private val predicateProvider: NativeShouldSendCrashPredicateProvider = mock()
    private val eventType = InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF
    private val subtag = "Subtag"
    private val pid = 100500
    private val apiKey = "api key"
    private val uuid = "Some uuid"

    private val nativeCrash: NativeCrash = mock {
        on { uuid } doReturn uuid
    }

    private val nativeCrashMetadata: AppMetricaNativeCrashMetadata = mock {
        on { processID } doReturn pid
        on { apiKey } doReturn apiKey
    }

    private val appMetricaNativeCrash: AppMetricaNativeCrash = mock {
        on { metadata } doReturn nativeCrashMetadata
        on { uuid } doReturn uuid
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val readAndReportRunnable: ReadAndReportRunnable<String> = mock()

    @get:Rule
    val nativeCrashRunnableProviderMockedConstructionRule = constructionRule<NativeCrashRunnableProvider> {
        on { get(eq(appMetricaNativeCrash), any()) } doReturn readAndReportRunnable
    }

    @get:Rule
    val appmetricaNativeCrashMockedStaticRule = staticRule<AppMetricaNativeCrashConverter> {
        on { AppMetricaNativeCrashConverter.from(nativeCrash) } doReturn appMetricaNativeCrash
    }

    private val logger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getOrCreatePublicLogger(apiKey) } doReturn logger
    }

    private lateinit var nativeCrashHandlerImpl: NativeCrashHandlerImpl

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor)
            .thenReturn(executor)
        nativeCrashHandlerImpl = NativeCrashHandlerImpl(
            context,
            reportConsumer,
            markCrashCompleted,
            predicateProvider,
            eventType,
            subtag
        )
    }

    @Test
    fun newCrash() {
        nativeCrashHandlerImpl.newCrash(nativeCrash)
        verify(logger).info(argThat { contains("Detected native crash with uuid = $uuid") })
        verify(executor).execute(readAndReportRunnable)
    }

    @Test
    fun `newCrash if null`() {
        whenever(AppMetricaNativeCrashConverter.from(nativeCrash)).thenReturn(null)
        nativeCrashHandlerImpl.newCrash(nativeCrash)
        verify(markCrashCompleted).invoke(uuid)
    }
}
