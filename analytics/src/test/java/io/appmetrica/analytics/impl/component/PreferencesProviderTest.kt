package io.appmetrica.analytics.impl.component

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentUnitFieldsFactory.PreferencesProvider
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.db.storage.ServiceStorageFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreferencesProviderTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val context: Context = mock()
    private val componentId: ComponentId = mock()

    private val keyValueTableDbHelper: IKeyValueTableDbHelper = mock()

    private val storageFactory: ServiceStorageFactory = mock {
        on { getComponentPreferenceDbHelper(context, componentId) } doReturn keyValueTableDbHelper
    }

    private val preferencesProvider: PreferencesProvider by setUp {
        whenever(GlobalServiceLocator.getInstance().context).doReturn(context)
        whenever(GlobalServiceLocator.getInstance().storageFactory).doReturn(storageFactory)
        PreferencesProvider(context, componentId)
    }

    @Test
    fun getComponentPreferences() {
        assertThat(preferencesProvider.createPreferencesComponentDbStorage())
            .isNotNull()
            .isExactlyInstanceOf(PreferencesComponentDbStorage::class.java)

        verify(storageFactory).getComponentPreferenceDbHelper(context, componentId)
    }
}
