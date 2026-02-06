package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.whenever
import java.io.File

@RunWith(Parameterized::class)
internal class FileProviderStorageDirectoryTest(
    private val applier: (FileProvider, Context) -> Any,
    private val expected: (File) -> Any,
    @Suppress("unused") description: String
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
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

    @get:Rule
    val contextRule = ContextRule()
    private lateinit var context: Context
    private lateinit var fileProvider: FileProvider

    @Before
    fun setUp() {
        context = contextRule.context
        fileProvider = FileProvider()
    }

    @Test
    fun checkForNull() {
        assertThat(applier(fileProvider, context)).isEqualTo(expected(context.noBackupFilesDir))
    }

    @Test
    fun checkForNonNull() {
        whenever(context.noBackupFilesDir).thenReturn(null)
        assertThat(applier(fileProvider, context)).isNull()
    }
}
