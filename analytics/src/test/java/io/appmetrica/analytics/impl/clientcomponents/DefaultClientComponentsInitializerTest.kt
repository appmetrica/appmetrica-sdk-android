package io.appmetrica.analytics.impl.clientcomponents

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.modules.ConstantModuleEntryPointProvider
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class DefaultClientComponentsInitializerTest : CommonTest() {

    val expectedModules = listOf(
        "io.appmetrica.analytics.adrevenue.admob.v23.internal.AdMobClientModuleEntryPoint",
        "io.appmetrica.analytics.adrevenue.applovin.v12.internal.AppLovinClientModuleEntryPoint",
        "io.appmetrica.analytics.adrevenue.fyber.v3.internal.FyberClientModuleEntryPoint",
        "io.appmetrica.analytics.adrevenue.ironsource.v7.internal.IronSourceClientModuleEntryPoint",
    )

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val initializer = DefaultClientComponentsInitializer()

    @Test
    fun onCreate() {
        whenever(ClientServiceLocator.getInstance().mainProcessDetector.isMainProcess).thenReturn(true)
        initializer.onCreate()
        verify(ClientServiceLocator.getInstance().moduleEntryPointsRegister)
            .register(*expectedModules.map { ConstantModuleEntryPointProvider(it) }.toTypedArray())
    }

    @Test
    fun onCreateIfNotMainProcess() {
        whenever(ClientServiceLocator.getInstance().mainProcessDetector.isMainProcess).thenReturn(false)
        initializer.onCreate()
        verifyNoInteractions( ClientServiceLocator.getInstance().moduleEntryPointsRegister)
    }
}
