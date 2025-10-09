package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ConstantModuleEntryPointProvider
import io.appmetrica.analytics.impl.modules.PreferencesBasedModuleEntryPoint
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultServiceComponentsInitializerTest : CommonTest() {

    private val context = mock<Context>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private lateinit var serviceComponentsInitializer: ServiceComponentsInitializer

    @Before
    fun setUp() {
        serviceComponentsInitializer = DefaultServiceComponentsInitializer()
    }

    @Test
    fun onCreate() {
        val expectedModules = listOf(
            "io.appmetrica.analytics.remotepermissions.internal.RemotePermissionsModuleEntryPoint",
            "io.appmetrica.analytics.apphud.internal.ApphudServiceModuleEntryPoint",
            "io.appmetrica.analytics.screenshot.internal.ScreenshotServiceModuleEntryPoint",
            "io.appmetrica.analytics.billing.internal.BillingServiceModuleEntryPoint",
        )
        serviceComponentsInitializer.onCreate(context)

        val moduleEntryPointsRegister = GlobalServiceLocator.getInstance().moduleEntryPointsRegister

        inOrder(moduleEntryPointsRegister) {
            verify(moduleEntryPointsRegister).register(
                *expectedModules.map { ConstantModuleEntryPointProvider(it) }.toTypedArray()
            )
            verify(moduleEntryPointsRegister).register(
                PreferencesBasedModuleEntryPoint(context, "io.appmetrica.analytics.modules.ads", "lsm")
            )
            verifyNoMoreInteractions()
        }
    }
}
