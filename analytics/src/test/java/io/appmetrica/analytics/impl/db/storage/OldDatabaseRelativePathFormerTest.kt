package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class OldDatabaseRelativePathFormerTest : CommonTest() {

    private val simpleName = "Simple name"

    private lateinit var oldDatabaseRelativePathFormer: OldDatabaseRelativePathFormer

    @Before
    fun setUp() {
        oldDatabaseRelativePathFormer = OldDatabaseRelativePathFormer()
    }

    @Test
    fun preparePath() {
        assertThat(oldDatabaseRelativePathFormer.preparePath(simpleName)).isEqualTo(simpleName)
    }
}
