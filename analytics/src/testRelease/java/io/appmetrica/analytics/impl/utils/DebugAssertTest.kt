package io.appmetrica.analytics.impl.utils

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class DebugAssertTest : CommonTest() {
    private val serviceKeyValueHelper: IKeyValueTableDbHelper = mock()

    @get:Rule
    val globalServiceLocatorRule: GlobalServiceLocatorRule = GlobalServiceLocatorRule()
    private val context by setUp { globalServiceLocatorRule.context }

    @get:Rule
    val clientServiceLocatorRule: ClientServiceLocatorRule = ClientServiceLocatorRule()

    @Before
    fun setUp() {
        val storageFactory = GlobalServiceLocator.getInstance().getStorageFactory()
        whenever(storageFactory.getServicePreferenceDbHelperForMigration(context)).thenReturn(serviceKeyValueHelper)
    }

    @Test
    fun autoInapp() {
        DebugAssert.assertMigrated(context, StorageType.AUTO_INAPP)
    }

    @Test
    fun clientVersionIsTheSame() {
        whenever(ClientServiceLocator.getInstance().getClientMigrationApiLevel(context))
            .thenReturn(BuildConfig.API_LEVEL.toLong())

        DebugAssert.assertMigrated(context, StorageType.CLIENT)
    }

    @Test
    fun clientVersionIsNotTheSame() {
        whenever(ClientServiceLocator.getInstance().getClientMigrationApiLevel(context))
            .thenReturn(BuildConfig.API_LEVEL.toLong() - 1)

        DebugAssert.assertMigrated(context, StorageType.CLIENT)
    }

    @Test
    fun serviceVersionIsTheSame() {
        val vitalCommonDataProvider: VitalCommonDataProvider = mock {
            on { lastMigrationApiLevel }.thenReturn(BuildConfig.API_LEVEL)
        }
        whenever(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().commonDataProviderForMigration)
            .thenReturn(vitalCommonDataProvider)

        DebugAssert.assertMigrated(context, StorageType.SERVICE)
    }

    @Test
    fun serviceVersionIsNotTheSame() {
        val vitalCommonDataProvider: VitalCommonDataProvider = mock {
            on { lastMigrationApiLevel }.thenReturn(BuildConfig.API_LEVEL - 1)
        }
        whenever(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().commonDataProviderForMigration)
            .thenReturn(vitalCommonDataProvider)

        DebugAssert.assertMigrated(context, StorageType.SERVICE)
    }
}
