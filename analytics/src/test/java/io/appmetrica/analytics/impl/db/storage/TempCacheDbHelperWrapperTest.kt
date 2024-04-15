package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TempCacheDbHelperWrapperTest : CommonTest() {

    private val storage: TempCacheStorage = mock()
    private val context: Context = mock()
    private val storageType = StorageType.SERVICE

    private val scope = "scope"
    private val timestamp = 100500L
    private val data = ByteArray(14) {it.toByte()}
    private val id = 13L
    private val limit = 100
    private val interval = 240L
    private val entry: TempCacheStorage.Entry = mock()

    @get:Rule
    val serviceMigrationCheckedRule = ServiceMigrationCheckedRule(true)

    private val wrapper by setUp { TempCacheDbHelperWrapper(context, storageType, storage) }

    @Test
    fun put() {
        wrapper.put(scope, timestamp, data)
        verify(storage).put(scope, timestamp, data)
    }

    @Test
    fun `get by scope only`() {
        whenever(storage.get(scope)).thenReturn(entry)
        assertThat(wrapper.get(scope)).isEqualTo(entry)
    }

    @Test
    fun `get by scope and limit`() {
        whenever(storage.get(scope, limit)).thenReturn(listOf(entry))
        assertThat(wrapper.get(scope, limit)).isEqualTo(listOf(entry))
    }

    @Test
    fun remove() {
        wrapper.remove(id)
        verify(storage).remove(id)
    }

    @Test
    fun removeOlderThan() {
        wrapper.removeOlderThan(scope, interval)
        verify(storage).removeOlderThan(scope, interval)
    }
}
