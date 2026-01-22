package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.SharedPreferences
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class PreferencesBasedModuleEntryPointTest : CommonTest() {

    private val prefValueName = "value name"
    private val prefValue = "value"
    private val prefName = "pref name"

    private val preferences: SharedPreferences = mock {
        on { getString(prefValueName, "") } doReturn prefValue
    }

    private val context: Context = mock {
        on { getSharedPreferences(prefName, Context.MODE_PRIVATE) } doReturn preferences
    }

    private val preferencesBasedModuleEntryPoint by setUp {
        PreferencesBasedModuleEntryPoint(context, prefName, prefValueName)
    }

    @Test
    fun className() {
        assertThat(preferencesBasedModuleEntryPoint.className).isEqualTo(prefValue)
    }
}
