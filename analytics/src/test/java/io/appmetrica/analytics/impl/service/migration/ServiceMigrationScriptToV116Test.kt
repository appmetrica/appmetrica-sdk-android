package io.appmetrica.analytics.impl.service.migration

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.db.storage.BinaryDataHelper
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class ServiceMigrationScriptToV116Test : CommonTest() {

    private val startupStateModelCaptor = argumentCaptor<StartupStateModel>()

    private val context = mock<Context>()
    private val storageFactory = mock<StorageFactory<StartupStateModel>>()
    private val protobufStateStorage = mock<ProtobufStateStorage<StartupStateModel>>()
    private val startupStateModel = mock<StartupStateModel>()
    private val startupStateBuilder = mock<StartupStateModel.StartupStateBuilder>()
    private val newStartupStateModel = mock<StartupStateModel>()

    private val databaseStorage = mock<DatabaseStorage>()
    private val serviceBinaryDataHelperForMigration: IBinaryDataHelper = mock()
    private val databaseStorageFactory = mock<DatabaseStorageFactory> {
        on {
            createLegacyStorageForMigration(
                eq("autoinapp-old"),
                any(),
                any()
            )
        } doReturn databaseStorage
        on { serviceBinaryDataHelperForMigration } doReturn serviceBinaryDataHelperForMigration
    }

    @get:Rule
    val databaseStorageFactoryRule = staticRule<DatabaseStorageFactory> {
        on { DatabaseStorageFactory.getInstance(context) } doReturn databaseStorageFactory
    }

    private val oldDbValue = "old database value".toByteArray()

    private var binaryDataHelperValue: ByteArray? = oldDbValue

    @get:Rule
    val binaryDataHelperRule = constructionRule<BinaryDataHelper> {
        on { get("auto_inapp_collecting_info_data") } doReturn binaryDataHelperValue
    }

    @get:Rule
    val storageFactoryProviderMockedStaticRule = staticRule<StorageFactory.Provider> {
        on { StorageFactory.Provider.get(StartupStateModel::class.java) } doReturn storageFactory
    }

    private lateinit var serviceMigrationScriptToV116: ServiceMigrationScriptToV116

    @Before
    fun setUp() {
        whenever(storageFactory.createForMigration(context)).thenReturn(protobufStateStorage)
        whenever(protobufStateStorage.read()).thenReturn(startupStateModel)
        whenever(startupStateModel.buildUpon()).thenReturn(startupStateBuilder)
        whenever(startupStateBuilder.withObtainTime(any())).thenReturn(startupStateBuilder)
        whenever(startupStateBuilder.build()).thenReturn(newStartupStateModel)

        serviceMigrationScriptToV116 = ServiceMigrationScriptToV116()
    }

    @Test
    fun run() {
        serviceMigrationScriptToV116.run(context)

        verify(protobufStateStorage).save(startupStateModelCaptor.capture())
        assertThat(startupStateModelCaptor.firstValue).isSameAs(newStartupStateModel)
        verify(startupStateBuilder).withObtainTime(0)

        verify(serviceBinaryDataHelperForMigration).insert(
            "auto_inapp_collecting_info_data",
            oldDbValue
        )
    }

    @Test
    fun runIfNoOldValue() {
        binaryDataHelperValue = null

        serviceMigrationScriptToV116.run(context)

        verify(protobufStateStorage).save(startupStateModelCaptor.capture())
        assertThat(startupStateModelCaptor.firstValue).isSameAs(newStartupStateModel)
        verify(startupStateBuilder).withObtainTime(0)

        verifyNoInteractions(serviceBinaryDataHelperForMigration)
    }
}
