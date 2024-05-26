package io.appmetrica.analytics.impl.component.sessionextras

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.impl.protobuf.client.SessionExtrasProtobuf
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

internal class SessionExtrasStorageTest : CommonTest() {

    private val dbKey = "session_extras"
    private val valueFromDb = "Value from db".toByteArray()
    private val sessionExtraProto = mock<SessionExtrasProtobuf.SessionExtras>()
    private val sessionExtraDefaultProto = mock<SessionExtrasProtobuf.SessionExtras>()
    private val sessionExtra = mapOf("extra key" to "extra value".toByteArray())
    private val emptyMap = emptyMap<String, ByteArray>()

    private val context = mock<Context>()
    private val apiKey = UUID.randomUUID().toString()
    private val componentId = mock<ComponentId> {
        on { apiKey } doReturn apiKey
    }

    private val binaryDataHelper = mock<IBinaryDataHelper> {
        on { get(dbKey) } doReturn valueFromDb
    }

    private val databaseStorageFactory = mock<DatabaseStorageFactory> {
        on { getBinaryDbHelperForComponent(componentId) } doReturn binaryDataHelper
    }

    @get:Rule
    val converterMockedConstructionRule = MockedConstructionRule(SessionExtrasConverter::class.java) {mock, _ ->
        whenever(mock.toModel(sessionExtraProto)).thenReturn(sessionExtra)
        whenever(mock.fromModel(sessionExtra)).thenReturn(sessionExtraProto)
        whenever(mock.toModel(sessionExtraDefaultProto)).thenReturn(emptyMap)
    }

    @get:Rule
    val serializerMockedConstructionRule = MockedConstructionRule(SessionExtrasSerializer::class.java) {mock, _ ->
        whenever(mock.defaultValue()).thenReturn(sessionExtraDefaultProto)
        whenever(mock.toState(valueFromDb)).thenReturn(sessionExtraProto)
        whenever(mock.toByteArray(sessionExtraProto)).thenReturn(valueFromDb)
    }

    @get:Rule
    val databaseStorageFactoryMockedStaticRule = MockedStaticRule(DatabaseStorageFactory::class.java)

    private lateinit var storage: SessionExtrasStorage
    private lateinit var converter: SessionExtrasConverter
    private lateinit var serializer: SessionExtrasSerializer

    @Before
    fun setUp() {
        whenever(DatabaseStorageFactory.getInstance(context)).thenReturn(databaseStorageFactory)
        storage = SessionExtrasStorage(context, componentId)
        converter = converter()
        serializer = serializer()
    }

    @Test
    fun `extras for valid value`() {
        assertThat(storage.extras).isEqualTo(sessionExtra)
    }

    @Test
    fun `extras for null value`() {
        whenever(binaryDataHelper.get(dbKey)).thenReturn(null)
        assertThat(storage.extras).isEqualTo(emptyMap)
    }

    @Test
    fun `extras for empty value`() {
        whenever(binaryDataHelper.get(dbKey)).thenReturn(ByteArray(0))
        assertThat(storage.extras).isEqualTo(emptyMap)
    }

    @Test
    fun `extras if throw`() {
        whenever(binaryDataHelper.get(dbKey)).thenThrow(RuntimeException())
        assertThat(storage.extras).isEqualTo(emptyMap)
    }

    @Test
    fun set() {
        storage.extras = sessionExtra
        verify(binaryDataHelper).insert(dbKey, valueFromDb)
    }

    private fun converter() : SessionExtrasConverter {
        assertThat(converterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(converterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return converterMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun serializer(): SessionExtrasSerializer {
        assertThat(serializerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(serializerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return serializerMockedConstructionRule.constructionMock.constructed().first()
    }
}
