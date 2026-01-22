package io.appmetrica.analytics.impl.utils.process

import android.app.Application
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
