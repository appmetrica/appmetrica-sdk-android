package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

internal class OuterRootDatabaseFullPathProviderTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val root: File by setUp { File(contextRule.filesDir, "root_dir") }

    private val simplePath = "simple"
    private val relativePath = "relative/path"

    private val relativePathFormer = mock<RelativePathFormer> {
        on { preparePath(simplePath) } doReturn relativePath
    }

    private val outerRootDatabaseFullPathProvider by setUp {
        OuterRootDatabaseFullPathProvider(root, relativePathFormer)
    }

    @Test
    fun fullPath() {
        assertThat(outerRootDatabaseFullPathProvider.fullPath(context, simplePath).path)
            .isEqualTo("${root.path}/$relativePath")
    }
}
