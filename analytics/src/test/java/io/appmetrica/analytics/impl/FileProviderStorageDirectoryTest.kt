package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class FileProviderStorageDirectoryTest(
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
                    { fileProvider: FileProvider, context: Context ->
                        fileProvider.getStorageSubDirectory(context, "dir")
                    },
                    { dir: File -> File(dir, "dir").absolutePath },
                    "getStorageSubDirectory"
                ),
                arrayOf(
                    { fileProvider: FileProvider, context: Context ->
                        fileProvider.getStorageSubDirectoryFile(context, "dir")
                    },
                    { dir: File -> File(dir, "dir") },
                    "getStorageSubDirectoryFile"
                ),
                arrayOf(
                    { fileProvider: FileProvider, context: Context ->
                        fileProvider.getFileFromStorage(context, "file")
                    },
                    { dir: File -> File(dir, "file") },
                    "getFileFromStorage"
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
        context = TestUtils.createMockedContext()
        noBackupDir = RuntimeEnvironment.getApplication().noBackupFilesDir
        filesDir = RuntimeEnvironment.getApplication().filesDir
        fileProvider = FileProvider()
    }

    @Test
    fun lollipop() {
        whenever(context.noBackupFilesDir).thenReturn(noBackupDir)
        assertThat(applier(fileProvider, context)).isEqualTo(expected(noBackupDir!!))
    }

    @Test
    fun lollipopNull() {
        whenever(context.noBackupFilesDir).thenReturn(null)
        assertThat(applier(fileProvider, context)).isNull()
    }
}
