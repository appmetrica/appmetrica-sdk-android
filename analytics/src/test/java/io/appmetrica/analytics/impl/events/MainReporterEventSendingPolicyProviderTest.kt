package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MainReporterEventSendingPolicyProviderTest : CommonTest() {

    private val triggerProvider: EventTriggerProvider = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    @get:Rule
    val nativeMainReporterEventSendingPolicyRule = constructionRule<NativeMainReporterEventSendingPolicy>()

    @get:Rule
    val pluginMainReporterEventSendingPolicyRule = constructionRule<PluginMainReporterEventSendingPolicy>()

    @get:Rule
    val frameworkDetectorRule = staticRule<FrameworkDetector>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val extraMetaInfoRetriever: ExtraMetaInfoRetriever by setUp {
        GlobalServiceLocator.getInstance().extraMetaInfoRetriever
    }

    private val provider: MainReporterEventSendingPolicyProvider by setUp { MainReporterEventSendingPolicyProvider() }

    @Test
    fun `getPolicy for native and null plugin id`() {
        whenever(FrameworkDetector.isNative()).thenReturn(true)
        whenever(extraMetaInfoRetriever.pluginId).thenReturn(null)
        assertThat(provider.getPolicy(triggerProvider, configurationHolder, initialConfig, preferences))
            .isEqualTo(nativePolicy())
    }

    @Test
    fun `getPolicy for native and empty plugin id`() {
        whenever(FrameworkDetector.isNative()).thenReturn(true)
        whenever(extraMetaInfoRetriever.pluginId).thenReturn("")
        assertThat(provider.getPolicy(triggerProvider, configurationHolder, initialConfig, preferences))
            .isEqualTo(nativePolicy())
    }

    @Test
    fun `getPolicy for native and non empty plugin id`() {
        whenever(FrameworkDetector.isNative()).thenReturn(true)
        whenever(extraMetaInfoRetriever.pluginId).thenReturn("Plugin id")
        assertThat(provider.getPolicy(triggerProvider, configurationHolder, initialConfig, preferences))
            .isEqualTo(pluginPolicy())
    }

    @Test
    fun `getPolicy for non native and null plugin id`() {
        whenever(FrameworkDetector.isNative()).thenReturn(false)
        whenever(extraMetaInfoRetriever.pluginId).thenReturn(null)
        assertThat(provider.getPolicy(triggerProvider, configurationHolder, initialConfig, preferences))
            .isEqualTo(pluginPolicy())
    }

    @Test
    fun `getPolicy for non native and empty plugin id`() {
        whenever(FrameworkDetector.isNative()).thenReturn(false)
        whenever(extraMetaInfoRetriever.pluginId).thenReturn("")
        assertThat(provider.getPolicy(triggerProvider, configurationHolder, initialConfig, preferences))
            .isEqualTo(pluginPolicy())
    }

    @Test
    fun `getPolicy for non native and non empty plugin id`() {
        whenever(FrameworkDetector.isNative()).thenReturn(false)
        whenever(extraMetaInfoRetriever.pluginId).thenReturn("Plugin id")
        assertThat(provider.getPolicy(triggerProvider, configurationHolder, initialConfig, preferences))
            .isEqualTo(pluginPolicy())
    }

    private fun nativePolicy(): NativeMainReporterEventSendingPolicy {
        assertThat(nativeMainReporterEventSendingPolicyRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeMainReporterEventSendingPolicyRule.argumentInterceptor.flatArguments()).isEmpty()
        assertThat(pluginMainReporterEventSendingPolicyRule.constructionMock.constructed()).isEmpty()
        return nativeMainReporterEventSendingPolicyRule.constructionMock.constructed().first()
    }

    private fun pluginPolicy(): PluginMainReporterEventSendingPolicy {
        assertThat(pluginMainReporterEventSendingPolicyRule.constructionMock.constructed()).hasSize(1)
        assertThat(pluginMainReporterEventSendingPolicyRule.argumentInterceptor.flatArguments())
            .containsExactly(triggerProvider, configurationHolder, initialConfig, preferences)
        assertThat(nativeMainReporterEventSendingPolicyRule.constructionMock.constructed()).isEmpty()
        return pluginMainReporterEventSendingPolicyRule.constructionMock.constructed().first()
    }
}
