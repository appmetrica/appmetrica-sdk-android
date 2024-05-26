package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class DatabaseStoragePathProviderFactoryTest : CommonTest() {

    private val tag = "Some tag"
    private lateinit var databaseStoragePathProviderFactory: DatabaseStoragePathProviderFactory

    @get:Rule
    val androidUtilsMockedStaticRule = MockedStaticRule(AndroidUtils::class.java)

    @get:Rule
    val databaseStoragePathProviderMockedConstructionRule =
        MockedConstructionRule(DatabaseStoragePathProvider::class.java)

    @get:Rule
    val databaseFullPathProviderImplMockedConstructionRule =
        MockedConstructionRule(DatabaseFullPathProviderImpl::class.java)

    @get:Rule
    val databaseRelativePathFormerMockedConstructionRule =
        MockedConstructionRule(DatabaseRelativePathFormer::class.java)

    @get:Rule
    val oldDatabaseRelativePathFormerMockedConstructionRule =
        MockedConstructionRule(OldDatabaseRelativePathFormer::class.java)

    @get:Rule
    val outerRootDatabaseFullPathProviderMockedConstructionRule =
        MockedConstructionRule(OuterRootDatabaseFullPathProvider::class.java)

    private val outerDir = mock<File>()

    @Test
    fun `create without outer storage`() {
        databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(null)

        checkDatabaseStoragePathProvider(
            databaseFullPathProviderImplMockedConstructionRule.constructionMock.constructed().first(),
            listOf(databaseFullPathProviderImplMockedConstructionRule.constructionMock.constructed()[1])
        )

        assertThat(databaseFullPathProviderImplMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(databaseFullPathProviderImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                databaseRelativePathFormerMockedConstructionRule.constructionMock.constructed().first(),
                oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed().first()
            )

        assertThat(databaseRelativePathFormerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseRelativePathFormerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()

        assertThat(oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(oldDatabaseRelativePathFormerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `create with outer storage`() {
        databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(outerDir)

        checkDatabaseStoragePathProvider(
            outerRootDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed().first(),
            listOf(
                outerRootDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()[1],
                databaseFullPathProviderImplMockedConstructionRule.constructionMock.constructed().first()
            )
        )

        assertThat(outerRootDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(outerRootDatabaseFullPathProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                outerDir,
                databaseRelativePathFormerMockedConstructionRule.constructionMock.constructed().first(),
                outerDir,
                oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed().first()
            )

        assertThat(databaseFullPathProviderImplMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseFullPathProviderImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed()[1])

        assertThat(databaseRelativePathFormerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseRelativePathFormerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()

        assertThat(oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(oldDatabaseRelativePathFormerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()

    }

    private fun checkDatabaseStoragePathProvider(
        expectedTargetDirProvider: DatabaseFullPathProvider,
        expectedPossibleDatabaseDirProviders: List<DatabaseFullPathProvider>
    ) {
        val doNotDeleteSourceFile = true
        assertThat(databaseStoragePathProviderFactory.create(tag, doNotDeleteSourceFile))
            .isSameAs(databaseStoragePathProviderMockedConstructionRule.constructionMock.constructed().first())
        assertThat(databaseStoragePathProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStoragePathProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                expectedTargetDirProvider,
                expectedPossibleDatabaseDirProviders,
                doNotDeleteSourceFile,
                tag
            )
    }
}
