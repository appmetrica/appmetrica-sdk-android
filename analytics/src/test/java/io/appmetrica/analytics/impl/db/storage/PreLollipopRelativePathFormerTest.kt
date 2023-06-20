package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class PreLollipopRelativePathFormerTest : CommonTest() {

    private val simpleName = "simple"

    private lateinit var preLollipopRelativePathFormer: PreLollipopRelativePathFormer

    @Before
    fun setUp() {
        preLollipopRelativePathFormer = PreLollipopRelativePathFormer()
    }

    @Test
    fun preparePath() {
        assertThat(preLollipopRelativePathFormer.preparePath(simpleName))
            .isEqualTo("appmetrica_analytics_$simpleName")
    }
}
