package io.appmetrica.analytics.impl.startup.uuid

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.startup.CollectingFlags.CollectingFlagsBuilder
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.impl.startup.StartupStateModel.StartupStateBuilder
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)

class UuidFromStartupStateImporterTest : CommonTest() {

    private val context: Context = mock()

    private val protobufStateStorage: ProtobufStateStorage<StartupStateModel> = mock()

    private val startupStateStorageFactory: StorageFactory<StartupStateModel> = mock {
        on { create(context) } doReturn protobufStateStorage
    }

    @get:Rule
    val storageFactoryProviderMockedStaticRule = staticRule<StorageFactory.Provider> {
        on { StorageFactory.Provider.get(StartupStateModel::class.java) } doReturn startupStateStorageFactory
    }
    private val uuidFromStartupStateImporter: UuidFromStartupStateImporter by setUp { UuidFromStartupStateImporter() }

    @Test
    fun `get if uuid is exist`() {
        val uuid = UUID.randomUUID().toString()
        whenever(protobufStateStorage.read()).thenReturn(
            StartupStateBuilder(CollectingFlagsBuilder().build())
                .withUuid(uuid)
                .build()
        )
        assertThat(uuidFromStartupStateImporter[context]).isEqualTo(uuid)
    }

    @Test
    fun `get if uuid is not exist`() {
        whenever(protobufStateStorage.read()).thenReturn(StartupStateBuilder(CollectingFlagsBuilder().build()).build())
        assertThat(uuidFromStartupStateImporter[context]).isNull()
    }

    @Test
    fun `get if factory is missing`() {
        whenever(StorageFactory.Provider.get(StartupStateModel::class.java)).thenReturn(null)
        assertThat(uuidFromStartupStateImporter[context]).isNull()
    }

    @Test
    fun `get if throw`() {
        whenever(protobufStateStorage.read()).thenThrow(RuntimeException())
        assertThat(uuidFromStartupStateImporter[context]).isNull()
    }
}
