package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CustomSdkHostsHolderTest : CommonTest() {

    private val customSdkHostsHolder = CustomSdkHostsHolder()
    private val filledHostsMap = mapOf("am" to listOf("host1", "host2"), "ads" to listOf("host3"))
    private val filledResult = IdentifiersResult(
        JsonHelper.customSdkHostsToString(filledHostsMap), IdentifierStatus.OK, null
    )

    @Test
    fun initialState() {
        assertThat(customSdkHostsHolder.commonResult).isNull()
        assertThat(customSdkHostsHolder.resultMap).isEmpty()
        val map = mutableMapOf<String, IdentifiersResult>()
        customSdkHostsHolder.putToMap(listOf("uuid", "device_id"), map)
        assertThat(map).isEqualTo(mapOf(
            Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(
                            emptyMap()
                        ), IdentifierStatus.UNKNOWN, null
                    )
        ))
    }

    @Test
    fun updateToNull() {
        val result = IdentifiersResult(
            null,
            IdentifierStatus.FEATURE_DISABLED,
            "feature disabled"
        )
        customSdkHostsHolder.update(result)

        assertThat(customSdkHostsHolder.commonResult).isEqualTo(result)
        assertThat(customSdkHostsHolder.resultMap).isEmpty()
        val map = mutableMapOf<String, IdentifiersResult>()
        customSdkHostsHolder.putToMap(listOf("uuid", "device_id"), map)
        assertThat(map).isEqualTo(mapOf(
            Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(
                            emptyMap()
                        ), IdentifierStatus.FEATURE_DISABLED, "feature disabled"
                    )
        ))
    }

    @Test
    fun updateToEmpty() {
        val result = IdentifiersResult(
            "",
            IdentifierStatus.NO_STARTUP,
            "no startup"
        )
        customSdkHostsHolder.update(result)

        assertThat(customSdkHostsHolder.commonResult).isEqualTo(result)
        assertThat(customSdkHostsHolder.resultMap).isEmpty()
        val map = mutableMapOf<String, IdentifiersResult>()
        customSdkHostsHolder.putToMap(listOf("uuid", "device_id"), map)
        assertThat(map).isEqualTo(mapOf(
            Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(
                            emptyMap()
                        ), IdentifierStatus.NO_STARTUP, "no startup"
                    )
        ))
    }

    @Test
    fun updateToFilled() {
        customSdkHostsHolder.update(filledResult)
        checkFilled()
    }

    @Test
    fun onlyRequestedIdentifiersAreFilled() {
        customSdkHostsHolder.update(filledResult)

        val map = mutableMapOf<String, IdentifiersResult>()
        customSdkHostsHolder.putToMap(listOf("am"), map)
        assertThat(map).isEqualTo(mapOf(
            Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(
                            mapOf("am" to listOf("host1", "host2"))
                        ), IdentifierStatus.OK, null
                    )
        ))
    }

    @Test
    fun updateFromFilledToNull() {
        customSdkHostsHolder.update(filledResult)
        customSdkHostsHolder.update(
            IdentifiersResult(
                null,
                IdentifierStatus.OK,
                null
            )
        )
        checkFilled()
    }

    @Test
    fun updateFromFilledToEmpty() {
        customSdkHostsHolder.update(filledResult)
        customSdkHostsHolder.update(
            IdentifiersResult(
                "",
                IdentifierStatus.OK,
                null
            )
        )
        checkFilled()
    }

    @Test
    fun updateFromFilledToFilled() {
        customSdkHostsHolder.update(filledResult)
        val otherCustomSdkHosts = mapOf("passport" to listOf("pas.host1"))
        val result = IdentifiersResult(
            JsonHelper.customSdkHostsToString(otherCustomSdkHosts), IdentifierStatus.OK, null
        )
        customSdkHostsHolder.update(result)
        assertThat(customSdkHostsHolder.commonResult).isEqualTo(result)
        assertThat(customSdkHostsHolder.resultMap).isEqualTo(mapOf(
            "passport" to IdentifiersResult(
                JsonHelper.listToJson(
                    listOf("pas.host1")
                )!!.toString(), IdentifierStatus.OK, null
            ),
        ))
        val map = mutableMapOf<String, IdentifiersResult>()
        customSdkHostsHolder.putToMap(listOf("am", "ads", "passport"), map)
        assertThat(map).isEqualTo(mapOf(
            Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(
                            otherCustomSdkHosts
                        ), IdentifierStatus.OK, null
                    )
        ))
    }

    private fun checkFilled() {
        assertThat(customSdkHostsHolder.commonResult).isEqualTo(filledResult)
        assertThat(customSdkHostsHolder.resultMap).isEqualTo(mapOf(
            "am" to IdentifiersResult(
                JsonHelper.listToJson(
                    listOf("host1", "host2")
                )!!.toString(), IdentifierStatus.OK, null
            ),
            "ads" to IdentifiersResult(
                JsonHelper.listToJson(
                    listOf("host3")
                )!!.toString(), IdentifierStatus.OK, null
            ),
        ))
        val map = mutableMapOf<String, IdentifiersResult>()
        customSdkHostsHolder.putToMap(listOf("am", "ads"), map)
        assertThat(map).isEqualTo(mapOf(
            Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(
                            filledHostsMap
                        ), IdentifierStatus.OK, null
                    )
        ))
    }
}
