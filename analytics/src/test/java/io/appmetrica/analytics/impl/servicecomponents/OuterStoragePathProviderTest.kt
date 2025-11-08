package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.system.SystemPropertiesHelper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class OuterStoragePathProviderTest : CommonTest() {

    private val dataDir = RuntimeEnvironment.getApplication().dataDir
    private val packageName = "io.appmetrica.analytics"
    private val context: Context = mock {
        on { packageName } doReturn packageName
    }

    private val databasesDirSystemProperty = "ro.yndx.metrica.db.dir"
    private val databasesDirDebugProperty = "debug.yndx.iaa.db.dir"

    private val systemRootDir = "$dataDir/system"
    private val debugRootDir = "$dataDir/debug"

    @get:Rule
    val systemPropertiesHelperRule = staticRule<SystemPropertiesHelper>()

    private val pathProvider by setUp { OuterStoragePathProvider() }

    @Test
    fun `getPath if both dirs are empty`() {
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)).thenReturn("")
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)).thenReturn("")
        assertThat(pathProvider.getPath(context)).isNull()
    }

    @Test
    fun `getPath if system dir is empty`() {
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)).thenReturn("")
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)).thenReturn(debugRootDir)
        assertThat(pathProvider.getPath(context))
            .isNotNull()
            .isEqualTo(File("$debugRootDir/$packageName"))
            .isEmptyDirectory()
            .exists()
    }

    @Test
    fun `getPath if debug dir is empty`() {
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)).thenReturn(systemRootDir)
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)).thenReturn("")
        assertThat(pathProvider.getPath(context))
            .isNotNull()
            .isEqualTo(File("$systemRootDir/$packageName"))
            .isEmptyDirectory()
            .exists()
    }

    @Test
    fun `getPath if both dirs are not empty`() {
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)).thenReturn(systemRootDir)
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)).thenReturn(debugRootDir)
        assertThat(pathProvider.getPath(context))
            .isNotNull()
            .isEqualTo(File("$systemRootDir/$packageName"))
            .isEmptyDirectory()
            .exists()
    }
}
