package io.appmetrica.analytics.coreutils.internal.io

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.File

class BufferedFileWriterTest : CommonTest() {

    private val testDir = File("test_buffered_writer")
    private val testFile = File(testDir, "test_file.txt")
    private val delayMillis = 1000L

    private lateinit var executor: IHandlerExecutor
    private lateinit var bufferedWriter: BufferedFileWriter
    private val runnableCaptor = argumentCaptor<Runnable>()

    @get:Rule
    val logRule = LogRule()

    @Before
    fun setUp() {
        testDir.mkdirs()
        executor = mock()
        bufferedWriter = BufferedFileWriter(executor, delayMillis, testFile)
    }

    @After
    fun tearDown() {
        testDir.deleteRecursively()
    }

    @Test
    fun `writeString schedules delayed task`() {
        bufferedWriter.writeString("test value")

        verify(executor).executeDelayed(any(), eq(delayMillis))
    }

    @Test
    fun `writeString does not cancel or reschedule task when called multiple times`() {
        bufferedWriter.writeString("value 1")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        bufferedWriter.writeString("value 2")
        bufferedWriter.writeString("value 3")

        // Should only schedule once
        verify(executor, times(1)).executeDelayed(any(), eq(delayMillis))
        // Should never remove tasks
        verify(executor, never()).remove(any())
    }

    @Test
    fun `readString returns pending value when available`() {
        bufferedWriter.writeString("pending value")

        val result = bufferedWriter.readString()

        assertThat(result).isEqualTo("pending value")
    }

    @Test
    fun `readString reads from file when no pending value`() {
        testFile.writeText("file content")

        val result = bufferedWriter.readString()

        assertThat(result).isEqualTo("file content")
    }

    @Test
    fun `readString returns null when file does not exist and no pending value`() {
        val result = bufferedWriter.readString()

        assertThat(result).isNull()
    }

    @Test
    fun `writeString task writes to file when executed`() {
        bufferedWriter.writeString("test content")

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(testFile.exists()).isTrue()
        assertThat(testFile.readText()).isEqualTo("test content")
    }

    @Test
    fun `writeString creates parent directory if needed`() {
        val nestedFile = File(testDir, "subdir/nested_file.txt")
        val nestedWriter = BufferedFileWriter(executor, delayMillis, nestedFile)

        nestedWriter.writeString("content")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(nestedFile.exists()).isTrue()
        assertThat(nestedFile.readText()).isEqualTo("content")
    }

    @Test
    fun `readString prefers pending value over file content`() {
        testFile.writeText("old content")

        bufferedWriter.writeString("new content")
        val result = bufferedWriter.readString()

        assertThat(result).isEqualTo("new content")
    }

    @Test
    fun `flush cancels pending task`() {
        bufferedWriter.writeString("value")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        val task = runnableCaptor.firstValue

        bufferedWriter.flush()

        verify(executor).remove(task)
    }

    @Test
    fun `flush writes pending value immediately`() {
        bufferedWriter.writeString("immediate value")

        bufferedWriter.flush()

        assertThat(testFile.exists()).isTrue()
        assertThat(testFile.readText()).isEqualTo("immediate value")
    }

    @Test
    fun `flush does nothing when no pending value`() {
        bufferedWriter.flush()

        verify(executor, never()).remove(any())
        assertThat(testFile.exists()).isFalse()
    }

    @Test
    fun `readString returns null after flush completes`() {
        bufferedWriter.writeString("value")
        bufferedWriter.flush()

        // Clear the file
        testFile.delete()

        val result = bufferedWriter.readString()

        assertThat(result).isNull()
    }

    @Test
    fun `multiple writeString calls only last value is written`() {
        bufferedWriter.writeString("value 1")
        bufferedWriter.writeString("value 2")
        bufferedWriter.writeString("value 3")

        // Only one task should be scheduled
        verify(executor, times(1)).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        val task = runnableCaptor.firstValue
        task.run()

        assertThat(testFile.readText()).isEqualTo("value 3")
    }

    @Test
    fun `writeString handles empty string`() {
        bufferedWriter.writeString("")

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(testFile.exists()).isTrue()
        assertThat(testFile.readText()).isEmpty()
    }

    @Test
    fun `writeString handles large string`() {
        val largeString = "x".repeat(10000)

        bufferedWriter.writeString(largeString)
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(testFile.readText()).isEqualTo(largeString)
    }

    @Test
    fun `readString returns pending value after multiple writes`() {
        bufferedWriter.writeString("first")
        bufferedWriter.writeString("second")
        bufferedWriter.writeString("third")

        val result = bufferedWriter.readString()

        assertThat(result).isEqualTo("third")
    }

    @Test
    fun `flush after task execution does nothing`() {
        bufferedWriter.writeString("value")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        bufferedWriter.flush()

        // File should still contain the value
        assertThat(testFile.readText()).isEqualTo("value")
    }

    @Test
    fun `writeString overwrites existing file content`() {
        testFile.writeText("old content")

        bufferedWriter.writeString("new content")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(testFile.readText()).isEqualTo("new content")
    }

    @Test
    fun `writeString schedules new task after previous task completes`() {
        // First batch
        bufferedWriter.writeString("batch 1 value 1")
        bufferedWriter.writeString("batch 1 value 2")
        verify(executor, times(1)).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(testFile.readText()).isEqualTo("batch 1 value 2")

        // Second batch - should schedule a new task
        bufferedWriter.writeString("batch 2 value 1")
        bufferedWriter.writeString("batch 2 value 2")
        verify(executor, times(2)).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.lastValue.run()

        assertThat(testFile.readText()).isEqualTo("batch 2 value 2")
    }

    @Test
    fun `readString caches value from file to avoid repeated disk reads`() {
        testFile.writeText("cached content")

        // First read should read from file
        val firstRead = bufferedWriter.readString()
        assertThat(firstRead).isEqualTo("cached content")

        // Modify file on disk
        testFile.writeText("modified content")

        // Second read should return cached value, not modified file content
        val secondRead = bufferedWriter.readString()
        assertThat(secondRead).isEqualTo("cached content")
    }

    @Test
    fun `readString does not cache null when file does not exist`() {
        // First read when file doesn't exist
        val firstRead = bufferedWriter.readString()
        assertThat(firstRead).isNull()

        // Create file
        testFile.writeText("new content")

        // Second read should read from file (null was not cached)
        val secondRead = bufferedWriter.readString()
        assertThat(secondRead).isEqualTo("new content")
    }

    @Test
    fun `writeString value takes precedence over file content in readString`() {
        // Create file with initial content
        testFile.writeText("old file content")

        // Write new value (not yet flushed)
        bufferedWriter.writeString("new pending value")

        // Read should return pending value, not file content
        val result = bufferedWriter.readString()
        assertThat(result).isEqualTo("new pending value")

        // File should still have old content
        assertThat(testFile.readText()).isEqualTo("old file content")
    }

    @Test
    fun `readString returns null when file read throws exception`() {
        // Create a file in a location that will cause read issues
        val readOnlyDir = File(testDir, "readonly")
        readOnlyDir.mkdirs()
        val problematicFile = File(readOnlyDir, "test.txt")
        problematicFile.writeText("content")

        // Make file unreadable by changing to directory (simulating read error)
        problematicFile.delete()
        problematicFile.mkdir()

        val writer = BufferedFileWriter(executor, delayMillis, problematicFile)

        // Should return null on read error
        val result = writer.readString()

        assertThat(result).isNull()

        // Cleanup
        problematicFile.deleteRecursively()
        readOnlyDir.deleteRecursively()
    }

    @Test
    fun `performWrite handles exception when writing to file`() {
        // Create a read-only directory to trigger write exception
        val readOnlyDir = File(testDir, "readonly_parent")
        readOnlyDir.mkdirs()
        val readOnlyFile = File(readOnlyDir, "readonly.txt")
        readOnlyFile.createNewFile()

        // Make directory read-only (on Unix-like systems)
        readOnlyDir.setWritable(false)

        val writer = BufferedFileWriter(executor, delayMillis, readOnlyFile)

        writer.writeString("test content")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        // Execute task - should handle exception gracefully
        runnableCaptor.firstValue.run()

        // Cleanup
        readOnlyDir.setWritable(true)
        readOnlyFile.delete()
        readOnlyDir.deleteRecursively()
    }
}
