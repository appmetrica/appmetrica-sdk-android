package io.appmetrica.analytics.impl.client

import android.os.Bundle
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ClientConfigurationTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule
    private val counterConfiguration: CounterConfiguration = mock()
    private val processConfiguration: ProcessConfiguration = mock {
        on { sdkApiLevel } doReturn BuildConfig.API_LEVEL
        on { packageName } doReturn ContextRule.PACKAGE_NAME
    }
    private val bundle: Bundle = mock()

    @get:Rule
    val clientConfigurationMockedStaticRule = staticRule<CounterConfiguration> {
        on { CounterConfiguration.fromBundle(bundle) } doReturn counterConfiguration
    }

    @get:Rule
    val processConfigurationMockedStaticRule = staticRule<ProcessConfiguration> {
        on { ProcessConfiguration.fromBundle(bundle) } doReturn processConfiguration
    }

    @Test
    fun invalidNullProcessConfiguration() {
        whenever(ProcessConfiguration.fromBundle(bundle)).thenReturn(null)
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNull()
    }

    @Test
    fun invalidDifferentPackageName() {
        whenever(processConfiguration.packageName).thenReturn("another package name")
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNull()
    }

    @Test
    fun invalidDifferentSdkApiLevel() {
        whenever(processConfiguration.sdkApiLevel).thenReturn(BuildConfig.API_LEVEL - 1)
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNull()
    }

    @Test
    fun valid() {
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNotNull()
    }
}
