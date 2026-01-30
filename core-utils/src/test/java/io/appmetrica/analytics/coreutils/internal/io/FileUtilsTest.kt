package io.appmetrica.analytics.coreutils.internal.io

import android.content.Context
import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileUtilsTest : CommonTest() {

    private val filesParentDir = File("filesParentDir")
    private val filesDir = File(filesParentDir, "filesDir")
    private val noBackupDir = File("noBackupDir")
    private val dataDir = File("dataDir")

    val context = mock<Context> {
        on { filesDir } doReturn filesDir
        on { noBackupFilesDir } doReturn noBackupDir
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            on { dataDir } doReturn dataDir
        }
    }

    @get:Rule
    val logRule = LogRule()

    @After
    fun tearDown() {
        FileUtils.resetSdkStorage()
        dataDir.deleteRecursively()
        filesParentDir.deleteRecursively()
        noBackupDir.deleteRecursively()
    }

    @Test
    fun `sdkStorage twice`() {
        val testFileName = "test_file.dat"
        val testText = "Test text"
        val sdkStorage = FileUtils.sdkStorage(context)
        File(sdkStorage, testFileName).writeText(testText)
        assertThat(File(FileUtils.sdkStorage(context), testFileName).readText()).isEqualTo(testText)
    }

    @Test
    fun getCrashesDirectory() {
        val file = FileUtils.getCrashesDirectory(context)
        assertThat(file!!.canonicalPath).endsWith("/appmetrica/analytics/crashes")
    }

    @Test
    fun getNativeCrashDirectory() {
        val file = FileUtils.getNativeCrashDirectory(context)
        assertThat(file!!.canonicalPath).endsWith("/appmetrica/analytics/native_crashes")
    }

    @Test
    fun getFileFromPath() {
        val path = "some/path/to/file"
        assertThat(FileUtils.getFileFromPath(path).path).isEqualTo(path)
    }

    @Test
    fun `getDataDir after N`() {
        assertThat(FileUtils.getAppDataDir(context)).isEqualTo(dataDir)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `getDataDir pre N`() {
        assertThat(FileUtils.getAppDataDir(context)).isEqualTo(filesParentDir)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `getData pre N if files dir is null`() {
        whenever(context.filesDir).thenReturn(null)
        assertThat(FileUtils.getAppDataDir(context)).isNull()
    }
}
