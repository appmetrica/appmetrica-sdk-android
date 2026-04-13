package io.appmetrica.analytics.impl.component.session

import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class SessionRequestParamsTest : CommonTest() {

    private val appVersion = "1.0.0"
    private val appBuild = "100"
    private val osVersion = "13.0"
    private val apiLevel = 33
    private val attributionId = 42
    private val analyticsSdkVersionName = "5.0.0"
    private val analyticsSdkBuildNumber = "500"

    private fun buildJson(): JSONObject = JSONObject()
        .put(Constants.RequestParametersJsonKeys.APP_VERSION, appVersion)
        .put(Constants.RequestParametersJsonKeys.APP_BUILD, appBuild)
        .put(Constants.RequestParametersJsonKeys.OS_VERSION, osVersion)
        .put(Constants.RequestParametersJsonKeys.OS_API_LEVEL, apiLevel)
        .put(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, attributionId)
        .put(Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME, analyticsSdkVersionName)
        .put(Constants.RequestParametersJsonKeys.ANALYTICS_SDK_BUILD_NUMBER, analyticsSdkBuildNumber)

    private val buildConfig: ReportRequestConfig = mock {
        on { appVersion } doReturn appVersion
        on { appBuildNumber } doReturn appBuild
        on { osVersion } doReturn osVersion
        on { osApiLevel } doReturn apiLevel
        on { attributionId } doReturn attributionId
        on { analyticsSdkVersionName } doReturn analyticsSdkVersionName
        on { analyticsSdkBuildNumber } doReturn analyticsSdkBuildNumber
    }

    @Test
    fun `areParamsSameAsInConfig returns true when all params match`() {
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isTrue
    }

    @Test
    fun `areParamsSameAsInConfig returns false when appVersion changed`() {
        whenever(buildConfig.appVersion).thenReturn("2.0.0")
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `areParamsSameAsInConfig returns false when appBuild changed`() {
        whenever(buildConfig.appBuildNumber).thenReturn("999")
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `areParamsSameAsInConfig returns false when osVersion changed`() {
        whenever(buildConfig.osVersion).thenReturn("14.0")
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `areParamsSameAsInConfig returns false when apiLevel changed`() {
        whenever(buildConfig.osApiLevel).thenReturn(34)
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `areParamsSameAsInConfig returns false when attributionId changed`() {
        whenever(buildConfig.attributionId).thenReturn(99)
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `areParamsSameAsInConfig returns false when analyticsSdkVersionName changed`() {
        whenever(buildConfig.analyticsSdkVersionName).thenReturn("6.0.0")
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `areParamsSameAsInConfig returns false when analyticsSdkBuildNumber changed`() {
        whenever(buildConfig.analyticsSdkBuildNumber).thenReturn("600")
        assertThat(SessionRequestParams(buildJson()).areParamsSameAsInConfig(buildConfig)).isFalse
    }

    @Test
    fun `toRequestParametersString returns original json string`() {
        val json = buildJson()
        assertThat(SessionRequestParams(json).toRequestParametersString()).isEqualTo(json.toString())
    }
}
