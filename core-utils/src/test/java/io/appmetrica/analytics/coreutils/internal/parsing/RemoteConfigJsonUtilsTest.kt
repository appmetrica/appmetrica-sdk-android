package io.appmetrica.analytics.coreutils.internal.parsing

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import java.util.concurrent.TimeUnit

private const val FEATURES = "features"
private const val LIST = "list"
private const val ENABLED = "enabled"
private const val QUERIES = "queries"
private const val URL = "url"
private const val QUERY_HOSTS = "query_hosts"
private const val URLS = "urls"

class RemoteConfigJsonUtilsTest {

    private val hostType = "Some host type"
    private val valueKey = "Some long key"

    @Test
    fun extractFeaturesIfFeaturesIsEmptyWithTrueFallback() {
        assertThat(RemoteConfigJsonUtils.extractFeature(JSONObject(), "feature", true))
    }

    @Test
    fun extractFeaturesIfFeaturesIsEmptyWithFalseFallback() {
        assertThat(RemoteConfigJsonUtils.extractFeature(JSONObject(), "feature", false)).isFalse()
    }

    @Test
    fun extractFeaturesIfListIsEmptyWithTrueFallback() {
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeatures(), "feature", true)).isTrue()
    }

    @Test
    fun extractFeaturesIfListIsEmptyWithFalseFallback() {
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeatures(), "feature", false)).isFalse()
    }

    @Test
    fun extractFeaturesIfNoFeatureWithTrueFallback() {
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeatures(), "feature", true)).isTrue()
    }

    @Test
    fun extractFeaturesIfNoFeatureWithFalseFallback() {
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeatureList(), "feature", false)).isFalse()
    }

    @Test
    fun extractFeaturesIfNoFeatureValueWithTrueFallback() {
        val featureName = "some feature"
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeature(featureName), featureName, true)).isTrue()
    }

    @Test
    fun extractFeaturesIfNoFeatureValueWithFalseFallback() {
        val featureName = "some feature"
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeature(featureName), featureName, false)).isFalse()
    }

    @Test
    fun extractFeatureIfTrueWithFalseFallback() {
        val featureName = "some feature"
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeatureValue(featureName, true), featureName, false)).isTrue()
    }

    @Test
    fun extractFeatureIfFalseWithTrueFallback() {
        val featureName = "some feature"
        assertThat(RemoteConfigJsonUtils.extractFeature(jsonWithFeatureValue(featureName, false), featureName, true)).isFalse()
    }

    @Test
    fun extractQueryIfNoQueries() {
        assertThat(RemoteConfigJsonUtils.extractQuery(JSONObject(), "query")).isNull()
    }

    @Test
    fun extractQueryWithEmptyQueries() {
        val json = JSONObject().put(QUERIES, JSONObject())
        assertThat(RemoteConfigJsonUtils.extractQuery(json, "query")).isNull()
    }

    @Test
    fun extractQueryWithEmptyList() {
        val json = JSONObject()
            .put(
                QUERIES,
                JSONObject().put(LIST, JSONObject())
            )
        assertThat(RemoteConfigJsonUtils.extractQuery(json, "query")).isNull()
    }

    @Test
    fun extractQueryWithEmptyValue() {
        val query = "some query"
        val json = JSONObject()
            .put(
                QUERIES,
                JSONObject().put(
                    LIST,
                    JSONObject().put(
                        query,
                        JSONObject()
                    )
                )
            )
        assertThat(RemoteConfigJsonUtils.extractQuery(json, query)).isNull()
    }

    @Test
    fun extractQueryValue() {
        val query = "some query"
        val queryValue = "some query value"
        val json = JSONObject()
            .put(
                QUERIES,
                JSONObject()
                    .put(
                        LIST,
                        JSONObject()
                            .put(
                                query,
                                JSONObject()
                                    .put(
                                        URL,
                                        queryValue
                                    )
                            )
                    )
            )
        assertThat(RemoteConfigJsonUtils.extractQuery(json, query)).isEqualTo(queryValue)
    }

    private fun jsonWithFeatureValue(feature: String, value: Boolean) = jsonWithFeature(feature).apply {
        getJSONObject(FEATURES).getJSONObject(LIST).getJSONObject(feature).put(ENABLED, value)
    }

    private fun jsonWithFeature(feature: String): JSONObject = jsonWithFeatureList().apply {
        getJSONObject(FEATURES).getJSONObject(LIST).put(feature, JSONObject())
    }

    private fun jsonWithFeatureList(): JSONObject = jsonWithFeatures().apply {
        getJSONObject(FEATURES).put(LIST, JSONObject())
    }

    private fun jsonWithFeatures(): JSONObject = JSONObject().put(FEATURES, JSONObject())

    @Test
    fun `extractHosts for empty json`() {
        assertThat(RemoteConfigJsonUtils.extractHosts(JSONObject(), hostType)).isEmpty()
    }

    @Test
    fun `extractHosts for json with empty query hosts json`() {
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject())
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType)).isEmpty()
    }

    @Test
    fun `extractHosts for json with empty query host list json`() {
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject().apply {
                put(LIST, JSONObject())
            })
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType)).isEmpty()
    }

    @Test
    fun `extractHosts for json with wrong host type`() {
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject().apply {
                put(LIST, JSONObject().apply {
                    put("wrong type", JSONObject())
                })
            })
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType)).isEmpty()
    }

    @Test
    fun `extractHosts for json with empty host type json`() {
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject().apply {
                put(LIST, JSONObject().apply {
                    put(hostType, JSONObject())
                })
            })
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType)).isEmpty()
    }

    @Test
    fun `extract hosts for json with empty urls json array`() {
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject().apply {
                put(LIST, JSONObject().apply {
                    put(hostType, JSONObject().apply {
                        put(URLS, JSONArray())
                    })
                })
            })
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType)).isEmpty()
    }

    @Test
    fun `extractHosts for json with single host`() {
        val host = "Host #1"
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject().apply {
                put(LIST, JSONObject().apply {
                    put(hostType, JSONObject().apply {
                        put(URLS, JSONArray().apply {
                            put(host)
                        })
                    })
                })
            })
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType))
            .containsExactly(host.toByteArray())
    }

    @Test
    fun `extractHosts for json with multiple hosts`() {
        val firstHost = "Host #1"
        val secondHost = "Host #2"
        val thirdHost = "Host #3"
        val input = JSONObject().apply {
            put(QUERY_HOSTS, JSONObject().apply {
                put(LIST, JSONObject().apply {
                    put(hostType, JSONObject().apply {
                        put(URLS, JSONArray().apply {
                            put(firstHost)
                            put(secondHost)
                            put(thirdHost)
                        })
                    })
                })
            })
        }
        assertThat(RemoteConfigJsonUtils.extractHosts(input, hostType))
            .containsExactly(
                firstHost.toByteArray(),
                secondHost.toByteArray(),
                thirdHost.toByteArray()
            )
    }

    @Test
    fun `extractMillisFromSecondsOrDefault for filled value`() {
        val valueInJson = 200500L
        val input = JSONObject().put(valueKey, valueInJson)
        assertThat(RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(input, valueKey, 10L))
            .isEqualTo(TimeUnit.SECONDS.toMillis(valueInJson))
    }

    @Test
    fun `extractMillisFromSecondsOrDefault for empty json`() {
        val default = 4242L
        assertThat(RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(JSONObject(), valueKey, default))
            .isEqualTo(default)
    }

    @Test
    fun `extractMillisFromSecondsOrDefault for wrong value`() {
        val default = 124233L
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(input, valueKey, default))
            .isEqualTo(default)
    }
}
