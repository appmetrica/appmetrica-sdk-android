package io.appmetrica.analytics.coreutils.internal.io

import android.content.Context
import android.os.Build
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
class FileUtilsTest {

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
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `sdkStorage Lollipop`() {
        val result = FileUtils.sdkStorage(context)
        assertThat(result?.path).isEqualTo(noBackupDir.path + "/appmetrica/analytics")
        assertThat(File(noBackupDir, "/appmetrica/analytics").exists()).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `sdkStorage twice`() {
        val testFileName = "test_file.dat"
        val testText = "Test text"
        val sdkStorage = FileUtils.sdkStorage(context)
        File(sdkStorage, testFileName).writeText(testText)
        assertThat(File(FileUtils.sdkStorage(context), testFileName).readText()).isEqualTo(testText)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun getStorageDirectoryLollipop() {
        assertThat(FileUtils.getAppStorageDirectory(context)).isEqualTo(noBackupDir)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun getStorageDirectoryLollipopNull() {
        whenever(context.noBackupFilesDir).thenReturn(null)
        assertThat(FileUtils.getAppStorageDirectory(context)).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun getFileFromAppStorageLollipopIfFileDirIsNull() {
        whenever(context.noBackupFilesDir).thenReturn(null)
        assertThat(FileUtils.getFileFromAppStorage(context, "fileName")).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun getFileFromAppStorageLollipop() {
        val fileName = "fileName"
        val file = FileUtils.getFileFromAppStorage(context, fileName)
        assertThat(file!!.parentFile).isEqualTo(noBackupDir)
        assertThat(file.name).isEqualTo(fileName)
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
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun getFileFromSdkStorageLollipopIfFileDirIsNull() {
        whenever(context.noBackupFilesDir).thenReturn(null)
        assertThat(FileUtils.getFileFromSdkStorage(context, "fileName")).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun getFileFromSdkStorageLollipop() {
        val fileName = "fileName"
        val file = FileUtils.getFileFromSdkStorage(context, fileName)
        assertThat(file!!.parentFile!!.parentFile!!.parentFile).isEqualTo(noBackupDir)
        assertThat(file.name).isEqualTo(fileName)
    }

    @Test
    fun getFileFromPath() {
        val path = "some/path/to/file"
        assertThat(FileUtils.getFileFromPath(path).path).isEqualTo(path)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `getDataDir for N`() {
        assertThat(FileUtils.getAppDataDir(context)).isEqualTo(dataDir)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `getDataDir pre N`() {
        assertThat(FileUtils.getAppDataDir(context)).isEqualTo(filesParentDir)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `getData pre N if files dir is null`() {
        whenever(context.filesDir).thenReturn(null)
        assertThat(FileUtils.getAppDataDir(context)).isNull()
    }
}
