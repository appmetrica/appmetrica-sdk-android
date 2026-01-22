package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class FileVitalDataSourceTest : CommonTest() {

    private val fileName = "appmetrica_vital.dat"

    private lateinit var file: File
    private lateinit var context: Context

    private lateinit var fileVitalDataSource: FileVitalDataSource

    @get:Rule
    val yandexSelfReportingFacadeMockedRule = MockedStaticRule(AppMetricaSelfReportFacade::class.java)

    @get:Rule
    val fileUtilsMockedStaticRule = MockedStaticRule(FileUtils::class.java)

    private val selfReporter = mock<SelfReporterWrapper>()

    @Before
    fun setUp() {
        context = TestUtils.createMockedContext()
        file = File(RuntimeEnvironment.getApplication().filesDir, "some_ƒile")
        whenever(FileUtils.getFileFromSdkStorage(context, fileName)).thenReturn(file)
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporter)
        fileVitalDataSource = FileVitalDataSource(context, fileName)
    }

    @After
    fun tearDown() {
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun someContent() {
        val content = "File content"
        file.writeText(content)
        assertThat(fileVitalDataSource.getVitalData()).isEqualTo(content)
    }

    @Test
    fun nullFile() {
        whenever(FileUtils.getFileFromSdkStorage(context, fileName)).thenReturn(null)
        assertThat(fileVitalDataSource.getVitalData()).isNull()
    }

    @Test
    fun fileWithEmptyContent() {
        file.writeText("")
        assertThat(fileVitalDataSource.getVitalData()).isEqualTo("")
    }

    @Test
    fun fileWithEmptyJson() {
        file.writeText(JSONObject().toString())
        assertThat(fileVitalDataSource.getVitalData()).isEqualTo(JSONObject().toString())
    }

    @Test
    fun anotherException() {
        whenever(FileUtils.getFileFromSdkStorage(context, fileName)).thenThrow(IllegalStateException())
        assertThat(fileVitalDataSource.getVitalData()).isNull()
        verifyNoMoreInteractions(selfReporter)
    }

    @Test
    fun putVitalData() {
        val content = "Vital data some content"
        fileVitalDataSource.putVitalData(content)
        assertThat(file.readText()).isEqualTo(content)
    }

    @Test
    fun putVitalDataIfFileIsNull() {
        whenever(FileUtils.getFileFromSdkStorage(context, fileName)).thenReturn(null)
        fileVitalDataSource.putVitalData("Vital data content")
        assertThat(file.exists()).isFalse()
    }

    @Test
    fun putVitalDataIfThrow() {
        whenever(FileUtils.getFileFromSdkStorage(context, fileName)).thenThrow(IllegalStateException())
        fileVitalDataSource.putVitalData("Vital data")
        assertThat(file.exists()).isFalse()
    }
}
