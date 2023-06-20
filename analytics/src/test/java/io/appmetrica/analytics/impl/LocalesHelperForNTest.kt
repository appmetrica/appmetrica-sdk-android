package io.appmetrica.analytics.impl

import android.content.res.Configuration
import android.os.LocaleList
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class LocalesHelperForNTest : CommonTest() {

    @Rule
    @JvmField
    val sPhoneUtils = MockedStaticRule(PhoneUtils::class.java)
    @Mock
    private lateinit var configuration: Configuration

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun nullLocales() {
        `when`(configuration.locales).thenReturn(null)
        assertThat(LocalesHelperForN.getLocales(configuration)).isNotNull.isEmpty()
    }

    @Test
    fun emptyLocales() {
        `when`(configuration.locales).thenReturn(LocaleList.getEmptyLocaleList())
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
        `when`(configuration.locales).thenReturn(LocaleList(firstLocale, secondLocale, thirdLocale))
        `when`(PhoneUtils.normalizedLocale(firstLocale)).thenReturn(firstNormalizedLocale)
        `when`(PhoneUtils.normalizedLocale(secondLocale)).thenReturn(secondNormalizedLocale)
        `when`(PhoneUtils.normalizedLocale(thirdLocale)).thenReturn(thirdNormalizedLocale)
        assertThat(LocalesHelperForN.getLocales(configuration))
            .isEqualTo(listOf(firstNormalizedLocale, secondNormalizedLocale, thirdNormalizedLocale))
    }
}
