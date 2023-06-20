package io.appmetrica.analytics.impl.telephony

import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BaseTelephonyInfoAdapterApplierTest : CommonTest() {

    private val data = mock<Any>()
    private val adapter = mock<TelephonyInfoAdapter<Any>>()
    private val extractor = mock<TelephonyInfoExtractor<Any>> {
        on { extract() } doReturn data
    }

    private lateinit var baseTelephonyInfoAdapterApplier: TelephonyInfoAdapterApplier<Any>

    @Before
    fun setUp() {
        baseTelephonyInfoAdapterApplier = BaseTelephonyInfoAdapterApplier(extractor)
    }

    @Test
    fun applyAdapter() {
        baseTelephonyInfoAdapterApplier.applyAdapter(adapter)
        verify(adapter).adopt(data)
    }
}
