package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class DatabaseFullPathProviderImplTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val simpleName = "simplepath"
    private val preparedPath = "preparedpath"
    private val relativePathFormer = mock<RelativePathFormer> {
        on { preparePath(simpleName) } doReturn preparedPath
    }

    private val databaseFullPathProviderImpl by setUp { DatabaseFullPathProviderImpl(relativePathFormer) }

    @Test
    fun fullPath() {
        assertThat(databaseFullPathProviderImpl.fullPath(context, simpleName).path)
            .isEqualTo("${context.noBackupFilesDir}/$preparedPath")
    }
}
