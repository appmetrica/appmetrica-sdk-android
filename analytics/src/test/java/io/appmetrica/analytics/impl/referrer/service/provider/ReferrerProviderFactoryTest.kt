package io.appmetrica.analytics.impl.referrer.service.provider

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.referrer.service.cache.CachedReferrerProvider
import io.appmetrica.analytics.impl.referrer.service.cache.VitalReferrerCache
import io.appmetrica.analytics.impl.referrer.service.provider.google.GoogleReferrerProvider
import io.appmetrica.analytics.impl.referrer.service.provider.huawei.HuaweiReferrerProvider
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.reflect.KClass

@RunWith(Parameterized::class)
internal class ReferrerProviderFactoryTest(
    private val installerPackageName: String?,
    expectedProviderClass: KClass<out ReferrerProvider>,
) : CommonTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: installer={0}, provider={1}")
        fun data(): Collection<Array<Any?>> = listOf(
            testCase("com.android.vending", GoogleReferrerProvider::class),
            testCase("com.huawei.appmarket", HuaweiReferrerProvider::class),
            testCase("com.unknown.store", NotSupportedPackageInstallerReferrerProvider::class),
            testCase(null, NotSupportedPackageInstallerReferrerProvider::class),
        )

        private fun testCase(installer: String?, providerClass: KClass<out ReferrerProvider>): Array<Any?> {
            return arrayOf(installer, providerClass)
        }
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val context: Context = mock()
    private val packageManager: SafePackageManager = mock()

    @get:Rule
    val providerConstructorRule = constructionRule(expectedProviderClass)

    @get:Rule
    val vitalReferrerCacheRule = constructionRule<VitalReferrerCache>()

    @get:Rule
    val cachedReferrerProviderRule = constructionRule<CachedReferrerProvider>()

    @get:Rule
    val safeReferrerProviderRule = constructionRule<SafeReferrerProvider>()

    @get:Rule
    val executorReferrerProviderRule = constructionRule<ExecutorReferrerProvider>()

    private val factory by setUp { ReferrerProviderFactory(packageManager) }

    @Test
    fun `create returns correct decorator chain based on installer package name`() {
        whenever(context.packageName).thenReturn("com.test.app")
        whenever(packageManager.getInstallerPackageName(context, "com.test.app"))
            .thenReturn(installerPackageName)

        val result = factory.create(context)

        // Base provider
        val baseProviders = providerConstructorRule.constructionMock.constructed()
        assertThat(baseProviders).hasSize(1)
        assertThat(result).isInstanceOf(ExecutorReferrerProvider::class.java)

        // VitalReferrerCache is created with vitalCommonDataProvider from GlobalServiceLocator
        val vitalCaches = vitalReferrerCacheRule.constructionMock.constructed()
        assertThat(vitalCaches).hasSize(1)
        assertThat(vitalReferrerCacheRule.argumentInterceptor.flatArguments()).containsExactly(
            GlobalServiceLocator.getInstance().vitalDataProviderStorage.commonDataProvider
        )

        // CachedReferrerProvider is created once: for vital caches
        val cachedProviders = cachedReferrerProviderRule.constructionMock.constructed()
        assertThat(cachedProviders).hasSize(1)
        assertThat(cachedReferrerProviderRule.argumentInterceptor.flatArguments()).containsExactly(
            baseProviders.last(), vitalCaches.last(), // vital
        )

        // SafeReferrerProvider wraps the second CachedReferrerProvider
        val safeProviders = safeReferrerProviderRule.constructionMock.constructed()
        assertThat(safeProviders).hasSize(1)
        assertThat(safeReferrerProviderRule.argumentInterceptor.flatArguments()).containsExactly(cachedProviders.last())

        // ExecutorReferrerProvider wraps SafeReferrerProvider with executor from GlobalServiceLocator
        val executorProviders = executorReferrerProviderRule.constructionMock.constructed()
        assertThat(executorProviders).hasSize(1)
        assertThat(executorReferrerProviderRule.argumentInterceptor.flatArguments()).containsExactly(
            safeProviders.last(), globalServiceLocatorRule.supportIOExecutor,
        )
    }
}
