package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule
import io.appmetrica.gradle.androidtestutils.rules.ContextRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BinaryDataHelperWrapperTest : CommonTest() {

    private val actualHelper: IBinaryDataHelper = mock()
    private val key = "some key"
    private val value = "some value".toByteArray()

    @get:Rule
    val powerMockRule: ServiceMigrationCheckedRule = ServiceMigrationCheckedRule(true)

    @get:Rule
    val contextRule: ContextRule = ContextRule()

    private val binaryDataHelperWrapper by setUp {
        BinaryDataHelperWrapper(contextRule.context, StorageType.SERVICE, actualHelper)
    }

    @Test
    fun insert() {
        binaryDataHelperWrapper.insert(key, value)
        verify(actualHelper).insert(key, value)
    }

    @Test
    fun get() {
        whenever(actualHelper.get(key)).thenReturn(value)
        assertThat(binaryDataHelperWrapper.get(key)).isEqualTo(value)
    }

    @Test
    fun remove() {
        binaryDataHelperWrapper.remove(key)
        verify(actualHelper).remove(key)
    }
}
