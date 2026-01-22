package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class CustomSdkHostsHolderTest : CommonTest() {

    @get:Rule
    val startupParamsItemAdapterMockedConstructionRule = MockedConstructionRule(StartupParamItemAdapter::class.java)
    private lateinit var startupParamItemAdapter: StartupParamItemAdapter

    private lateinit var customSdkHostsHolder: CustomSdkHostsHolder

    private val filledHostsMap = mapOf("am" to listOf("host1", "host2"), "ads" to listOf("host3"))
    private val filledResult =
        IdentifiersResult(
            JsonHelper.customSdkHostsToString(filledHostsMap), IdentifierStatus.OK, null
        )

    @Before
    fun setUp() {
        customSdkHostsHolder = CustomSdkHostsHolder()

        assertThat(startupParamsItemAdapterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupParamsItemAdapterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        startupParamItemAdapter = startupParamsItemAdapterMockedConstructionRule.constructionMock.constructed().first()
    }

    @Test
    fun initialState() {
        assertThat(customSdkHostsHolder.commonResult).isNull()
        assertThat(customSdkHostsHolder.resultMap).isEmpty()
        val map = mutableMapOf<String, StartupParamsItem>()
        val startupParamsItem = mock<StartupParamsItem>()
        whenever(
            startupParamItemAdapter.adapt(
                eq(
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(emptyMap()),
                        IdentifierStatus.UNKNOWN,
                        null
                    )
                )
            )
        ).thenReturn(startupParamsItem)
        customSdkHostsHolder.putToMap(listOf("uuid", "device_id"), map)
        assertThat(map).isEqualTo(mapOf(Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to startupParamsItem))
    }

    @Test
    fun updateToNull() {
        val result = IdentifiersResult(
            null,
            IdentifierStatus.FEATURE_DISABLED,
            "feature disabled"
        )
        customSdkHostsHolder.update(result)
        val startupParamsItem = mock<StartupParamsItem>()
        whenever(
            startupParamItemAdapter.adapt(
                eq(
                    IdentifiersResult(
                        JsonHelper.customSdkHostsToString(emptyMap()),
                        IdentifierStatus.FEATURE_DISABLED,
                        "feature disabled"
                    )
                )
            )
        ).thenReturn(startupParamsItem)

        assertThat(customSdkHostsHolder.commonResult).isEqualTo(result)
        assertThat(customSdkHostsHolder.resultMap).isEmpty()
        val map = mutableMapOf<String, StartupParamsItem>()
        customSdkHostsHolder.putToMap(listOf("uuid", "device_id"), map)
        assertThat(map).isEqualTo(mapOf(Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to startupParamsItem))
    }

    @Test
    fun updateToEmpty() {
        val result = IdentifiersResult(
            "",
            IdentifierStatus.UNKNOWN,
            "UNKNOWN"
        )
        customSdkHostsHolder.update(result)

        assertThat(customSdkHostsHolder.commonResult).isEqualTo(result)
        assertThat(customSdkHostsHolder.resultMap).isEmpty()
        val map = mutableMapOf<String, StartupParamsItem>()
        val startupParamsItem = mock<StartupParamsItem>()
        whenever(
            startupParamItemAdapter.adapt(
                IdentifiersResult(
                    JsonHelper.customSdkHostsToString(emptyMap()),
                    IdentifierStatus.UNKNOWN,
                    "UNKNOWN"
                )
            )
        ).thenReturn(startupParamsItem)
        customSdkHostsHolder.putToMap(listOf("uuid", "device_id"), map)
        assertThat(map).isEqualTo(
            mapOf(
                Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to startupParamsItem
            )
        )
    }

    @Test
    fun updateToFilled() {
        customSdkHostsHolder.update(filledResult)
        checkFilled()
    }

    @Test
    fun onlyRequestedIdentifiersAreFilled() {
        customSdkHostsHolder.update(filledResult)

        val map = mutableMapOf<String, StartupParamsItem>()
        val startupParamsItem = mock<StartupParamsItem>()
        whenever(
            startupParamItemAdapter.adapt(
                IdentifiersResult(
                    JsonHelper.customSdkHostsToString(
                        mapOf("am" to listOf("host1", "host2"))
                    ),
                    IdentifierStatus.OK,
                    null
                )
            )
        ).thenReturn(startupParamsItem)
        customSdkHostsHolder.putToMap(listOf("am"), map)
        assertThat(map).isEqualTo(mapOf(Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to startupParamsItem))
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
        val hosts = listOf("pas.host1")
        val otherCustomSdkHosts = mapOf("passport" to hosts)
        val result = IdentifiersResult(
            JsonHelper.customSdkHostsToString(otherCustomSdkHosts), IdentifierStatus.OK, null
        )
        val startupParamsItem = mock<StartupParamsItem>()
        val initialIdentifierResult =
            IdentifiersResult(
                JsonHelper.listToJson(hosts).toString(),
                IdentifierStatus.OK,
                null
            )
        whenever(
            startupParamItemAdapter.adapt(
                IdentifiersResult(
                    JsonHelper.customSdkHostsToString(otherCustomSdkHosts),
                    IdentifierStatus.OK,
                    null
                )
            )
        ).thenReturn(startupParamsItem)
        customSdkHostsHolder.update(result)
        assertThat(customSdkHostsHolder.commonResult).isEqualTo(result)
        assertThat(customSdkHostsHolder.resultMap).isEqualTo(mapOf("passport" to initialIdentifierResult))
        val map = mutableMapOf<String, StartupParamsItem>()
        customSdkHostsHolder.putToMap(listOf("am", "ads", "passport"), map)
        assertThat(map).isEqualTo(mapOf(Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to startupParamsItem))
    }

    private fun checkFilled() {
        assertThat(customSdkHostsHolder.commonResult).isEqualTo(filledResult)
        assertThat(customSdkHostsHolder.resultMap).isEqualTo(
            mapOf(
                "am" to IdentifiersResult(
                    JsonHelper.listToJson(
                        listOf("host1", "host2")
                    )!!.toString(),
                    IdentifierStatus.OK,
                    null
                ),
                "ads" to IdentifiersResult(
                    JsonHelper.listToJson(
                        listOf("host3")
                    )!!.toString(),
                    IdentifierStatus.OK,
                    null
                ),
            )
        )
        val customSdkHostString = JsonHelper.customSdkHostsToString(filledHostsMap)
        val hostsStartupParamsItem = StartupParamsItem(
            customSdkHostString,
            StartupParamsItemStatus.OK,
            null
        )
        whenever(
            startupParamItemAdapter.adapt(
                eq(
                    IdentifiersResult(
                        customSdkHostString,
                        filledResult.status,
                        filledResult.errorExplanation
                    )
                )
            )
        ).thenReturn(hostsStartupParamsItem)
        val map = mutableMapOf<String, StartupParamsItem>()
        customSdkHostsHolder.putToMap(listOf("am", "ads"), map)
        assertThat(map).isEqualTo(
            mapOf(
                Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS to hostsStartupParamsItem
            )
        )
    }
}
