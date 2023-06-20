package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

internal class DatabaseStorageFactoryGetInstanceTest : CommonTest() {

    private val context = mock<Context>()
    private val file = mock<File>()

    @get:Rule
    val databaseStorageFactoryMockedConstructionRule = MockedConstructionRule(DatabaseStorageFactory::class.java)

    @Before
    fun setUp() {
        whenever(context.applicationContext).thenReturn(context)
    }

    @After
    fun destroy() {
        DatabaseStorageFactory.destroy()
    }

    @Test
    fun getInstance() {
        val first = DatabaseStorageFactory.getInstance(context)
        val second = DatabaseStorageFactory.getInstance(context)

        assertThat(databaseStorageFactoryMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStorageFactoryMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, null)
        assertThat(databaseStorageFactoryMockedConstructionRule.constructionMock.constructed().first())
            .isSameAs(first)
            .isSameAs(second)
    }

    @Test
    fun initWithOverwrittenDbStorage() {
        DatabaseStorageFactory.initWithOverwrittenDbStorage(context, file)
        DatabaseStorageFactory.initWithOverwrittenDbStorage(context, file)

        val first = DatabaseStorageFactory.getInstance(context)
        val second = DatabaseStorageFactory.getInstance(context)

        assertThat(databaseStorageFactoryMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStorageFactoryMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, file)
        assertThat(databaseStorageFactoryMockedConstructionRule.constructionMock.constructed().first())
            .isSameAs(first)
            .isSameAs(second)
    }
}
