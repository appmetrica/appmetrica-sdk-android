package io.appmetrica.analytics.impl

import android.content.res.Configuration
import android.os.LocaleList
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class LocalesHelperForNTest : CommonTest() {

    @get:Rule
    val sPhoneUtils = MockedStaticRule(PhoneUtils::class.java)

    private val configuration: Configuration = mock()

    @Test
    fun nullLocales() {
        whenever(configuration.locales).thenReturn(null)
        assertThat(LocalesHelperForN.getLocales(configuration)).isNotNull.isEmpty()
    }

    @Test
    fun emptyLocales() {
        whenever(configuration.locales).thenReturn(LocaleList.getEmptyLocaleList())
        assertThat(LocalesHelperForN.getLocales(configuration)).isNotNull.isEmpty()
    }

    @Test
    fun nonEmptyLocales() {
        val firstLocale = Locale("ru", "BY")
        val secondLocale = Locale("es", "ES")
        val thirdLocale = Locale("fr", "CA")
        val firstNormalizedLocale = "first_normalized_locale"
        val secondNormalizedLocale = "second_normalized_locale"
        val thirdNormalizedLocale = "third_normalized_locale"
        whenever(configuration.locales).thenReturn(LocaleList(firstLocale, secondLocale, thirdLocale))
        whenever(PhoneUtils.normalizedLocale(firstLocale)).thenReturn(firstNormalizedLocale)
        whenever(PhoneUtils.normalizedLocale(secondLocale)).thenReturn(secondNormalizedLocale)
        whenever(PhoneUtils.normalizedLocale(thirdLocale)).thenReturn(thirdNormalizedLocale)
        assertThat(LocalesHelperForN.getLocales(configuration))
            .isEqualTo(listOf(firstNormalizedLocale, secondNormalizedLocale, thirdNormalizedLocale))
    }
}
