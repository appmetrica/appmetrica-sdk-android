package io.appmetrica.analytics.impl.client.connection

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.internal.AppMetricaService
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DefaultServiceDescriptionProviderTest : CommonTest() {

    private val packageName = "test.package.name"
    private val context: Context = mock {
        on { packageName } doReturn packageName
    }

    private val provider by setUp { DefaultServiceDescriptionProvider() }

    @Test
    fun serviceDescription() {
        ObjectPropertyAssertions(provider.serviceDescription(context))
            .checkField("packageName", packageName)
            .checkField("serviceScheme", "appmetrica")
            .checkField("serviceClass", AppMetricaService::class.java)
            .checkAll()
    }
}
