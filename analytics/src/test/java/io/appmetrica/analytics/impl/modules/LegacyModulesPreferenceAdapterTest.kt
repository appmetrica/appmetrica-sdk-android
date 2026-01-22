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

internal class LegacyModulesPreferenceAdapterTest : CommonTest() {

    private val preferences = mock<PreferencesServiceDbStorage> {
        on { putString(any(), anyOrNull()) } doReturn mock
        on { putLong(any(), any()) } doReturn mock
        on { putBoolean(any(), any()) } doReturn mock
        on { putInt(any(), any()) } doReturn mock
    }

    private val adapter = LegacyModulePreferenceAdapter(preferences)

    private val key = "Some key"
    private val stringValue = "Some value"

    @Test
    fun putLegacyString() {
        adapter.putString(key, stringValue)
        inOrder(preferences) {
            verify(preferences).putString(key, stringValue)
            verify(preferences).commit()
        }
    }

    @Test
    fun putLegacyStringIfNull() {
        adapter.putString(key, null)
        inOrder(preferences) {
            verify(preferences).putString(key, null)
            verify(preferences).commit()
        }
    }

    @Test
    fun getLegacyStringWithFallback() {
        val fallback = "non null fallback"
        whenever(preferences.getString(key, fallback)).thenReturn(stringValue)
        assertThat(adapter.getString(key, fallback)).isEqualTo(stringValue)
    }

    @Test
    fun getLegacyStringWithFallbackReturnFallback() {
        val fallback = "non null fallback"
        whenever(preferences.getString(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getString(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun getLegacyStringWithNullFallback() {
        whenever(preferences.getString(key, null)).thenReturn(null)
        assertThat(adapter.getString(key, null)).isNull()
    }

    @Test
    fun getLegacyString() {
        val value = "Some value"
        whenever(preferences.getString(key, null)).thenReturn(value)
        assertThat(adapter.getString(key)).isEqualTo(value)
    }

    @Test
    fun getLegacyStringIfNull() {
        whenever(preferences.getString(key, null)).thenReturn(null)
        assertThat(adapter.getString(key)).isNull()
    }

    @Test
    fun putLegacyLong() {
        val value = 242L
        adapter.putLong(key, value)
        inOrder(preferences) {
            verify(preferences).putLong(key, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun getLegacyLong() {
        val value = 12421L
        whenever(preferences.getLong(key, 0L)).thenReturn(value)
        assertThat(adapter.getLong(key)).isEqualTo(value)
    }

    @Test
    fun getLegacyLongWithFallbackIfReturnValue() {
        val value = 14L
        val fallback = 45433L
        whenever(preferences.getLong(key, fallback)).thenReturn(value)
        assertThat(adapter.getLong(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getLegacyLongWithFallbackIfReturnFallback() {
        val fallback = 12312L
        whenever(preferences.getLong(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getLong(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun putLegacyInt() {
        val value = 400
        adapter.putInt(key, value)
        inOrder(preferences) {
            verify(preferences).putInt(key, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun getLegacyInt() {
        val value = 500
        whenever(preferences.getInt(key, 0)).thenReturn(value)
        assertThat(adapter.getInt(key)).isEqualTo(value)
    }

    @Test
    fun `getLegacyInt with fallback`() {
        val value = 500
        val fallback = 800
        whenever(preferences.getInt(key, fallback)).thenReturn(value)
        assertThat(adapter.getInt(key, fallback)).isEqualTo(value)
    }

    @Test
    fun `getLegacyInt if return fallback`() {
        val fallback = 200
        whenever(preferences.getInt(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getInt(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun putLegacyBooleanIfTrue() {
        val value = true
        adapter.putBoolean(key, value)
        inOrder(preferences) {
            verify(preferences).putBoolean(key, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun putLegacyBooleanIfFalse() {
        val value = false
        adapter.putBoolean(key, value)
        inOrder(preferences) {
            verify(preferences).putBoolean(key, value)
            verify(preferences).commit()
        }
    }

    @Test
    fun getLegacyBooleanIfReturnTrueValue() {
        val value = true
        val fallback = false
        whenever(preferences.getBoolean(key, fallback)).thenReturn(value)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getLegacyBooleanIfReturnFalseValue() {
        val value = false
        val fallback = true
        whenever(preferences.getBoolean(key, fallback)).thenReturn(value)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(value)
    }

    @Test
    fun getLegacyBooleanIfReturnTrueFallback() {
        val value = false
        val fallback = true
        whenever(preferences.getBoolean(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(fallback)
    }

    @Test
    fun getLegacyBooleanIfReturnFalseFallback() {
        val value = true
        val fallback = false
        whenever(preferences.getBoolean(key, fallback)).thenReturn(fallback)
        assertThat(adapter.getBoolean(key, fallback)).isEqualTo(fallback)
    }
}
