package io.appmetrica.analytics.impl.utils.process

import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class CurrentProcessDetectorTest : CommonTest() {

    @get:Rule
    val processNameProviderForPRule = constructionRule<ProcessNameProviderForP> {
        on { getProcessName() } doReturn "processNameProviderForPRule"
    }

    @get:Rule
    val processNameProviderBeforePRule = constructionRule<ProcessNameProviderBeforeP> {
        on { getProcessName() } doReturn "processNameProviderBeforePRule"
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getProcessNameForP() {
        val detector = CurrentProcessDetector()
        assertThat(detector.getProcessName()).isEqualTo("processNameProviderForPRule")
        assertThat(processNameProviderBeforePRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun getProcessNameBeforeP() {
        val detector = CurrentProcessDetector()
        assertThat(detector.getProcessName()).isEqualTo("processNameProviderBeforePRule")
        assertThat(processNameProviderForPRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun isMainProcess() {
        val processNameProvider: ProcessNameProvider = mock {
            on { getProcessName() } doReturn "processNameProvider"
        }
        val detector = CurrentProcessDetector(processNameProvider)

        assertThat(detector.isMainProcess()).isTrue()
    }

    @Test
    fun isMainProcessIfNot() {
        val processNameProvider: ProcessNameProvider = mock {
            on { getProcessName() } doReturn ":processNameProvider"
        }
        val detector = CurrentProcessDetector(processNameProvider)

        assertThat(detector.isMainProcess()).isFalse()
    }

    @Test
    fun isMainProcessIfEmpty() {
        val processNameProvider: ProcessNameProvider = mock {
            on { getProcessName() } doReturn ""
        }
        val detector = CurrentProcessDetector(processNameProvider)

        assertThat(detector.isMainProcess()).isFalse()
    }

    @Test
    fun isNonMainProcess() {
        val processNameProvider: ProcessNameProvider = mock {
            on { getProcessName() } doReturn ":processNameProvider"
        }
        val detector = CurrentProcessDetector(processNameProvider)

        assertThat(detector.isNonMainProcess("processNameProvider")).isTrue()
    }

    @Test
    fun isNonMainProcessIfNot() {
        val processNameProvider: ProcessNameProvider = mock {
            on { getProcessName() } doReturn ":processNameProvider"
        }
        val detector = CurrentProcessDetector(processNameProvider)

        assertThat(detector.isNonMainProcess("fakeProcessNameProvider")).isFalse()
    }

    @Test
    fun isNonMainProcessIfEmpty() {
        val processNameProvider: ProcessNameProvider = mock {
            on { getProcessName() } doReturn ""
        }
        val detector = CurrentProcessDetector(processNameProvider)

        assertThat(detector.isNonMainProcess("")).isFalse()
    }
}
