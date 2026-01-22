package io.appmetrica.analytics.impl.crash

import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.backport.Function
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class ReadAndReportRunnableTest : CommonTest() {

    private val crashFileName = "crash file name"

    private val crashFile: File = mock {
        on { name } doReturn crashFileName
        on { exists() } doReturn true
    }

    private val crash: JvmCrash = mock()

    private val fileReader: Function<File, JvmCrash?> = mock {
        on { apply(crashFile) } doReturn crash
    }

    private val finalizator: Consumer<File> = mock()
    private val crashConsumer: Consumer<JvmCrash> = mock()

    private val fileLock: ExclusiveMultiProcessFileLock = mock()

    private val fileLocksHolder: FileLocksHolder = mock {
        on { getOrCreate(crashFileName) } doReturn fileLock
    }

    private val shouldSendCrashPredicate: ShouldSendCrashNowPredicate<JvmCrash> = mock {
        on { shouldSend(crash) } doReturn true
    }

    private val readAndReportRunnable: ReadAndReportRunnable<JvmCrash> by setUp {
        ReadAndReportRunnable(
            crashFile,
            fileReader,
            finalizator,
            crashConsumer,
            fileLocksHolder,
            shouldSendCrashPredicate
        )
    }

    @Test
    fun run() {
        readAndReportRunnable.run()
        inOrder(fileLocksHolder, fileLock, crashConsumer, finalizator) {
            verify(fileLock).lock()
            verify(crashConsumer).consume(crash)
            verify(finalizator).consume(crashFile)
            verify(fileLock).unlockAndClear()
            verify(fileLocksHolder).clear(crashFileName)
        }
    }

    @Test
    fun `run if no crash file`() {
        whenever(crashFile.exists()).thenReturn(false)
        readAndReportRunnable.run()
        verifyNoInteractions(fileLocksHolder, fileLock, crashConsumer, finalizator)
    }

    @Test
    fun `run if crash is null`() {
        whenever(fileReader.apply(crashFile)).thenReturn(null)
        readAndReportRunnable.run()
        verifyNoInteractions(crashConsumer)
        inOrder(fileLock, finalizator, fileLocksHolder) {
            verify(fileLock).lock()
            verify(finalizator).consume(crashFile)
            verify(fileLock).unlockAndClear()
            verify(fileLocksHolder).clear(crashFileName)
        }
    }

    @Test
    fun `run if throw exception`() {
        whenever(fileReader.apply(crashFile)).thenThrow(RuntimeException())
        readAndReportRunnable.run()
        verifyNoInteractions(crashConsumer)
        inOrder(fileLock, finalizator, fileLocksHolder) {
            verify(fileLock).lock()
            verify(finalizator).consume(crashFile)
            verify(fileLock).unlockAndClear()
            verify(fileLocksHolder).clear(crashFileName)
        }
    }

    @Test
    fun `run if should not send crash predicate`() {
        whenever(shouldSendCrashPredicate.shouldSend(crash)).thenReturn(false)
        readAndReportRunnable.run()
        verifyNoInteractions(crashConsumer)
        inOrder(fileLock, finalizator, fileLocksHolder) {
            verify(fileLock).lock()
            verify(finalizator, never()).consume(crashFile)
            verify(fileLock).unlockAndClear()
            verify(fileLocksHolder).clear(crashFileName)
        }
    }
}
