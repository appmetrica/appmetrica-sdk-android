package io.appmetrica.analytics.impl.utils

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.db.FileConstants
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner
import java.io.File

@RunWith(ParameterizedRobolectricTestRunner::class)
class FirstLaunchDetectorTest(
    private val uuidFileExists: Boolean?,
    private val legacyUuidFileExists: Boolean?,
    private val expectedValue: Boolean
) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}, {1} -> {2}")
        @JvmStatic
        fun data(): List<Array<Any?>> = listOf(
            arrayOf(true, true, true),
            arrayOf(true, false, true),
            arrayOf(false, true, true),
            arrayOf(false, false, false),
            arrayOf(true, null, true),
            arrayOf(null, true, true),
            arrayOf(null, null, false),
            arrayOf(null, false, false),
            arrayOf(false, null, false)
        )
    }

    @get:Rule
    val fileUtilsMockedStaticRule = staticRule<FileUtils>()

    private val context: Context = mock()
    private val actualFile: File = mock()
    private val legacyFile: File = mock()

    private val firstLaunchDetector: FirstLaunchDetector by setUp { FirstLaunchDetector() }

    @Before
    fun setUp() {
        if (uuidFileExists != null) {
            whenever(actualFile.exists()).thenReturn(uuidFileExists)
            whenever(FileUtils.getFileFromSdkStorage(context, FileConstants.UUID_FILE_NAME)).thenReturn(actualFile)
        }
        if (legacyUuidFileExists != null) {
            whenever(legacyFile.exists()).thenReturn(legacyUuidFileExists)
            whenever(FileUtils.getFileFromAppStorage(context, FileConstants.UUID_FILE_NAME)).thenReturn(legacyFile)
        }
    }

    @Test
    fun isNotFirstLaunch() {
        // Before init
        assertThat(firstLaunchDetector.isNotFirstLaunch()).isEqualTo(false)

        // Init
        firstLaunchDetector.init(context)
        assertThat(firstLaunchDetector.isNotFirstLaunch()).isEqualTo(expectedValue)

        // Second init
        whenever(FileUtils.getFileFromSdkStorage(context, FileConstants.UUID_FILE_NAME)).thenReturn(null)
        whenever(FileUtils.getFileFromAppStorage(context, FileConstants.UUID_FILE_NAME)).thenReturn(null)
        firstLaunchDetector.init(context)
        assertThat(firstLaunchDetector.isNotFirstLaunch()).isEqualTo(expectedValue)
    }
}
