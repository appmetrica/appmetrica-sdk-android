package io.appmetrica.analytics.impl.servicecomponents

import io.appmetrica.analytics.coreutils.internal.system.SystemPropertiesHelper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever
import java.io.File

internal class OuterStoragePathProviderTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val databasesDirSystemProperty = "ro.yndx.metrica.db.dir"
    private val databasesDirDebugProperty = "debug.yndx.iaa.db.dir"

    private val systemRootDir by lazy { "${contextRule.dataDir}/system" }
    private val debugRootDir by lazy { "${contextRule.dataDir}/debug" }

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
            .isEqualTo(File("$debugRootDir/${ContextRule.PACKAGE_NAME}"))
            .isEmptyDirectory()
            .exists()
    }

    @Test
    fun `getPath if debug dir is empty`() {
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)).thenReturn(systemRootDir)
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)).thenReturn("")
        assertThat(pathProvider.getPath(context))
            .isNotNull()
            .isEqualTo(File("$systemRootDir/${ContextRule.PACKAGE_NAME}"))
            .isEmptyDirectory()
            .exists()
    }

    @Test
    fun `getPath if both dirs are not empty`() {
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)).thenReturn(systemRootDir)
        whenever(SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)).thenReturn(debugRootDir)
        assertThat(pathProvider.getPath(context))
            .isNotNull()
            .isEqualTo(File("$systemRootDir/${ContextRule.PACKAGE_NAME}"))
            .isEmptyDirectory()
            .exists()
    }
}
