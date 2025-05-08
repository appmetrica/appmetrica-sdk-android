package io.appmetrica.analytics.impl.component.sessionextras

import android.content.Context
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class SessionExtrasHolderTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()
    private val context = mock<Context>()
    private val componentId = mock<ComponentId> {
        on { apiKey } doReturn apiKey
    }
    private val firstKey = "First key"
    private val secondKey = "Second key"
    private val defaultKey = "Default key"
    private val firstValue = ByteArray(4) { it.toByte() }
    private val secondValue = ByteArray(6) { it.toByte() }
    private val defaultValue = "Default value".toByteArray()

    @get:Rule
    val storageMockedConstructionRule = MockedConstructionRule(SessionExtrasStorage::class.java) { mock, _ ->
        whenever(mock.extras).thenReturn(mapOf(defaultKey to defaultValue))
    }

    private lateinit var sessionExtrasHolder: SessionExtrasHolder
    private lateinit var storage: SessionExtrasStorage

    @Before
    fun setUp() {
        sessionExtrasHolder = SessionExtrasHolder(context, componentId)
        storage = storage()
    }

    @Test
    fun `initial state`() {
        assertThat(sessionExtrasHolder.snapshot).containsExactlyEntriesOf(mapOf(defaultKey to defaultValue))
    }

    @Test
    fun `snapshot make copy`() {
        sessionExtrasHolder.put(firstKey, firstValue)
        val snapshot = sessionExtrasHolder.snapshot
        sessionExtrasHolder.put(secondKey, secondValue)

        assertThat(snapshot).containsExactlyEntriesOf(mapOf(defaultKey to defaultValue, firstKey to firstValue))
    }

    @Test
    fun `put write data to storage`() {
        sessionExtrasHolder.put(firstKey, firstValue)
        verify(storage).extras = mapOf(defaultKey to defaultValue, firstKey to firstValue)
    }

    @Test
    fun `put updated value`() {
        sessionExtrasHolder.put(firstKey, firstValue)
        sessionExtrasHolder.put(firstKey, secondValue)

        assertThat(sessionExtrasHolder.snapshot)
            .containsExactlyEntriesOf(mapOf(defaultKey to defaultValue, firstKey to secondValue))
    }

    @Test
    fun `put null value`() {
        sessionExtrasHolder.put(firstKey, firstValue)
        sessionExtrasHolder.put(firstKey, null)
        assertThat(sessionExtrasHolder.snapshot).containsExactlyEntriesOf(mapOf(defaultKey to defaultValue))
    }

    @Test
    fun `put empty value`() {
        sessionExtrasHolder.put(firstKey, firstValue)
        sessionExtrasHolder.put(firstKey, ByteArray(0))
        assertThat(sessionExtrasHolder.snapshot).containsExactlyEntriesOf(mapOf(defaultKey to defaultValue))
    }

    private fun storage(): SessionExtrasStorage {
        assertThat(storageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(storageMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, componentId)
        return storageMockedConstructionRule.constructionMock.constructed().first()
    }
}
