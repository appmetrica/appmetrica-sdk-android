package io.appmetrica.analytics.testutils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MockProviderBundleTest : CommonTest() {

    @Test
    fun `mockedBundle should store and retrieve String values`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putString("key1", "value1")
        bundle.putString("key2", "value2")

        assertThat(bundle.getString("key1")).isEqualTo("value1")
        assertThat(bundle.getString("key2")).isEqualTo("value2")
    }

    @Test
    fun `mockedBundle should store and retrieve Int values`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putInt("count", 42)

        assertThat(bundle.getInt("count")).isEqualTo(42)
        assertThat(bundle.getInt("missing")).isEqualTo(0) // default value
    }

    @Test
    fun `mockedBundle should store and retrieve Long values`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putLong("timestamp", 123456789L)

        assertThat(bundle.getLong("timestamp")).isEqualTo(123456789L)
    }

    @Test
    fun `mockedBundle should store and retrieve Boolean values`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putBoolean("enabled", true)
        bundle.putBoolean("disabled", false)

        assertThat(bundle.getBoolean("enabled")).isTrue()
        assertThat(bundle.getBoolean("disabled")).isFalse()
    }

    @Test
    fun `mockedBundle should handle containsKey`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putString("exists", "value")

        assertThat(bundle.containsKey("exists")).isTrue()
        assertThat(bundle.containsKey("missing")).isFalse()
    }

    @Test
    fun `mockedBundle should handle size`() {
        val bundle = MockProvider.mockedBundle()

        assertThat(bundle.size()).isEqualTo(0)

        bundle.putString("key1", "value1")
        bundle.putInt("key2", 42)

        assertThat(bundle.size()).isEqualTo(2)
    }

    @Test
    fun `mockedBundle should handle remove`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putString("key", "value")
        assertThat(bundle.containsKey("key")).isTrue()

        bundle.remove("key")
        assertThat(bundle.containsKey("key")).isFalse()
    }

    @Test
    fun `mockedBundle should handle clear`() {
        val bundle = MockProvider.mockedBundle()

        bundle.putString("key1", "value1")
        bundle.putInt("key2", 42)
        assertThat(bundle.size()).isEqualTo(2)

        bundle.clear()
        assertThat(bundle.size()).isEqualTo(0)
    }

    @Test
    fun `mockedBundle should handle nested Bundle`() {
        val innerBundle = MockProvider.mockedBundle()
        innerBundle.putString("inner", "value")

        val outerBundle = MockProvider.mockedBundle()
        outerBundle.putBundle("nested", innerBundle)

        val retrieved = outerBundle.getBundle("nested")
        assertThat(retrieved).isEqualTo(innerBundle)
    }

    @Test
    fun `mockedBundle should handle default values in get methods`() {
        val bundle = MockProvider.mockedBundle()

        assertThat(bundle.getString("missing", "default")).isEqualTo("default")
        assertThat(bundle.getInt("missing", 999)).isEqualTo(999)
        assertThat(bundle.getLong("missing", 999L)).isEqualTo(999L)
        assertThat(bundle.getBoolean("missing", true)).isTrue()
    }
}
