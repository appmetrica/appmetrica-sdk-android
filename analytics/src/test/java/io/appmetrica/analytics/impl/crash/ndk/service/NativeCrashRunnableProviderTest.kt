package io.appmetrica.analytics.impl.crash.ndk.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.impl.FileProvider
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable
import io.appmetrica.analytics.impl.crash.jvm.converter.NativeCrashConverter
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashDumpReader
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

internal class NativeCrashRunnableProviderTest : CommonTest() {

    private val context: Context = mock()
    private val reportConsumer: ReportConsumer = mock()

    private val eventType = InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF

    private val dumpFilePath = "Dump file"
    private val nativeCrashFile: File = mock()

    private val nativeCrashSource: NativeCrashSource = mock()
    private val handlerVersion = "100500"
    private val nativeCrashMetadata: AppMetricaNativeCrashMetadata = mock()

    private val nativeCrash: AppMetricaNativeCrash = mock {
        on { dumpFile } doReturn dumpFilePath
        on { source } doReturn nativeCrashSource
        on { handlerVersion } doReturn handlerVersion
        on { metadata } doReturn nativeCrashMetadata
    }

    private val finalizer: Consumer<File> = mock()

    private val shouldSendCrashPredicate: ShouldSendCrashNowPredicate<String> = mock()

    private val shouldSendCrashPredicateProvider: NativeShouldSendCrashPredicateProvider = mock {
        on { predicate(nativeCrash) } doReturn shouldSendCrashPredicate
    }

    @get:Rule
    val fileProviderMockedConstructionRule = constructionRule<FileProvider> {
        on { getFileByNonNullPath(dumpFilePath) } doReturn nativeCrashFile
    }

    @get:Rule
    val nativeCrashConverterMockedConstructionRule = constructionRule<NativeCrashConverter>()
    private val nativeCrashConverter: NativeCrashConverter by nativeCrashConverterMockedConstructionRule

    @get:Rule
    val nativeCrashHandlerDescriptionRule = constructionRule<NativeCrashHandlerDescription>()
    private val nativeCrashHandlerDescription: NativeCrashHandlerDescription by nativeCrashHandlerDescriptionRule

    @get:Rule
    val nativeCrashDumpReaderMockedConstructionRule = constructionRule<NativeCrashDumpReader>()
    private val nativeCrashDumpReader: NativeCrashDumpReader by nativeCrashDumpReaderMockedConstructionRule

    @get:Rule
    val nativeCrashReporterCreatorMockedConstructionRule = constructionRule<NativeCrashReportCreator>()
    private val nativeCrashReportCreator: NativeCrashReportCreator by nativeCrashReporterCreatorMockedConstructionRule

    @get:Rule
    val crashConsumerMockedConstructionRule = constructionRule<NativeCrashConsumer>()
    private val crashConsumer: NativeCrashConsumer by crashConsumerMockedConstructionRule

    private val fileLocksHolder: FileLocksHolder = mock()

    @get:Rule
    val readAndReportRunnableMockedConstructionRule = constructionRule<ReadAndReportRunnable<String>>()
    private val readAndReportRunnable: ReadAndReportRunnable<String> by readAndReportRunnableMockedConstructionRule

    private lateinit var nativeCrashRunnableProvider: NativeCrashRunnableProvider

    @Before
    fun setUp() {
        FileLocksHolder.stubInstance(fileLocksHolder)
        nativeCrashRunnableProvider =
            NativeCrashRunnableProvider(context, reportConsumer, shouldSendCrashPredicateProvider, eventType)
    }

    @Test
    fun get() {
        assertThat(nativeCrashRunnableProvider.get(nativeCrash, finalizer)).isEqualTo(readAndReportRunnable)

        assertThat(readAndReportRunnableMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(readAndReportRunnableMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                nativeCrashFile,
                nativeCrashDumpReader,
                finalizer,
                crashConsumer,
                fileLocksHolder,
                shouldSendCrashPredicate
            )
    }

    @Test
    fun `get - check crash consumer`() {
        nativeCrashRunnableProvider.get(nativeCrash, finalizer)

        assertThat(crashConsumerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(crashConsumerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(reportConsumer, nativeCrashMetadata, nativeCrashReportCreator)
    }

    @Test
    fun `get - check reports creator`() {
        nativeCrashRunnableProvider.get(nativeCrash, finalizer)

        assertThat(nativeCrashReporterCreatorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeCrashReporterCreatorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(nativeCrash, eventType)
    }

    @Test
    fun `get - check crash dump reader`() {
        nativeCrashRunnableProvider.get(nativeCrash, finalizer)

        assertThat(nativeCrashDumpReaderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeCrashDumpReaderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(nativeCrashHandlerDescription, nativeCrashConverter)
    }

    @Test
    fun `get check handler description`() {
        nativeCrashRunnableProvider.get(nativeCrash, finalizer)

        assertThat(nativeCrashHandlerDescriptionRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeCrashHandlerDescriptionRule.argumentInterceptor.flatArguments())
            .containsExactly(nativeCrashSource, handlerVersion)
    }
}
