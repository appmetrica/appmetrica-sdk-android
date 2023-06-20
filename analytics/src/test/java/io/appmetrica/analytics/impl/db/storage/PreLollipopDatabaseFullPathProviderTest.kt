package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

internal class PreLollipopDatabaseFullPathProviderTest : CommonTest() {

    private val simpleName = "simple_name"
    private val relativePath = "relative_path"

    private val databasePath = File("databases")
    private val context: Context = mock {
        on { getDatabasePath(relativePath) } doReturn databasePath
    }

    private val relativePathFormer = mock<RelativePathFormer> {
        on { preparePath(simpleName) } doReturn relativePath
    }

    private lateinit var preLollipopDatabaseFullPathProvider: PreLollipopDatabaseFullPathProvider

    @Before
    fun setUp() {
        preLollipopDatabaseFullPathProvider = PreLollipopDatabaseFullPathProvider(relativePathFormer)
    }

    @Test
    fun fullPath() {
        assertThat(preLollipopDatabaseFullPathProvider.fullPath(context, simpleName)).isEqualTo(databasePath)
    }
}
