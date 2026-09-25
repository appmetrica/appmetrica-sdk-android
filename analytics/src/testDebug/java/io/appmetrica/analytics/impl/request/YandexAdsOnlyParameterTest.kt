package io.appmetrica.analytics.impl.request

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class YandexAdsOnlyParameterTest : CommonTest() {

    @Test
    fun toQueryValue() {
        assertThat(YandexAdsOnlyParameter.toQueryValue(true)).isEqualTo("1")
        assertThat(YandexAdsOnlyParameter.toQueryValue(false)).isEqualTo("0")
    }
}
