package io.appmetrica.analytics.impl.crash.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.executors.BlockingExecutor
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ReadOldCrashesRunnable
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher
import io.appmetrica.analytics.impl.crash.jvm.service.CrashFromFileConsumer
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashService
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class ServiceCrashControllerTest : CommonTest() {

    private val context: Context = mock()
    private val reportConsumer: ReportConsumer = mock()

    @get:Rule
    val crashFromFileConsumerMockedConstructionRule = constructionRule<CrashFromFileConsumer>()

    @get:Rule
    val alwaysAllowSendCrashPredicateMockedConstructionRule =
        constructionRule<AlwaysAllowSendCrashPredicate<File>>()
    private val alwaysAllowSendCrashPredicate: AlwaysAllowSendCrashPredicate<File>
        by alwaysAllowSendCrashPredicateMockedConstructionRule

    @get:Rule
    val jvmCrashFromCurrentSessionPredicateMockedConstructionRule =
        constructionRule<JvmCrashFromCurrentSessionPredicate>()
    private val jvmCrashFromCurrentSessionPredicate: JvmCrashFromCurrentSessionPredicate
        by jvmCrashFromCurrentSessionPredicateMockedConstructionRule

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val executor: IHandlerExecutor = mock()
    private val nativeCrashService: NativeCrashService = mock()

    private val crashDirectory: File = mock()

    @get:Rule
    val fileUtilsMockedStaticRule = staticRule<FileUtils> {
        on { FileUtils.getCrashesDirectory(context) } doReturn crashDirectory
    }

    @get:Rule
    val crashDirectoryWatcherMockedConstructionRule = constructionRule<CrashDirectoryWatcher>()
    private val crashDirectoryWatcher by crashDirectoryWatcherMockedConstructionRule

    @get:Rule
    val readOldCrashesRunnableMockedConstructionRule = constructionRule<ReadOldCrashesRunnable>()
    private val readOldCrashesRunnable: ReadOldCrashesRunnable by readOldCrashesRunnableMockedConstructionRule

    @get:Rule
    val blockingExecutorMockedConstructionRule = constructionRule<BlockingExecutor>()
    private val blockingExecutor: BlockingExecutor by blockingExecutorMockedConstructionRule

    private lateinit var serviceCrashController: ServiceCrashController

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor)
            .thenReturn(executor)
        whenever(GlobalServiceLocator.getInstance().nativeCrashService)
            .thenReturn(nativeCrashService)
        serviceCrashController = ServiceCrashController(context, reportConsumer)
    }

    @Test
    fun init() {
        serviceCrashController.init()
        verify(executor).execute(readOldCrashesRunnable)
        verify(crashDirectoryWatcher).startWatching()
        nativeCrashService.initNativeCrashReporting(context, reportConsumer)
    }

    @Test
    fun `init if no crashes directory`() {
        whenever(FileUtils.getCrashesDirectory(context)).thenReturn(null)
        serviceCrashController.init()
        Assertions.assertThat(crashDirectoryWatcherMockedConstructionRule.constructionMock.constructed())
            .isEmpty()
        Assertions.assertThat(readOldCrashesRunnableMockedConstructionRule.constructionMock.constructed())
            .isEmpty()
        verifyNoInteractions(executor)
        verify(nativeCrashService).initNativeCrashReporting(context, reportConsumer)
    }

    @Test
    fun `init - check read old crashes runnable`() {
        serviceCrashController.init()
        Assertions.assertThat(readOldCrashesRunnableMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        Assertions.assertThat(readOldCrashesRunnableMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                crashDirectory,
                crashFromFileConsumerMockedConstructionRule.constructionMock.constructed().first()
            )
    }

    @Test
    fun `check crashes directory watcher`() {
        serviceCrashController.init()
        Assertions.assertThat(crashDirectoryWatcherMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        Assertions.assertThat(crashDirectoryWatcherMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                crashDirectory,
                crashFromFileConsumerMockedConstructionRule.constructionMock.constructed()[1]
            )
    }

    @Test
    fun `constructor - check crashesFromPreviousLaunchListener`() {
        Assertions.assertThat(crashFromFileConsumerMockedConstructionRule.argumentInterceptor.arguments[0])
            .containsExactly(
                context,
                reportConsumer,
                InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE,
                alwaysAllowSendCrashPredicate,
                blockingExecutor,
                "previous"
            )
    }

    @Test
    fun `constructor - check crashesFromActualLaunchListener`() {
        Assertions.assertThat(crashFromFileConsumerMockedConstructionRule.argumentInterceptor.arguments[1])
            .containsExactly(
                context,
                reportConsumer,
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
                jvmCrashFromCurrentSessionPredicate,
                executor,
                "actual"
            )
    }
}
