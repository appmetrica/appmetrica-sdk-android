package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupRequiredUtilsPickIdentifiersThatShouldTriggerStartupTest(
    private val input: List<String>,
    private val expected: List<String>,
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            // #0
            arrayOf(emptyList<String>(), emptyList<String>()),
            arrayOf(
                listOf(Constants.StartupParamsCallbackKeys.UUID),
                listOf(Constants.StartupParamsCallbackKeys.UUID)
            ),
            arrayOf(
                listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID),
                listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID)
            ),
            arrayOf(
                listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH),
                listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH)
            ),
            arrayOf(
                listOf(Constants.StartupParamsCallbackKeys.GET_AD_URL),
                listOf(Constants.StartupParamsCallbackKeys.GET_AD_URL)
            ),
            // #5
            arrayOf(
                listOf(Constants.StartupParamsCallbackKeys.REPORT_AD_URL),
                listOf(Constants.StartupParamsCallbackKeys.REPORT_AD_URL)
            ),
            arrayOf(
                listOf(Constants.StartupParamsCallbackKeys.CLIDS),
                listOf(Constants.StartupParamsCallbackKeys.CLIDS)
            ),
            arrayOf(
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
            ),
            arrayOf(listOf("custom"), emptyList<String>()),
            arrayOf(
                listOf(
                    "custom",
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
            ),
            // #10
            arrayOf(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED), emptyList<String>()),
            arrayOf(
                listOf(
                    Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED,
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
            ),
            arrayOf(
                listOf("custom", Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED),
                emptyList<String>()
            ),
            arrayOf(
                listOf(
                    "custom",
                    Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED,
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS,
                ),
            ),
        )
    }

    @Test
    fun pickIdentifiersThatShouldTriggerStartup() {
        assertThat(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(input))
            .containsExactlyInAnyOrderElementsOf(expected)
    }
}
