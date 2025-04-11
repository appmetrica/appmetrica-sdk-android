package io.appmetrica.analytics.impl.crash.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.executors.BlockingExecutor
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ReadOldCrashesRunnable
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher
import io.appmetrica.analytics.impl.crash.jvm.service.CrashFromFileConsumer
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashService
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File

internal class ServiceCrashController(
    private val context: Context,
    private val reportsConsumer: ReportConsumer
) {

    private val tag = "[ServiceCrashController]"

    private val nativeCrashesService: NativeCrashService = GlobalServiceLocator.getInstance().nativeCrashService
    private val executor: ICommonExecutor =
        GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor

    private val crashesFromPreviousLaunchListener: Consumer<File> = CrashFromFileConsumer(
        context,
        reportsConsumer,
        InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE,
        AlwaysAllowSendCrashPredicate(),
        BlockingExecutor(),
        "previous"
    )

    private val crashesFromActualLaunchListener: Consumer<File> = CrashFromFileConsumer(
        context,
        reportsConsumer,
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
        JvmCrashFromCurrentSessionPredicate(),
        executor,
        "actual"
    )

    fun init() {
        FileUtils.getCrashesDirectory(context)?.let { crashDirectory ->
            DebugLogger.info(tag, "Setup crash directory watcher. Directory: ${crashDirectory.path}")
            val crashDirectoryWatcher = CrashDirectoryWatcher(crashDirectory, crashesFromActualLaunchListener)
            executor.execute(ReadOldCrashesRunnable(context, crashDirectory, crashesFromPreviousLaunchListener))
            crashDirectoryWatcher.startWatching()
        }
        nativeCrashesService.initNativeCrashReporting(context, reportsConsumer)
    }
}
