package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@Config(sdk = [Build.VERSION_CODES.M])
@RunWith(RobolectricTestRunner::class)
class LocaleHolderPreNTest : CommonTest() {

    @Mock
    private lateinit var resources: Resources
    private lateinit var configuration: Configuration
    @Mock
    private lateinit var listener1: LocaleHolder.Listener
    @Mock
    private lateinit var listener2: LocaleHolder.Listener
    private lateinit var context: Context
    @Rule
    @JvmField
    val sPhoneUtils = MockedStaticRule(PhoneUtils::class.java)
    private lateinit var localeHolder: LocaleHolder

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        `when`(context.resources).thenReturn(resources)
        configuration = Configuration()
        `when`(resources.configuration).thenReturn(configuration)
    }

    @Test
    fun initialLocales() {
        val locale = Locale("ru", "UA")
        val normalizedLocale = "normalized_locale"
        `when`(PhoneUtils.normalizedLocale(locale)).thenReturn(normalizedLocale)
        configuration.locale = locale
        localeHolder = LocaleHolder(context)
        Assertions.assertThat(localeHolder.locales).isEqualTo(listOf(normalizedLocale))
    }

    @Test
    fun updateLocalesNoListeners() {
        val oldLocale = Locale("ru", "UA")
        val normalizedOldLocale = "normalized_old_locale"
        `when`(PhoneUtils.normalizedLocale(oldLocale)).thenReturn(normalizedOldLocale)
        configuration.locale = oldLocale
        localeHolder = LocaleHolder(context)
        val newLocale = Locale("es", "ES")
        val normalizedNewLocale = "normalized_new_locale"
        `when`(PhoneUtils.normalizedLocale(newLocale)).thenReturn(normalizedNewLocale)
        configuration.locale = newLocale
        localeHolder.updateLocales(configuration)
        Assertions.assertThat(localeHolder.locales).isEqualTo(listOf(normalizedNewLocale))
    }

    @Test
    fun updateLocalesHasListeners() {
        localeHolder = LocaleHolder(context)
        localeHolder.registerLocaleUpdatedListener(listener1)
        localeHolder.registerLocaleUpdatedListener(listener2)
        val newLocale = Locale("fr", "CA")
        val normalizedNewLocale = "normalized_new_locale"
        `when`(PhoneUtils.normalizedLocale(newLocale)).thenReturn(normalizedNewLocale)
        configuration.locale = newLocale
        localeHolder.updateLocales(configuration)
        val inOrder = Mockito.inOrder(listener1, listener2)
        inOrder.verify(listener1).onLocalesUpdated()
        inOrder.verify(listener2).onLocalesUpdated()
    }
}
