package io.appmetrica.analytics.adrevenue.applovin.v12.auto

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.model.ClientApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ClientApplovinConfigWrapper
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ServiceApplovinConfigWrapper
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
            ClientApplovinConfig(enabled = true).toTestCase(),
            ClientApplovinConfigWrapper(mock<ClientApplovinConfig>()).toTestCase(),
            ServiceApplovinConfig(enabled = true).toTestCase(),
            ServiceApplovinConfigWrapper(mock<ServiceApplovinConfig>()).toTestCase(),
        )
    }
}
