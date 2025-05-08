package io.appmetrica.analytics.impl.crash.jvm.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.ClientCounterReport
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.impl.crash.jvm.JvmCrashReader
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.File

class ReportCrashRunnableProviderTest : CommonTest() {

    private val apiKey = "api key"
    private val packageName = "packageName"
    private val pid = 100500
    private val psid = "psid"
    private val reporterType = CounterConfigurationReporterType.MAIN
    private val eventType = InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE
    private val name = "name"
    private val crashValue = "crash value".toByteArray()
    private val bytesTruncated = 100
    private val trimmedFields = hashMapOf<ClientCounterReport.TrimmedField, Int>()
    private val environment = "environment"

    private val context: Context = mock()
    private val crashEventConsumer: CrashEventConsumer = mock()
    private val crashPredicate: ShouldSendCrashNowPredicate<JvmCrash> = mock()

    @get:Rule
    val jvmCrashReaderMockedConstructionRule = constructionRule<JvmCrashReader>()
    private val jvmCrashReader: JvmCrashReader by jvmCrashReaderMockedConstructionRule

    @get:Rule
    val startupRequestConfigArgumentsRule = constructionRule<StartupRequestConfig.Arguments>()
    private val startupRequestConfigArguments: StartupRequestConfig.Arguments by startupRequestConfigArgumentsRule

    @get:Rule
    val reporterArgumentsMockedConstructionRule = constructionRule<CommonArguments.ReporterArguments>()
    private val reporterArguments: CommonArguments.ReporterArguments by reporterArgumentsMockedConstructionRule

    @get:Rule
    val commonArgumentsMockedConstructionRule = constructionRule<CommonArguments>()
    private val commonArguments: CommonArguments by commonArgumentsMockedConstructionRule

    @get:Rule
    val clientDescriptionMockedConstructionRule = constructionRule<ClientDescription>()
    private val clientDescription: ClientDescription by clientDescriptionMockedConstructionRule

    private val counterReport: CounterReport = mock()

    private val publicLogger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getOrCreatePublicLogger(apiKey) } doReturn publicLogger
    }

    @get:Rule
    val eventsManagerMockedStaticRule = staticRule<EventsManager> {
        on {
            EventsManager.unhandledExceptionFromFileReportEntry(
                eventType,
                name,
                crashValue,
                bytesTruncated,
                trimmedFields,
                environment,
                publicLogger
            )
        } doReturn counterReport
    }

    private val fileLocksHolder: FileLocksHolder = mock()

    private val crashFile: File = mock()

    @get:Rule
    val readAndReportRunnableMockedConstructionRule = constructionRule<ReadAndReportRunnable<JvmCrash>>()
    private val readAndReportRunnable: ReadAndReportRunnable<JvmCrash> by readAndReportRunnableMockedConstructionRule

    private val jvmCrash: JvmCrash = mock {
        on { name } doReturn name
        on { crashValue } doReturn crashValue
        on { bytesTruncated } doReturn bytesTruncated
        on { trimmedFields } doReturn trimmedFields
        on { environment } doReturn environment
        on { apiKey } doReturn apiKey
        on { packageName } doReturn packageName
        on { pid } doReturn pid
        on { psid } doReturn psid
        on { reporterType } doReturn reporterType
    }

    private lateinit var reportCrashRunnableProvider: ReportCrashRunnableProvider

    @Before
    fun setUp() {
        FileLocksHolder.stubInstance(fileLocksHolder)

        reportCrashRunnableProvider =
            ReportCrashRunnableProvider(context, crashEventConsumer, eventType, crashPredicate)
    }

    @Test
    fun get() {
        assertThat(reportCrashRunnableProvider.get(crashFile)).isEqualTo(readAndReportRunnable)

        assertThat(readAndReportRunnableMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        val arguments = readAndReportRunnableMockedConstructionRule.argumentInterceptor.flatArguments()
        SoftAssertions().apply {
            assertThat(arguments).hasSize(6)
            assertThat(arguments[0]).isEqualTo(crashFile)
            assertThat(arguments[1]).isEqualTo(jvmCrashReader)
            assertThat(arguments[2]).isEqualTo(jvmCrashReader)
            assertThat(arguments[3]).isNotNull() // Detailed check consumer in separate test
            assertThat(arguments[4]).isEqualTo(fileLocksHolder)
            assertThat(arguments[5]).isEqualTo(crashPredicate)
            assertAll()
        }
    }

    @Test
    fun `get - check arguments`() {
        reportCrashRunnableProvider.get(crashFile)

        assertThat(commonArgumentsMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(commonArgumentsMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(startupRequestConfigArguments, reporterArguments, null)

        assertThat(startupRequestConfigArgumentsRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupRequestConfigArgumentsRule.argumentInterceptor.flatArguments()).isEmpty()

        assertThat(reporterArgumentsMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterArgumentsMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `get - check crash consumer`() {
        reportCrashRunnableProvider.get(crashFile)

        val consumer: Consumer<JvmCrash> =
            readAndReportRunnableMockedConstructionRule.argumentInterceptor.flatArguments()[3] as Consumer<JvmCrash>

        consumer.consume(jvmCrash)
        verify(crashEventConsumer).consumeCrash(clientDescription, counterReport, commonArguments)

        assertThat(clientDescriptionMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(clientDescriptionMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(apiKey, packageName, pid, psid, reporterType)
    }
}
