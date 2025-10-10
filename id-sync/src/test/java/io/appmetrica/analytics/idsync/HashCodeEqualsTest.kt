package io.appmetrica.analytics.idsync

import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class HashCodeEqualsTest(private val clazz: Class<Any?>) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(IdSyncConfig::class.java),
            arrayOf(Preconditions::class.java),
            arrayOf(RequestConfig::class.java)
        )
    }

    @Test
    fun hashCodeAndEquals() {
        EqualsVerifier.forClass<Any?>(clazz)
            .usingGetClass()
            .verify()
    }
}
