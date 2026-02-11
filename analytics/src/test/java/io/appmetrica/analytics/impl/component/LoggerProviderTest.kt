package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.TestsData
import io.appmetrica.analytics.impl.component.ComponentUnitFieldsFactory.LoggerProvider
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class LoggerProviderTest : CommonTest() {
    private val apiKey: String = TestsData.generateApiKey()
    private val loggerProvider = LoggerProvider(apiKey)

    @Test
    fun getPublicLogger() {
        assertThat(loggerProvider.publicLogger)
            .isNotNull()
            .isExactlyInstanceOf(PublicLogger::class.java)
    }
}
