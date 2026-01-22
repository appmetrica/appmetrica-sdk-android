package io.appmetrica.analytics.impl

import android.content.res.Configuration
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
internal class LocaleExtractorTest : CommonTest() {

    private val deprecatedLocale: Locale = mock()
    private val configuration: Configuration = Configuration().apply {
        locale = deprecatedLocale
    }

    @get:Rule
    val androidUtilsMockedStaticRule = staticRule<AndroidUtils>()

    private val localesForN = listOf("Locale for N #1", "Locale for N #2")

    @get:Rule
    val localeHelperForNMockedStaticRule = staticRule<LocalesHelperForN> {
        on { LocalesHelperForN.getLocales(configuration) } doReturn localesForN
    }

    private val deprecatedLocaleString = "Deprecated locale"

    @get:Rule
    val phoneUtilsMockedStaticRule = staticRule<PhoneUtils> {
        on { PhoneUtils.normalizedLocale(deprecatedLocale) } doReturn deprecatedLocaleString
    }

    private val localeExtractor: LocaleExtractor by setUp { LocaleExtractor() }

    @Test
    fun extractLocalesForN() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)).thenReturn(true)
        assertThat(localeExtractor.extractLocales(configuration)).isEqualTo(localesForN)
    }

    @Test
    fun extractLocalesPreN() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)).thenReturn(false)
        assertThat(localeExtractor.extractLocales(configuration)).isEqualTo(listOf(deprecatedLocaleString))
    }
}
