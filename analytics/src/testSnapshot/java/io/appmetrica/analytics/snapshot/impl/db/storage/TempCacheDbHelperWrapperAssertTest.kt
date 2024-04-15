package io.appmetrica.analytics.snapshot.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.storage.TempCacheDbHelperWrapper
import io.appmetrica.analytics.impl.utils.DebugAssert
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class TempCacheDbHelperWrapperAssertTest : CommonTest() {

    @get:Rule
    val debugAssertMockedStaticRule = staticRule<DebugAssert>()

    private val storage: TempCacheStorage = mock()
    private val context: Context = mock()

    private val wrapper by setUp { TempCacheDbHelperWrapper(context, StorageType.SERVICE, storage) }

    @Test
    fun put() {
        wrapper.put("scope", 100500L, ByteArray(0))
        debugAssertMockedStaticRule.staticMock.verify {
            DebugAssert.assertMigrated(context, StorageType.SERVICE)
        }
    }

    @Test
    fun get() {
        wrapper.get("scope")
        debugAssertMockedStaticRule.staticMock.verify {
            DebugAssert.assertMigrated(context, StorageType.SERVICE)
        }
    }

    @Test
    fun `get with limit`() {
        wrapper.get("scope", 100)
        debugAssertMockedStaticRule.staticMock.verify {
            DebugAssert.assertMigrated(context, StorageType.SERVICE)
        }
    }

    @Test
    fun remove() {
        wrapper.remove(100L)
        debugAssertMockedStaticRule.staticMock.verify {
            DebugAssert.assertMigrated(context, StorageType.SERVICE)
        }
    }

    @Test
    fun removeOlderThan() {
        wrapper.removeOlderThan("scope", 200L)
        debugAssertMockedStaticRule.staticMock.verify {
            DebugAssert.assertMigrated(context, StorageType.SERVICE)
        }
    }
}
