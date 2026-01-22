package io.appmetrica.analytics.impl.telephony

import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions

internal class DummyTelephonyInfoAdapterApplierTest : CommonTest() {

    private val adapter = mock<TelephonyInfoAdapter<Any>>()

    private lateinit var dummyTelephonyInfoAdapterApplier: DummyTelephonyInfoAdapterApplier<Any>

    @Before
    fun setUp() {
        dummyTelephonyInfoAdapterApplier = DummyTelephonyInfoAdapterApplier()
    }

    @Test
    fun applyAdapter() {
        dummyTelephonyInfoAdapterApplier.applyAdapter(adapter)
        verifyNoMoreInteractions(adapter)
    }
}
