package io.appmetrica.analytics.impl.service.migration

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ServiceMigrationScriptToV115Test : CommonTest() {

    private val startupStateModelCaptor = argumentCaptor<StartupStateModel>()

    private val context = mock<Context>()
    private val storageFactory = mock<StorageFactory<StartupStateModel>>()
    private val protobufStateStorage = mock<ProtobufStateStorage<StartupStateModel>>()
    private val startupStateModel = mock<StartupStateModel>()
    private val startupStateBuilder = mock<StartupStateModel.StartupStateBuilder>()
    private val newStartupStateModel = mock<StartupStateModel>()

    @get:Rule
    val storageFactoryProviderMockedStaticRule = staticRule<StorageFactory.Provider> {
        on { StorageFactory.Provider.get(StartupStateModel::class.java) } doReturn storageFactory
    }

    private lateinit var serviceMigrationScriptToV115: ServiceMigrationScriptToV115

    @Before
    fun setUp() {
        whenever(storageFactory.createForMigration(context)).thenReturn(protobufStateStorage)
        whenever(protobufStateStorage.read()).thenReturn(startupStateModel)
        whenever(startupStateModel.buildUpon()).thenReturn(startupStateBuilder)
        whenever(startupStateBuilder.withObtainTime(any())).thenReturn(startupStateBuilder)
        whenever(startupStateBuilder.build()).thenReturn(newStartupStateModel)

        serviceMigrationScriptToV115 = ServiceMigrationScriptToV115()
    }

    @Test
    fun run() {
        serviceMigrationScriptToV115.run(context)
        verify(protobufStateStorage).save(startupStateModelCaptor.capture())
        assertThat(startupStateModelCaptor.firstValue).isSameAs(newStartupStateModel)
        verify(startupStateBuilder).withObtainTime(0)
    }
}
