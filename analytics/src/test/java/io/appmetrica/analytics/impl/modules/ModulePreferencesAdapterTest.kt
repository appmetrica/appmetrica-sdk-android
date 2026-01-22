package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ModulePreferencesAdapterTest : CommonTest() {

    private val identifier = "some_id"
    private val preferences = mock<PreferencesServiceDbStorage> {
        on { putString(any(), anyOrNull()) } doReturn mock
        on { putLong(any(), any()) } doReturn mock
        on { putBoolean(any(), any()) } doReturn mock
        on { putInt(any(), any()) } doReturn mock
    }

    private val key = "some key"
    private val preferenceKey = "$key-$identifier"

    private val adapter = ModulePreferencesAdapter(identifier, preferences)

    @Test
    fun putString() {
        val value = "some value"
        adapter.putString(key, value)
        inOrder(preferences) {
            verify(preferences).putString(preferenceKey, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun putStringIfNull() {
        adapter.putString(key, null)
        inOrder(preferences) {
            verify(preferences).putString(preferenceKey, null)
            verify(preferences).commit()
        }
    }

    @Test
    fun getStringWithFallbackIfReturnValue() {
        val value = "some value"
        val fallback = "some fallback"
        whenever(preferences.getString(preferenceKey, fallback)).thenReturn(value)
        assertThat(adapter.getString(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getStringWithFallbackIfReturnFallback() {
        val fallback = "some fallback"
        whenever(preferences.getString(preferenceKey, fallback)).thenReturn(fallback)
        assertThat(adapter.getString(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun getStringWithNullFallbackIfReturnValue() {
        val value = "some value"
        whenever(preferences.getString(preferenceKey, null)).thenReturn(value)
        assertThat(adapter.getString(key, null)).isEqualTo(value)
    }

    @Test
    fun getStringWithNullFallbackIfReturnFallback() {
        whenever(preferences.getString(key, null)).thenReturn(null)
        assertThat(adapter.getString(key, null)).isNull()
    }

    @Test
    fun getStringWithFallbackIfReturnNullValue() {
        val fallback = "some fallback"
        whenever(preferences.getString(key, fallback)).thenReturn(null)
        assertThat(adapter.getString(key, fallback)).isNull()
    }

    @Test
    fun getString() {
        val value = "some value"
        whenever(preferences.getString(preferenceKey, null)).thenReturn(value)
        assertThat(adapter.getString(key)).isEqualTo(value)
    }

    @Test
    fun getStringIfNull() {
        whenever(preferences.getString(key, null)).thenReturn(null)
        assertThat(adapter.getString(key)).isNull()
    }

    @Test
    fun putLong() {
        val value = 1242L
        adapter.putLong(key, value)
        inOrder(preferences) {
            verify(preferences).putLong(preferenceKey, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun getLongWithFallbackIfReturnValue() {
        val value = 2421L
        val fallback = 34323L
        whenever(preferences.getLong(preferenceKey, fallback)).thenReturn(value)
        assertThat(adapter.getLong(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getLongWithFallbackIfReturnFallback() {
        val fallback = 9876543L
        whenever(preferences.getLong(preferenceKey, fallback)).thenReturn(fallback)
        assertThat(adapter.getLong(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun getLong() {
        val value = 234352L
        whenever(preferences.getLong(preferenceKey, 0L)).thenReturn(value)
        assertThat(adapter.getLong(key)).isEqualTo(value)
    }

    @Test
    fun putInt() {
        val value = 600
        adapter.putInt(key, value)
        inOrder(preferences) {
            verify(preferences).putInt(key, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun getInt() {
        val value = 134
        whenever(preferences.getInt(key, 0)).thenReturn(value)
        assertThat(adapter.getInt(key)).isEqualTo(value)
    }

    @Test
    fun `getInt with fallback`() {
        val value = 34234
        val fallback = 3423
        whenever(preferences.getInt(key, fallback)).thenReturn(value)
        assertThat(adapter.getInt(key, fallback)).isEqualTo(value)
    }

    @Test
    fun `getInt with fallback if return fallback`() {
        val fallback = 12
        whenever(preferences.getInt(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getInt(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun putBooleanIfTrue() {
        adapter.putBoolean(key, true)
        inOrder(preferences) {
            verify(preferences).putBoolean(preferenceKey, true)
            verify(preferences).commit()
        }
    }

    @Test
    fun putBooleanIfFalse() {
        adapter.putBoolean(key, false)
        inOrder(preferences) {
            verify(preferences).putBoolean(preferenceKey, false)
            verify(preferences).commit()
        }
    }

    @Test
    fun getBooleanWithFallbackIfReturnTrueValue() {
        val value = true
        val fallback = false
        whenever(preferences.getBoolean(key, fallback)).thenReturn(value)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getBooleanWithFallbackIfReturnFalseValue() {
        val value = false
        val fallback = true
        whenever(preferences.getBoolean(key, fallback)).thenReturn(value)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getBooleanWithFallbackIfReturnTrueFallback() {
        val fallback = true
        whenever(preferences.getBoolean(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun getBooleanWithFallbackIfReturnFalseFallback() {
        val fallback = false
        whenever(preferences.getBoolean(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(fallback)
    }
}
