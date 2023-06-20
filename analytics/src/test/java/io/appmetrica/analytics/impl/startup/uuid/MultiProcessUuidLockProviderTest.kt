package io.appmetrica.analytics.impl.startup.uuid

import android.content.Context
import io.appmetrica.analytics.impl.db.FileConstants
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class MultiProcessUuidLockProviderTest : CommonTest() {

    @get:Rule
    val exclusiveMultiProcessFileLockMockedConstructionRule =
        MockedConstructionRule(ExclusiveMultiProcessFileLock::class.java)

    private val context = mock<Context>()

    @Before
    fun setUp() {
        MultiProcessUuidLockProvider.reset()
    }

    @After
    fun tearDown() {
        MultiProcessUuidLockProvider.reset()
    }

    @Test
    fun getLock() {
        val first = MultiProcessUuidLockProvider.getLock(context)
        val second = MultiProcessUuidLockProvider.getLock(context)

        assertThat(first)
            .isSameAs(second)
            .isSameAs(exclusiveMultiProcessFileLockMockedConstructionRule.constructionMock.constructed().first())

        assertThat(exclusiveMultiProcessFileLockMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)

        assertThat(exclusiveMultiProcessFileLockMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, FileConstants.UUID_FILE_NAME)
    }
}
