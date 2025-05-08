package io.appmetrica.analytics.coreutils.internal.collection

import android.os.Bundle
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class CollectionUtilsTestKt : CommonTest() {

    @Test
    fun `getFirstOrNull for null`() {
        assertThat(CollectionUtils.getFirstOrNull(null as List<Any>?))
    }

    @Test
    fun `getFirstOrNull for empty list`() {
        assertThat(CollectionUtils.getFirstOrNull(emptyList<Any>())).isNull()
    }

    @Test
    fun `getFirstOrNull for list with null`() {
        assertThat(CollectionUtils.getFirstOrNull(listOf(null as Any?, null as Any?))).isNull()
    }

    @Test
    fun `getFirstOrNull for list with single item`() {
        val item = Object()
        assertThat(CollectionUtils.getFirstOrNull(listOf(item))).isEqualTo(item)
    }

    @Test
    fun `getFirstOrNull for list with multiple items`() {
        val first = Object()
        assertThat(CollectionUtils.getFirstOrNull(listOf(first, Object(), Object(), Object()))).isEqualTo(first)
    }

    @Test
    fun `mapToBundle for empty`() {
        assertThat(CollectionUtils.mapToBundle(emptyMap()).isEmpty).isTrue()
    }

    @Test
    fun `mapToBundle for empty byte array`() {
        val key = "key"
        val result = CollectionUtils.mapToBundle(mapOf(key to ByteArray(0)))
        assertThat(result.size()).isEqualTo(1)
        assertThat(result.getByteArray(key)).isEmpty()
    }

    @Test
    fun `mapToBundle for single item`() {
        val key = "key"
        val value = Random(100).nextBytes(1024)
        val result = CollectionUtils.mapToBundle(mapOf(key to value))
        assertThat(result.size()).isEqualTo(1)
        assertThat(result.getByteArray(key)).isEqualTo(value)
    }

    @Test
    fun `mapToBundle for multiple items`() {
        val random = Random(100)
        val firstKey = "firstKey"
        val secondKey = "secondKey"
        val thirdKey = "thirdKey"
        val firstValue = random.nextBytes(1024)
        val secondValue = random.nextBytes(2048)
        val thirdValue = random.nextBytes(4096)

        val result = CollectionUtils.mapToBundle(
            mapOf(
                firstKey to firstValue,
                secondKey to secondValue,
                thirdKey to thirdValue
            )
        )

        assertThat(result.size()).isEqualTo(3)
        assertThat(result.getByteArray(firstKey)).isEqualTo(firstValue)
        assertThat(result.getByteArray(secondKey)).isEqualTo(secondValue)
        assertThat(result.getByteArray(thirdKey)).isEqualTo(thirdValue)
    }

    @Test
    fun `bundleToMap for null`() {
        assertThat(CollectionUtils.bundleToMap(null))
            .isNotNull
            .isEmpty()
    }

    fun `bundleToMap for empty`() {
        assertThat(CollectionUtils.bundleToMap(Bundle())).isEmpty()
    }

    @Test
    fun `bundleToMap for single int item`() {
        val key = "key"
        assertThat(CollectionUtils.bundleToMap(Bundle().apply { putInt(key, 100) })).isEmpty()
    }

    @Test
    fun `bundleToMap for string item`() {
        val key = "key"
        assertThat(CollectionUtils.bundleToMap(Bundle().apply { putString(key, "Value") })).isEmpty()
    }

    @Test
    fun `bundleToMap for byte item`() {
        val key = "key"
        assertThat(CollectionUtils.bundleToMap(Bundle().apply { putByte(key, Random(100).nextBytes(1).first()) }))
            .isEmpty()
    }

    @Test
    fun `bundleToMap for single item`() {
        val key = "key"
        val value = Random(100).nextBytes(1024)
        val input = Bundle().apply { putByteArray(key, value) }
        val expected = mapOf(key to value)
        assertThat(CollectionUtils.bundleToMap(input)).isEqualTo(expected)
    }

    @Test
    fun `bundleToMap for multiple items`() {
        val random = Random(100)
        val firstKey = "first key"
        val secondKey = "second key"
        val thirdKey = "third key"
        val firstValue = random.nextBytes(1024)
        val secondValue = random.nextBytes(2048)
        val thirdValue = random.nextBytes(4096)

        val input = Bundle().apply {
            putByteArray(firstKey, firstValue)
            putByteArray(secondKey, secondValue)
            putByteArray(thirdKey, thirdValue)
        }

        val expected = mapOf(
            firstKey to firstValue,
            secondKey to secondValue,
            thirdKey to thirdValue
        )

        assertThat(CollectionUtils.bundleToMap(input)).isEqualTo(expected)
    }

    @Test
    fun `nullIfEmptyList for null`() {
        assertThat(CollectionUtils.nullIfEmptyList(null as List<Any>?)).isNull()
    }

    @Test
    fun `nullIfEmptyList for empty list`() {
        assertThat(CollectionUtils.nullIfEmptyList(emptyList<Any>())).isNull()
    }

    @Test
    fun `nullIfEmpty for filled list`() {
        val element = Any()
        assertThat(CollectionUtils.nullIfEmptyList(listOf(element))).containsExactly(element)
    }

    @Test
    fun `isNullOrEmpty for null`() {
        assertThat(CollectionUtils.isNullOrEmpty(null)).isTrue()
    }

    @Test
    fun `isNullOrEmpty for empty`() {
        assertThat(CollectionUtils.isNullOrEmpty(emptyList<Any>())).isTrue()
    }

    @Test
    fun `isNullOrEmpty for filled`() {
        assertThat(CollectionUtils.isNullOrEmpty(listOf(Any()))).isFalse()
    }
}
