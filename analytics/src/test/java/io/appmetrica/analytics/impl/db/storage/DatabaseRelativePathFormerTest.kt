package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class DatabaseRelativePathFormerTest : CommonTest() {

    private val simpleName = "simple_name"
    private lateinit var databaseRelativePathFormer: DatabaseRelativePathFormer

    @Before
    fun setUp() {
        databaseRelativePathFormer = DatabaseRelativePathFormer()
    }

    @Test
    fun preparePath() {
        assertThat(databaseRelativePathFormer.preparePath(simpleName)).isEqualTo("/appmetrica/analytics/db/$simpleName")
    }
}
