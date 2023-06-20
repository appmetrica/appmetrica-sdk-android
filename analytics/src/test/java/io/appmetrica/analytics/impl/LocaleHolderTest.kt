package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocaleHolderTest : CommonTest() {

    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var configuration: Configuration
    @Mock
    private lateinit var listener1: LocaleHolder.Listener
    @Mock
    private lateinit var listener2: LocaleHolder.Listener
    private lateinit var context: Context
    private lateinit var localeHolder: LocaleHolder
    @Rule
    @JvmField
    val sLocalesHelperForN = MockedStaticRule(LocalesHelperForN::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        `when`(context.resources).thenReturn(resources)
        `when`(resources.configuration).thenReturn(configuration)
    }

    @Test
    fun initialLocales() {
        val locales = listOf("en_US", "ru_UA")
        `when`(LocalesHelperForN.getLocales(configuration)).thenReturn(locales)
        localeHolder = LocaleHolder(context)
        assertThat(localeHolder.locales).isEqualTo(locales)
    }

    @Test
    fun updateLocalesNoListeners() {
        val oldLocales = listOf("en_US", "ru_UA")
        `when`(LocalesHelperForN.getLocales(configuration)).thenReturn(oldLocales)
        localeHolder = LocaleHolder(context)
        val newLocales = listOf("fr_FR", "es_US")
        `when`(LocalesHelperForN.getLocales(configuration)).thenReturn(newLocales)
        localeHolder.updateLocales(configuration)
        assertThat(localeHolder.locales).isEqualTo(newLocales)
    }

    @Test
    fun updateLocalesHasListeners() {
        `when`(LocalesHelperForN.getLocales(configuration)).thenReturn(emptyList())
        localeHolder = LocaleHolder(context)
        localeHolder.registerLocaleUpdatedListener(listener1)
        localeHolder.registerLocaleUpdatedListener(listener2)
        val newLocales = listOf("fr_FR", "es_US")
        `when`(LocalesHelperForN.getLocales(configuration)).thenReturn(newLocales)
        localeHolder.updateLocales(configuration)
        val inOrder = inOrder(listener1, listener2)
        inOrder.verify(listener1).onLocalesUpdated()
        inOrder.verify(listener2).onLocalesUpdated()
    }

    @Test
    fun instance() {
        localeHolder = LocaleHolder.getInstance(context)
        assertThat(LocaleHolder.getInstance(context)).isSameAs(localeHolder)
    }
}
