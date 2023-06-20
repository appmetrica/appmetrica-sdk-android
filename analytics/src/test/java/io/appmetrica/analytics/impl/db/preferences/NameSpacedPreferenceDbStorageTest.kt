package io.appmetrica.analytics.impl.db.preferences

import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class NameSpacedPreferenceDbStorageTest(
    private val implementation: NameSpacedPreferenceDbStorage,
    keyPattern: String,
    private val description: String
) : CommonTest() {

    private val key = "Some key"
    private val storageKey = String.format(keyPattern, key)

    companion object {
        private val dbStorage = mock<IKeyValueTableDbHelper>()

        @ParameterizedRobolectricTestRunner.Parameters(name = "{2}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(PreferencesServiceDbStorage(dbStorage), "%s", PreferencesServiceDbStorage::class.simpleName),
            arrayOf(PreferencesComponentDbStorage(dbStorage), "%s", PreferencesComponentDbStorage::class.simpleName),
        )
    }

    @Before
    fun setUp() {
        // dbStorage is static mock
        clearInvocations(dbStorage)
    }

    @Test
    fun putString() {
        val value = "Some value"
        implementation.putString(key, value)
        verify(dbStorage).put(storageKey, value)
    }

    @Test
    fun `putString for nullable value`() {
        implementation.putString(key, null)
        verify(dbStorage).put(storageKey, null)
    }

    @Test
    fun getString() {
        val fallback = "fallback"
        val value = "value"
        whenever(dbStorage.getString(storageKey, fallback)).thenReturn(value)
        assertThat(implementation.getString(key, fallback)).isEqualTo(value)
    }

    @Test
    fun `getString with nullable fallback`() {
        whenever(dbStorage.getString(storageKey, null)).thenReturn(null)
        assertThat(implementation.getString(key, null)).isNull()
    }

    @Test
    fun putInt() {
        val value = 2321
        implementation.putInt(key, value)
        verify(dbStorage).put(storageKey, value)
    }

    @Test
    fun getInt() {
        val value = 2312
        val fallback = 321
        whenever(dbStorage.getInt(storageKey, fallback)).thenReturn(value)
        assertThat(implementation.getInt(key, fallback)).isEqualTo(value)
    }

    @Test
    fun putLong() {
        val value = 4323L
        implementation.putLong(key, value)
        verify(dbStorage).put(storageKey, value)
    }

    @Test
    fun getLong() {
        val value = 231232L
        val fallback = -12312L
        whenever(dbStorage.getLong(storageKey, fallback)).thenReturn(value)
        assertThat(implementation.getLong(key, fallback)).isEqualTo(value)
    }

    @Test
    fun `putBoolean for true`() {
        putBoolean(true)
    }

    @Test
    fun `putBoolean for false`() {
        putBoolean(false)
    }

    private fun putBoolean(value: Boolean) {
        implementation.putBoolean(key, value)
        verify(dbStorage).put(storageKey, value)
    }

    @Test
    fun `getBoolean for true`() {
        getBoolean(true)
    }

    @Test
    fun `getBoolean for false`() {
        getBoolean(false)
    }

    private fun getBoolean(value: Boolean) {
        val fallback = !value
        whenever(dbStorage.getBoolean(storageKey, fallback)).thenReturn(value)
        assertThat(implementation.getBoolean(key, fallback)).isEqualTo(value)
    }

    @Test
    fun remove() {
        implementation.remove(key)
        verify(dbStorage).remove(storageKey)
    }

    @Test
    fun contains() {
        whenever(dbStorage.containsKey(storageKey)).thenReturn(true)
        assertThat(implementation.contains(key)).isTrue()
    }
}
