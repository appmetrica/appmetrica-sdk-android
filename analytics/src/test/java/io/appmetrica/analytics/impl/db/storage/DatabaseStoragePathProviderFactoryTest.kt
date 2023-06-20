package io.appmetrica.analytics.impl.db.storage

import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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
    val lollipopDatabaseFullPathProviderMockedConstructionRule =
        MockedConstructionRule(LollipopDatabaseFullPathProvider::class.java)

    @get:Rule
    val databaseRelativePathFormerMockedConstructionRule =
        MockedConstructionRule(DatabaseRelativePathFormer::class.java)

    @get:Rule
    val oldDatabaseRelativePathFormerMockedConstructionRule =
        MockedConstructionRule(OldDatabaseRelativePathFormer::class.java)

    @get:Rule
    val preLollipopRelativePathFormerMockedConstructionRule =
        MockedConstructionRule(PreLollipopRelativePathFormer::class.java)

    @get:Rule
    val preLollipopDatabaseFullPathProviderMockedConstructionRule =
        MockedConstructionRule(PreLollipopDatabaseFullPathProvider::class.java)

    @get:Rule
    val outerRootDatabaseFullPathProviderMockedConstructionRule =
        MockedConstructionRule(OuterRootDatabaseFullPathProvider::class.java)

    private val outerDir = mock<File>()

    @Test
    fun `create pre lollipop without outer storage`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)).thenReturn(false)
        databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(null)

        checkPreLollipop()
    }

    @Test
    fun `create pre lollipop with outer storage`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)).thenReturn(false)
        databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(outerDir)

        checkPreLollipop()
    }

    private fun checkPreLollipop() {
        checkDatabaseStoragePathProvider(
            preLollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed().first(),
            listOf(preLollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()[1])
        )

        assertThat(preLollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(preLollipopDatabaseFullPathProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                preLollipopRelativePathFormerMockedConstructionRule.constructionMock.constructed().first(),
                oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed().first()
            )

        assertThat(preLollipopRelativePathFormerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(preLollipopRelativePathFormerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()

        assertThat(oldDatabaseRelativePathFormerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(oldDatabaseRelativePathFormerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `create lollipop without outer storage`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)).thenReturn(true)
        databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(null)

        checkDatabaseStoragePathProvider(
            lollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed().first(),
            listOf(lollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()[1])
        )

        assertThat(lollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(lollipopDatabaseFullPathProviderMockedConstructionRule.argumentInterceptor.flatArguments())
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
    fun `create lollipop with outer storage`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)).thenReturn(true)
        databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(outerDir)

        checkDatabaseStoragePathProvider(
            outerRootDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed().first(),
            listOf(
                outerRootDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()[1],
                lollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed().first()
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

        assertThat(lollipopDatabaseFullPathProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(lollipopDatabaseFullPathProviderMockedConstructionRule.argumentInterceptor.flatArguments())
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
        assertThat(databaseStoragePathProviderFactory.create(tag))
            .isSameAs(databaseStoragePathProviderMockedConstructionRule.constructionMock.constructed().first())
        assertThat(databaseStoragePathProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStoragePathProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                expectedTargetDirProvider,
                expectedPossibleDatabaseDirProviders,
                tag
            )
    }
}
