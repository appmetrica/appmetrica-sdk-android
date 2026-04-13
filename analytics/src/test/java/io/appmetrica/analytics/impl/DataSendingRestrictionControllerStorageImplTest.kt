package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl.StorageImpl
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class DataSendingRestrictionControllerStorageImplTest : CommonTest() {
    private val dbStorage: PreferencesServiceDbStorage = mock {
        on { dataSendingRestrictedFromMainReporter } doReturn false
    }

    private val storage by setUp { StorageImpl(dbStorage) }

    @Test
    fun `store restriction from main reporter`() {
        storage.storeRestrictionFromMainReporter(true)
        verify(dbStorage).putDataSendingRestrictedFromMainReporter(true)
    }

    @Test
    fun `read restriction from main reporter`() {
        assertThat(storage.readRestrictionFromMainReporter()).isFalse()
        verify(dbStorage).dataSendingRestrictedFromMainReporter
    }
}
