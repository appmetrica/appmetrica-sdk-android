package io.appmetrica.analytics.internal

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper.onCreate
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper.onInsertFinished
import io.appmetrica.analytics.impl.ContentProviderHelper
import io.appmetrica.analytics.impl.preloadinfo.ContentProviderHelperFactory
import io.appmetrica.analytics.impl.preloadinfo.ContentProviderHelperFactory.createClidsInfoHelper
import io.appmetrica.analytics.impl.preloadinfo.ContentProviderHelperFactory.createPreloadInfoHelper
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
internal class PreloadInfoContentProviderTest : CommonTest() {

    companion object {
        private const val AUTHORITY_SUFFIX = ".appmetrica.preloadinfo.retail"
    }

    private val preloadInfoHelper: ContentProviderHelper<PreloadInfoState> = mock()
    private val clidsInfoHelper: ContentProviderHelper<Map<String, String>> = mock()
    private val values: ContentValues = mock()

    private lateinit var contentProvider: PreloadInfoContentProvider
    private lateinit var validPreloadInfoUri: Uri
    private lateinit var validClidsInfoUri: Uri

    @get:Rule
    val contentProviderHelperFactoryMockedStaticRule = staticRule<ContentProviderHelperFactory>()

    @get:Rule
    val contentProviderFirstLaunchHelperMockedStaticRule = staticRule<ContentProviderFirstLaunchHelper>()

    @Before
    fun setUp() {
        validPreloadInfoUri = Uri.parse(
            "content://" + RuntimeEnvironment.getApplication().packageName + AUTHORITY_SUFFIX + "/preloadinfo"
        )
        validClidsInfoUri = Uri.parse(
            "content://" + RuntimeEnvironment.getApplication().packageName + AUTHORITY_SUFFIX + "/clids"
        )
        whenever(createClidsInfoHelper()).thenReturn(clidsInfoHelper)
        whenever(createPreloadInfoHelper()).thenReturn(preloadInfoHelper)

        contentProvider = PreloadInfoContentProvider()
    }

    @Test
    fun onCreate() {
        assertThat(contentProvider.onCreate()).isTrue()
        contentProviderFirstLaunchHelperMockedStaticRule.staticMock.verify(
            MockedStatic.Verification {
                onCreate(contentProvider)
            }
        )
    }

    @Test
    fun insertPreloadInfoWithoutOnCreate() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertClidsInfoWithoutOnCreate() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertPreloadInfoWithoutContextAtAll() {
        contentProvider.onCreate()
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertClidsInfoWithoutContextAtAll() {
        contentProvider.onCreate()
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertPreloadInfoWithoutContextInOnCreate() {
        contentProvider.onCreate()
        setContentProviderContext(RuntimeEnvironment.getApplication())
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertClidsInfoWithoutContextInOnCreate() {
        contentProvider.onCreate()
        setContentProviderContext(RuntimeEnvironment.getApplication())
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertBadUri() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        val uri = Uri.parse(validPreloadInfoUri.toString() + "/1")
        assertThat(contentProvider.insert(uri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertPreloadInfoValuesAreNull() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.insert(validPreloadInfoUri, null)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertClidsInfoValuesAreNull() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.insert(validClidsInfoUri, null)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertPreloadInfoNoContext() {
        setContentProviderContext(null)
        contentProvider.onCreate()
        contentProvider.insert(validPreloadInfoUri, values)
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertClidsInfoNoContext() {
        setContentProviderContext(null)
        contentProvider.onCreate()
        contentProvider.insert(validClidsInfoUri, values)
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertPreloadInfoEverythingIsFine() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull()
        verify(preloadInfoHelper).handle(RuntimeEnvironment.getApplication(), values)
        verifyNoInteractions(clidsInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertClidsInfoEverythingIsFine() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull()
        verify(clidsInfoHelper).handle(RuntimeEnvironment.getApplication(), values)
        verifyNoInteractions(preloadInfoHelper)
        verifyOnInsertFinished()
    }

    @Test
    fun insertPreloadInfoAfterDisable() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        contentProvider.disable()
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        contentProviderFirstLaunchHelperMockedStaticRule.staticMock.verify(
            MockedStatic.Verification { onInsertFinished() },
            never()
        )
    }

    @Test
    fun insertClidsInfoAfterDisable() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        contentProvider.disable()
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull()
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper)
        contentProviderFirstLaunchHelperMockedStaticRule.staticMock.verify(
            MockedStatic.Verification { onInsertFinished() },
            never()
        )
    }

    @Test
    fun delete() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.delete(validPreloadInfoUri, null, null)).isEqualTo(-1)
        assertThat(contentProvider.delete(validClidsInfoUri, null, null)).isEqualTo(-1)
    }

    @Test
    fun update() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.update(validPreloadInfoUri, values, null, null)).isEqualTo(-1)
        assertThat(contentProvider.update(validClidsInfoUri, values, null, null)).isEqualTo(-1)
    }

    @Test
    fun query() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(
            contentProvider.query(
                validPreloadInfoUri,
                null,
                null,
                null,
                null
            )
        ).isNull()
        assertThat(
            contentProvider.query(
                validClidsInfoUri,
                null,
                null,
                null,
                null
            )
        ).isNull()
    }

    @Test
    fun getTypeIsNull() {
        setContentProviderContext(RuntimeEnvironment.getApplication())
        contentProvider.onCreate()
        assertThat(contentProvider.getType(validPreloadInfoUri)).isNull()
        assertThat(contentProvider.getType(validClidsInfoUri)).isNull()
    }
    private fun setContentProviderContext(context: Context?) {
        ReflectionHelpers.setField(contentProvider, "mContext", context)
    }
    private fun verifyOnInsertFinished() {
        contentProviderFirstLaunchHelperMockedStaticRule.staticMock.verify(
            MockedStatic.Verification { onInsertFinished() }
        )
    }
}
