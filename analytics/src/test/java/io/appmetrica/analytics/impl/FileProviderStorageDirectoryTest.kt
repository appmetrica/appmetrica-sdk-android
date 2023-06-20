package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(ParameterizedRobolectricTestRunner::class)
class FileProviderStorageDirectoryTest(
    private val applier: (FileProvider, Context) -> Any,
    private val expected: (File) -> Any,
    description: String
) : CommonTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{2}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    { fileProvider: FileProvider, context: Context -> fileProvider.getStorageSubDirectory(context, "dir") },
                    { dir: File -> File(dir, "dir").absolutePath },
                    "getStorageSubDirectory"
                ),
                arrayOf(
                    { fileProvider: FileProvider, context: Context -> fileProvider.getStorageSubDirectoryFile(context, "dir") },
                    { dir: File -> File(dir, "dir") },
                    "getStorageSubDirectoryFile"
                ),
                arrayOf(
                    { fileProvider: FileProvider, context: Context -> fileProvider.getFileFromStorage(context, "file") },
                    { dir: File -> File(dir, "file") },
                    "getFileFromStorage"
                ),
                arrayOf(
                    { fileProvider: FileProvider, context: Context -> fileProvider.getCrashesDirectory(context) },
                    { dir: File -> File(dir, "appmetrica_crashes") },
                    "getCrashesDirectory"
                ),
            )
        }
    }

    private lateinit var context: Context
    private lateinit var fileProvider: FileProvider
    private var noBackupDir: File? = null
    private lateinit var filesDir: File

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noBackupDir = RuntimeEnvironment.getApplication().noBackupFilesDir
        }
        filesDir = RuntimeEnvironment.getApplication().filesDir
        fileProvider = FileProvider()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun lollipop() {
        `when`(context.noBackupFilesDir).thenReturn(noBackupDir)
        assertThat(applier(fileProvider, context)).isEqualTo(expected(noBackupDir!!))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun lollipopNull() {
        `when`(context.noBackupFilesDir).thenReturn(null)
        assertThat(applier(fileProvider, context)).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    fun preLollipop() {
        `when`(context.filesDir).thenReturn(filesDir)
        assertThat(applier(fileProvider, context)).isEqualTo(expected(filesDir))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    fun preLollipopNull() {
        `when`(context.filesDir).thenReturn(null)
        assertThat(applier(fileProvider, context)).isNull()
    }
}
