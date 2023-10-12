package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultServiceComponentInitializerTest : CommonTest() {

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
        serviceComponentsInitializer.onCreate(context)
        verify(GlobalServiceLocator.getInstance().moduleEntryPointsRegister)
            .register("io.appmetrica.analytics.remotepermissions.internal.RemotePermissionsModuleEntryPoint")
        verifyNoMoreInteractions(context)
    }

}
