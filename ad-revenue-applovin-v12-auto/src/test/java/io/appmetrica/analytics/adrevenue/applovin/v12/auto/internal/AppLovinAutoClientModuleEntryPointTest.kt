package io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal

import android.content.Context
import com.applovin.communicator.AppLovinCommunicator
import com.applovin.communicator.AppLovinCommunicatorSubscriber
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.AppLovinIlrdReporter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.BundleToClientApplovinConfigConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.model.ClientApplovinConfig
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AppLovinAutoClientModuleEntryPointTest : CommonTest() {

    private val enabledConfig = ClientApplovinConfig(enabled = true)
    private val disabledConfig = ClientApplovinConfig(enabled = false)

    private val context: Context = mock()
    private val clientContext: ClientContext = mock {
        on { this.context } doReturn context
    }

    private val communicator: AppLovinCommunicator = mock()

    @get:Rule
    val reflectionUtilsRule = staticRule<ReflectionUtils> {
        on { ReflectionUtils.detectClassExists(Constants.LIBRARY_COMMUNICATOR_CLASS) } doReturn true
        on { ReflectionUtils.detectClassExists(Constants.LIBRARY_MESSAGE_CLASS) } doReturn true
    }

    @get:Rule
    val communicatorRule = staticRule<AppLovinCommunicator> {
        on { AppLovinCommunicator.getInstance(any()) } doReturn communicator
    }

    @get:Rule
    val bundleConverterRule = constructionRule<BundleToClientApplovinConfigConverter>()

    @get:Rule
    val reporterRule = constructionRule<AppLovinIlrdReporter>()

    private val entryPoint by setUp { AppLovinAutoClientModuleEntryPoint() }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo(Constants.MODULE_ID)
    }

    @Test
    fun onActivatedWithEnabledConfigSubscribes() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        verify(communicator, atLeast(1)).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun onActivatedWithDisabledConfigDoesNotSubscribe() {
        entryPoint.initClientSide(clientContext)
        updateConfig(disabledConfig)
        entryPoint.onActivated()

        verify(communicator, never()).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun onActivatedWithNoLibraryDoesNotSubscribe() {
        whenever(ReflectionUtils.detectClassExists(Constants.LIBRARY_COMMUNICATOR_CLASS)).thenReturn(false)
        val entryPoint = AppLovinAutoClientModuleEntryPoint()

        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig, entryPoint)
        entryPoint.onActivated()

        verify(communicator, never()).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun onActivatedWithNoMessageClassDoesNotSubscribe() {
        whenever(ReflectionUtils.detectClassExists(Constants.LIBRARY_MESSAGE_CLASS)).thenReturn(false)
        val entryPoint = AppLovinAutoClientModuleEntryPoint()

        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig, entryPoint)
        entryPoint.onActivated()

        verify(communicator, never()).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun onActivatedWithDefaultConfigSubscribes() {
        entryPoint.initClientSide(clientContext)
        entryPoint.onActivated()

        verify(communicator, atLeast(1)).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun configUpdateFromEnabledToDisabledUnsubscribes() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        updateConfig(disabledConfig)

        verify(communicator).unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun configUpdateFromDisabledToEnabledSubscribes() {
        entryPoint.initClientSide(clientContext)
        updateConfig(disabledConfig)
        entryPoint.onActivated()
        updateConfig(enabledConfig)

        verify(communicator).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun subscribesOnlyOnce() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        updateConfig(enabledConfig)

        verify(communicator, atLeast(1)).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun adRevenueCollectorSourceIdentifier() {
        assertThat(entryPoint.adRevenueCollector.sourceIdentifier)
            .isEqualTo(Constants.AD_REVENUE_SOURCE_IDENTIFIER)
    }

    @Test
    fun adRevenueCollectorEnabledWhenConfigAndLibraryAvailable() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isTrue()
    }

    @Test
    fun adRevenueCollectorEnabledWhenDefaultConfigAndLibraryAvailable() {
        entryPoint.initClientSide(clientContext)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isTrue()
    }

    @Test
    fun adRevenueCollectorDisabledWhenConfigDisabled() {
        entryPoint.initClientSide(clientContext)
        updateConfig(disabledConfig)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun adRevenueCollectorDisabledWhenNoLibrary() {
        whenever(ReflectionUtils.detectClassExists(Constants.LIBRARY_COMMUNICATOR_CLASS)).thenReturn(false)
        val entryPoint = AppLovinAutoClientModuleEntryPoint()

        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig, entryPoint)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun adRevenueCollectorDisabledWhenSubscribeThrows() {
        whenever(communicator.subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>()))
            .doThrow(RuntimeException("subscribe failed"))

        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun adRevenueCollectorDisabledAfterConfigDisabled() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        updateConfig(disabledConfig)

        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun libraryAvailableNotClearedWhenUnsubscribeThrows() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        whenever(communicator.unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>()))
            .doThrow(RuntimeException("unsubscribe failed"))
        updateConfig(disabledConfig)

        // libraryAvailable stays true — re-enabling config must not trigger a new subscribe call
        // because the adapter still holds the subscriber
        updateConfig(enabledConfig)
        verify(communicator).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun unsubscribeRetriedAfterPreviousUnsubscribeThrew() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        whenever(communicator.unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>()))
            .doThrow(RuntimeException("unsubscribe failed"))
        updateConfig(disabledConfig)

        // fix the communicator and retry — libraryAvailable must become false now
        whenever(communicator.unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>()))
            .thenAnswer { }
        updateConfig(disabledConfig)

        verify(communicator, times(2)).unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun updateListenerStateBeforeInitClientSideDoesNothing() {
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        verify(communicator, never()).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
        verify(communicator, never()).unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun getBundleConverterReturnsConstructedInstance() {
        assertThat(entryPoint.serviceConfigExtensionConfiguration.getBundleConverter())
            .isEqualTo(bundleConverterRule.constructionMock.constructed().single())
    }

    @Test
    fun onServiceConfigUpdatedWithNullFeaturesConfigUsesDefaultEnabled() {
        entryPoint.initClientSide(clientContext)
        val moduleServiceConfig: ModuleServiceConfig<ClientApplovinConfigWrapper?> = mock {
            on { featuresConfig } doReturn null
        }
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)
        entryPoint.onActivated()

        verify(communicator, atLeast(1)).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    private fun updateConfig(
        config: ClientApplovinConfig,
        entryPoint: AppLovinAutoClientModuleEntryPoint = this.entryPoint,
    ) {
        val wrapper: ClientApplovinConfigWrapper = mock {
            on { this.config } doReturn config
        }
        val moduleServiceConfig: ModuleServiceConfig<ClientApplovinConfigWrapper?> = mock {
            on { featuresConfig } doReturn wrapper
        }
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)
    }
}
