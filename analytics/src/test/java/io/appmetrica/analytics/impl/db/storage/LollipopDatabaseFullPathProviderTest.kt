package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
@RunWith(RobolectricTestRunner::class)
internal class LollipopDatabaseFullPathProviderTest : CommonTest() {

    private lateinit var context: Context

    private val simpleName = "simplepath"
    private val preparedPath = "preparedpath"
    private val relativePathFormer = mock<RelativePathFormer> {
        on { preparePath(simpleName) } doReturn preparedPath
    }

    private lateinit var lollipopDatabaseFullPathProvider: LollipopDatabaseFullPathProvider

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()

        lollipopDatabaseFullPathProvider = LollipopDatabaseFullPathProvider(relativePathFormer)
    }

    @Test
    fun fullPath() {
        assertThat(lollipopDatabaseFullPathProvider.fullPath(context, simpleName).path)
            .isEqualTo("${context.noBackupFilesDir}/$preparedPath")
    }
}
