package io.appmetrica.analytics.impl.crash.jvm.client

import android.os.Looper
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.crash.utils.FullStateConverter
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper

internal class AnrFromApiReportingTask(
    private val anrReporter: AnrReporter,
    private val allThreads: Map<Thread, Array<StackTraceElement>>
) {

    private val mainThread: Thread = Looper.getMainLooper().thread

    fun execute() {
        val threadsStateDumper = ThreadsStateDumper(
            object : ThreadsStateDumper.ThreadProvider {
                override fun getMainThread(): Thread = this@AnrFromApiReportingTask.mainThread
                override fun getMainThreadStacktrace(): Array<StackTraceElement>? = allThreads[mainThread]
                override fun getAllThreadsStacktraces(): Map<Thread, Array<StackTraceElement>> = allThreads
            },
            FullStateConverter(),
            ClientServiceLocator.getInstance().processNameProvider
        )

        anrReporter.reportAnr(threadsStateDumper.threadsDumpForAnr)
    }
}
