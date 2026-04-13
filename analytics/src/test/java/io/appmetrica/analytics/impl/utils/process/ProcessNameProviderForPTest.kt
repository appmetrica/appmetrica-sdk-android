package io.appmetrica.analytics.impl.utils.process

import android.app.Application
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn

internal class ProcessNameProviderForPTest : CommonTest() {

    @get:Rule
    val applicationRule = staticRule<Application> {
        on { Application.getProcessName() } doReturn "some_name"
    }

    @Test
    fun getProcessName() {
        val processNameProvider = ProcessNameProviderForP()
        assertThat(processNameProvider.getProcessName()).isEqualTo("some_name")
    }
}
