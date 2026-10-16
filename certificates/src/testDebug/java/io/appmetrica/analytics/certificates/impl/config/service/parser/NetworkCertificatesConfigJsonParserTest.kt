package io.appmetrica.analytics.certificates.impl.config.service.parser

import io.appmetrica.analytics.certificates.impl.Constants
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
internal class NetworkCertificatesConfigJsonParserTest : CommonTest() {

    private val parser by setUp { NetworkCertificatesConfigJsonParser() }

    @Test
    fun defaultsWhenFeatureAbsent() {
        val result = parser.parse(JSONObject())
        assertThat(result.enabled).isEqualTo(Constants.Defaults.FEATURE_ENABLED)
    }

    @Test
    fun parsesEnabledFalse() {
        val rawData = JSONObject().put(
            "features",
            JSONObject().put(
                "list",
                JSONObject().put(
                    Constants.RemoteConfig.FEATURE_NAME,
                    JSONObject().put("enabled", false)
                )
            )
        )
        val result = parser.parse(rawData)
        assertThat(result.enabled).isFalse()
    }

    @Test
    fun parsesEnabledTrue() {
        val rawData = JSONObject().put(
            "features",
            JSONObject().put(
                "list",
                JSONObject().put(
                    Constants.RemoteConfig.FEATURE_NAME,
                    JSONObject().put("enabled", true)
                )
            )
        )
        val result = parser.parse(rawData)
        assertThat(result.enabled).isTrue()
    }
}
