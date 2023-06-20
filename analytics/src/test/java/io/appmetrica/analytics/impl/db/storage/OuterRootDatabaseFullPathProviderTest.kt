package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class OuterRootDatabaseFullPathProviderTest : CommonTest() {

    private lateinit var context: Context
    private lateinit var root: File

    private val simplePath = "simple"
    private val relativePath = "relative/path"

    private val relativePathFormer = mock<RelativePathFormer> {
        on { preparePath(simplePath) } doReturn relativePath
    }

    private lateinit var outerRootDatabaseFullPathProvider: OuterRootDatabaseFullPathProvider

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        root = File(context.dataDir, "root_dir")

        outerRootDatabaseFullPathProvider = OuterRootDatabaseFullPathProvider(root, relativePathFormer)
    }

    @Test
    fun fullPath() {
        assertThat(outerRootDatabaseFullPathProvider.fullPath(context, simplePath).path)
            .isEqualTo("${root.path}/$relativePath")
    }
}
