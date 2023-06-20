package io.appmetrica.analytics.location.impl

import android.Manifest
import android.content.Context
import android.location.LocationManager
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.location.impl.gpl.GplLastKnownLocationExtractorProvider
import io.appmetrica.analytics.location.impl.system.PermissionStrategyProvider
import io.appmetrica.analytics.location.impl.system.SystemLastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class LastKnownLocationExtractorProviderFactoryImplTest {

    private val passiveLastKnownLocationExtractorProvider = mock<LastKnownLocationExtractorProvider>()
    private val context = mock<Context>()
    private val permissionExtractor = mock<PermissionExtractor>()

    @get:Rule
    val gplLastKnownLocationExtractorProviderMockedConstructionRule =
        MockedConstructionRule(GplLastKnownLocationExtractorProvider::class.java)

    @get:Rule
    val systemLastKnownLocationExtractorProviderMockedConstructionRule =
        MockedConstructionRule(SystemLastKnownLocationExtractorProvider::class.java)

    private lateinit var gplLastKnownLocationExtractorProvider: GplLastKnownLocationExtractorProvider
    private lateinit var networkLastKnownLocationExtractorProvider: SystemLastKnownLocationExtractorProvider
    private lateinit var gpsLastKnownLocationExtractorProvider: SystemLastKnownLocationExtractorProvider

    private lateinit var lastKnownLocationExtractorProviderFactoryImpl: LastKnownLocationExtractorProviderFactoryImpl

    @Before
    fun setUp() {
        lastKnownLocationExtractorProviderFactoryImpl =
            LastKnownLocationExtractorProviderFactoryImpl(passiveLastKnownLocationExtractorProvider)

        assertThat(systemLastKnownLocationExtractorProviderMockedConstructionRule.constructionMock.constructed())
            .hasSize(2)
        networkLastKnownLocationExtractorProvider =
            systemLastKnownLocationExtractorProviderMockedConstructionRule.constructionMock.constructed().first()
        gpsLastKnownLocationExtractorProvider =
            systemLastKnownLocationExtractorProviderMockedConstructionRule.constructionMock.constructed()[1]

        gplLastKnownLocationExtractorProvider = gplLastKnownLocationExtractorProvider()
    }

    @Test
    fun passiveLastKnownLocationExtractorProvider() {
        val first = lastKnownLocationExtractorProviderFactoryImpl.passiveLastKnownLocationExtractorProvider
        val second = lastKnownLocationExtractorProviderFactoryImpl.passiveLastKnownLocationExtractorProvider

        assertThat(first).isEqualTo(passiveLastKnownLocationExtractorProvider)
        assertThat(second).isEqualTo(passiveLastKnownLocationExtractorProvider)
    }

    @Test
    fun `networkLastKnownLocationExtractorProvider provider`() {
        assertThat(systemLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.arguments[0][0])
            .isInstanceOf(String::class.java)
            .isEqualTo(LocationManager.NETWORK_PROVIDER)
    }

    @Test
    fun `networkLastKnownLocationExtractorProvider permissionStrategy for true`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
            .thenReturn(true)
        val strategy = networkLastKnownLocationExtractorProviderPermissionStrategy().getPermissionResolutionStrategy(
            permissionExtractor
        )
        assertThat(strategy.hasNecessaryPermissions(context)).isTrue()
    }

    @Test
    fun `networkLastKnownLocationExtractorProvider permissionStrategy for false`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
            .thenReturn(false)
        val strategy = networkLastKnownLocationExtractorProviderPermissionStrategy().getPermissionResolutionStrategy(
            permissionExtractor
        )
        assertThat(strategy.hasNecessaryPermissions(context)).isFalse()
    }

    private fun networkLastKnownLocationExtractorProviderPermissionStrategy(): PermissionStrategyProvider {
        val strategyProvider =
            systemLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.arguments[0][1]
        assertThat(strategyProvider)
            .isInstanceOf(PermissionStrategyProvider::class.java)
        return strategyProvider as PermissionStrategyProvider
    }

    @Test
    fun `networkLastKnownLocationExtractorProvider identifier`() {
        assertThat(systemLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.arguments[0][2])
            .isInstanceOf(String::class.java)
            .isEqualTo("location-module-network")
    }

    @Test
    fun gpsLastKnownLocationExtractorProvider() {
        val first = lastKnownLocationExtractorProviderFactoryImpl.gpsLastKnownLocationExtractorProvider
        val second = lastKnownLocationExtractorProviderFactoryImpl.gpsLastKnownLocationExtractorProvider

        assertThat(first).isEqualTo(gpsLastKnownLocationExtractorProvider)
        assertThat(second).isEqualTo(gpsLastKnownLocationExtractorProvider)
    }

    @Test
    fun `gpsLastKnownLocationExtractorProvider provider`() {
        assertThat(systemLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.arguments[1][0])
            .isInstanceOf(String::class.java)
            .isEqualTo(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun `gpsLastKnownLocationExtractorProvider permissionStrategy for true`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(true)
        val strategy = gpsLastKnownLocationExtractorProviderPermissionStrategy().getPermissionResolutionStrategy(
            permissionExtractor
        )
        assertThat(strategy.hasNecessaryPermissions(context)).isTrue()
    }

    @Test
    fun `gpsLastKnownLocationExtractorProvider permissionStrategy for false`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
            .thenReturn(false)
        val strategy = gpsLastKnownLocationExtractorProviderPermissionStrategy().getPermissionResolutionStrategy(
            permissionExtractor
        )
        assertThat(strategy.hasNecessaryPermissions(context)).isFalse()
    }

    private fun gpsLastKnownLocationExtractorProviderPermissionStrategy(): PermissionStrategyProvider {
        val strategyProvider =
            systemLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.arguments[1][1]
        assertThat(strategyProvider)
            .isInstanceOf(PermissionStrategyProvider::class.java)
        return strategyProvider as PermissionStrategyProvider
    }

    @Test
    fun `gpsLastKnownLocationExtractorProvider identifier`() {
        assertThat(systemLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.arguments[1][2])
            .isInstanceOf(String::class.java)
            .isEqualTo("location-module-gps")
    }

    @Test
    fun `create gplLastKnownLocationExtractorProvider`() {
        val first = lastKnownLocationExtractorProviderFactoryImpl.gplLastKnownLocationExtractorProvider
        val second = lastKnownLocationExtractorProviderFactoryImpl.gplLastKnownLocationExtractorProvider

        assertThat(first).isEqualTo(gplLastKnownLocationExtractorProvider)
        assertThat(second).isEqualTo(gplLastKnownLocationExtractorProvider)
    }

    @Test
    fun networkLastKnownLocationExtractorProvider() {
        val first = lastKnownLocationExtractorProviderFactoryImpl.networkLastKnownLocationExtractorProvider
        val second = lastKnownLocationExtractorProviderFactoryImpl.networkLastKnownLocationExtractorProvider

        assertThat(first).isEqualTo(networkLastKnownLocationExtractorProvider)
        assertThat(second).isEqualTo(networkLastKnownLocationExtractorProvider)
    }

    private fun gplLastKnownLocationExtractorProvider(): GplLastKnownLocationExtractorProvider {
        assertThat(gplLastKnownLocationExtractorProviderMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(gplLastKnownLocationExtractorProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly("location-module-gpl")
        return gplLastKnownLocationExtractorProviderMockedConstructionRule.constructionMock.constructed().first()
    }
}
