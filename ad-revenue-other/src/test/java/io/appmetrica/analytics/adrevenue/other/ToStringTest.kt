package io.appmetrica.analytics.adrevenue.other

import io.appmetrica.analytics.adrevenue.other.impl.config.client.model.ClientSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.internal.ClientSideAdRevenueOtherConfigWrapper
import io.appmetrica.gradle.androidtestutils.tostring.BaseToStringTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
internal class ToStringTest(
    actualValue: Any?,
    modifierPreconditions: Int,
    excludedFields: Set<String>?,
    additionalDescription: String?
) : BaseToStringTest(
    actualValue,
    modifierPreconditions,
    excludedFields,
    additionalDescription
) {

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            ClientSideAdRevenueOtherConfigWrapper(mock<ClientSideAdRevenueOtherConfig>()).toTestCase(),
            ServiceSideAdRevenueOtherConfig(enabled = true, includeSource = true).toTestCase()
        )
    }
}
