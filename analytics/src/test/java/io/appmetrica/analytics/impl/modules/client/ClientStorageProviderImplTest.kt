package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.modules.ModulePreferencesAdapter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class ClientStorageProviderImplTest : CommonTest() {

    private val preferenceClientDbStorage = mock<PreferencesClientDbStorage>()
    private val identifier = "moduleIdentifier"

    @get:Rule
    val modulePreferencesAdapterMockedRule =
        MockedConstructionRule(ModulePreferencesAdapter::class.java)

    private val storageProviderImpl by setUp {
        ClientStorageProviderImpl(preferenceClientDbStorage)
    }

    @Test
    fun modulePreferences() {
        assertThat(storageProviderImpl.modulePreferences(identifier))
            .isEqualTo(modulePreferencesAdapterMockedRule.constructionMock.constructed()[0])
        assertThat(modulePreferencesAdapterMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(modulePreferencesAdapterMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(identifier, preferenceClientDbStorage)
    }
}
