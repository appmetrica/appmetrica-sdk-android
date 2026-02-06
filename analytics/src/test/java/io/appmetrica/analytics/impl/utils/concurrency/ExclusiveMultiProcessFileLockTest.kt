package io.appmetrica.analytics.impl.utils.concurrency

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer

internal class ExclusiveMultiProcessFileLockTest : CommonTest() {

    private val simpleFileName = "fileName"

    @get:Rule
    val contextRule = ContextRule()
    private val context: Context by contextRule
    private val file: File = mock()

    @get:Rule
    val fileUtilsMockedStaticRule = staticRule<FileUtils>()
    private val reentrantLock: ReentrantLock = mock()
    private val fileLocker: FileLocker = mock()
    private val exclusiveMultiProcessFileLock: ExclusiveMultiProcessFileLock by setUp {
        whenever(FileUtils.getFileFromSdkStorage(context, "$simpleFileName.lock")).thenReturn(file)
        ExclusiveMultiProcessFileLock(reentrantLock, fileLocker)
    }

    @Test
    fun constructor() {
        ObjectPropertyAssertions(ExclusiveMultiProcessFileLock(context, simpleFileName))
            .withPrivateFields(true)
            .checkFieldNonNull("reentrantLock")
            .checkFieldRecursively(
                "fileLocker",
                Consumer<ObjectPropertyAssertions<FileLocker>> { assertions ->
                    assertions.withPrivateFields(true)
                        .checkField("lockFile", file)
                        .checkAll()
                }
            )
            .checkAll()
    }

    @Test
    fun lock() {
        exclusiveMultiProcessFileLock.lock()
        inOrder(reentrantLock, fileLocker) {
            verify(reentrantLock).lock()
            verify(fileLocker).lock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun unlock() {
        exclusiveMultiProcessFileLock.unlock()
        inOrder(reentrantLock, fileLocker) {
            verify(fileLocker).unlock()
            verify(reentrantLock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun unlockAndClear() {
        exclusiveMultiProcessFileLock.unlockAndClear()
        inOrder(reentrantLock, fileLocker) {
            verify(fileLocker).unlockAndClear()
            verify(reentrantLock).unlock()
            verifyNoMoreInteractions()
        }
    }
}
