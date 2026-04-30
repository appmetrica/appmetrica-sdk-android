package io.appmetrica.analytics.adrevenue.other.impl

import android.os.Bundle
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class BundleToJsonConverterTest : CommonTest() {

    @Test
    fun convertWithValues() {
        val bundle = Bundle().apply {
            putString("key1", "value1")
            putInt("key2", 42)
            putBoolean("key3", true)
        }

        val result = BundleToJsonConverter.convert(bundle)
        val json = JSONObject(result)

        assertThat(json.getString("key1")).isEqualTo("value1")
        assertThat(json.getString("key2")).isEqualTo("42")
        assertThat(json.getString("key3")).isEqualTo("true")
    }

    @Test
    fun convertEmptyBundle() {
        val result = BundleToJsonConverter.convert(Bundle())
        val json = JSONObject(result)

        assertThat(json.length()).isZero()
    }
}
