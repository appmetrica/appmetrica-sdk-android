package io.appmetrica.analytics.impl

import android.app.Activity
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.nullable
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ScreenInfoRetrieverTest : CommonTest() {

    @Mock
    private lateinit var clientPreferences: PreferencesClientDbStorage
    @Mock
    private lateinit var extractor: ScreenInfoExtractor
    @Mock
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var screenInfoRetriever: ScreenInfoRetriever

    @Rule
    @JvmField
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)
    }

    @Test
    fun listenerRegistered() {
        verify(ClientServiceLocator.getInstance().activityAppearedListener).registerListener(screenInfoRetriever)
    }

    @Test
    fun retrieveScreenInfoNoActivityNull() {
        `when`(extractor.extractScreenInfo(context)).thenReturn(null)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isNull()
        verify(extractor).extractScreenInfo(context)
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    fun retrieveScreenInfoNoActivityNonNull() {
        val screenInfo = ScreenInfo(555, 666, 777, 88.8f)
        `when`(extractor.extractScreenInfo(context)).thenReturn(screenInfo)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(screenInfo)
        verify(extractor).extractScreenInfo(context)
        verify(clientPreferences).saveScreenInfo(screenInfo)
    }

    @Test
    fun retrieveScreenInfoNoActivityHasInitial() {
        clearInvocations(extractor, clientPreferences)

        val initialScreenInfo = ScreenInfo(555, 666, 777, 88.8f)
        `when`(clientPreferences.screenInfo).thenReturn(initialScreenInfo)
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(initialScreenInfo)
        verify(extractor, never()).extractScreenInfo(context)
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    fun retrieveScreenInfoHasActivityNullForActivityAndContext() {
        screenInfoRetriever.onActivityAppeared(activity)

        clearInvocations(extractor, clientPreferences)
        `when`(extractor.extractScreenInfo(context)).thenReturn(null)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(null)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isNull()
        verify(extractor).extractScreenInfo(activity)
        verify(extractor).extractScreenInfo(context)
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    fun retrieveScreenInfoHasActivityNullForActivityNonNullForContext() {
        val screenInfo = ScreenInfo(222, 333, 444, 55.5f)
        screenInfoRetriever.onActivityAppeared(activity)

        clearInvocations(extractor, clientPreferences)
        `when`(extractor.extractScreenInfo(context)).thenReturn(screenInfo)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(null)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(screenInfo)
        verify(extractor).extractScreenInfo(activity)
        verify(extractor).extractScreenInfo(context)
        verify(clientPreferences).saveScreenInfo(screenInfo)
    }

    @Test
    fun retrieveScreenInfoHasActivityNonNull() {
        val screenInfo = ScreenInfo(222, 333, 444, 55.5f)
        screenInfoRetriever.onActivityAppeared(activity)

        clearInvocations(extractor, clientPreferences)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(screenInfo)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(screenInfo)
        verify(extractor).extractScreenInfo(activity)
        verify(extractor, never()).extractScreenInfo(context)
        verify(clientPreferences).saveScreenInfo(screenInfo)
    }

    @Test
    fun retrieveScreenInfoHasActivityHasAnotherInitial() {
        val initialScreenInfo = ScreenInfo(666, 777, 888, 99.9f)
        val newScreenInfo = ScreenInfo(222, 333, 444, 55.5f)
        `when`(clientPreferences.screenInfo).thenReturn(initialScreenInfo)
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)
        screenInfoRetriever.onActivityAppeared(activity)

        clearInvocations(extractor, clientPreferences)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(newScreenInfo)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(newScreenInfo)
        verify(extractor).extractScreenInfo(activity)
        verify(extractor, never()).extractScreenInfo(context)
        verify(clientPreferences).saveScreenInfo(newScreenInfo)
    }

    @Test
    fun retrieveScreenInfoHasActivityHasSameInitial() {
        val initialScreenInfo = ScreenInfo(222, 333, 444, 55.5f)
        `when`(clientPreferences.screenInfo).thenReturn(initialScreenInfo)
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)
        screenInfoRetriever.onActivityAppeared(activity)

        clearInvocations(extractor, clientPreferences)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(initialScreenInfo)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(initialScreenInfo)
        verify(extractor).extractScreenInfo(activity)
        verify(extractor, never()).extractScreenInfo(context)
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    fun retrieveScreenInfoHasActivityHasInitialNullForActivity() {
        val initialScreenInfo = ScreenInfo(222, 333, 444, 55.5f)
        `when`(clientPreferences.screenInfo).thenReturn(initialScreenInfo)
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)
        screenInfoRetriever.onActivityAppeared(activity)

        clearInvocations(extractor, clientPreferences)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(null)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(initialScreenInfo)
        verify(extractor).extractScreenInfo(activity)
        verify(extractor, never()).extractScreenInfo(context)
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    fun onActivityAppearedHasCached() {
        val initialScreenInfo = ScreenInfo(222, 333, 444, 55.5f)
        `when`(clientPreferences.screenInfo).thenReturn(initialScreenInfo)
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)

        clearInvocations(extractor, clientPreferences)
        screenInfoRetriever.onActivityAppeared(activity)
        verify(extractor, never()).extractScreenInfo(activity)
    }

    @Test
    fun onActivityAppearedNoCachedNull() {
        `when`(extractor.extractScreenInfo(activity)).thenReturn(null)
        screenInfoRetriever.onActivityAppeared(activity)
        verify(extractor).extractScreenInfo(activity)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isNull()
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    fun onActivityAppearedNoCachedNonNull() {
        val screenInfo = ScreenInfo(222, 333, 444, 55.5f)
        `when`(extractor.extractScreenInfo(activity)).thenReturn(screenInfo)
        screenInfoRetriever.onActivityAppeared(activity)
        verify(extractor).extractScreenInfo(activity)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(screenInfo)
        verify(clientPreferences).saveScreenInfo(screenInfo)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun retrieveScreenInfoRNoActivityCheckedForDeprecated() {
        `when`(clientPreferences.isScreenSizeCheckedByDeprecated).thenReturn(true)
        screenInfoRetriever = ScreenInfoRetriever(context, clientPreferences, extractor)

        clearInvocations(extractor, clientPreferences)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isNull()
        verify(extractor, never()).extractScreenInfo(context)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun retrieveScreenInfoRNoActivityNotCheckedForDeprecatedNullForContext() {
        `when`(extractor.extractScreenInfo(context)).thenReturn(null)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isNull()
        verify(extractor).extractScreenInfo(context)
        verify(clientPreferences, never()).saveScreenInfo(nullable(ScreenInfo::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun retrieveScreenInfoRNullForActivityNotCheckedForDeprecatedNonNullForContext() {
        val screenInfo = ScreenInfo(222, 333, 444, 55.5f)
        `when`(extractor.extractScreenInfo(context)).thenReturn(screenInfo)
        assertThat(screenInfoRetriever.retrieveScreenInfo()).isEqualTo(screenInfo)
        verify(extractor).extractScreenInfo(context)
        verify(clientPreferences).saveScreenInfo(screenInfo)
    }
}
