package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClidsDataSaverTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()
    private val candidateCaptor = argumentCaptor<ClidsInfo.Candidate>()
    private val dataSaver = ClidsDataSaver()

    @Test
    fun saveClids() {
        val clids = mapOf("clid0" to "0")
        dataSaver(clids)
        val clidsStorage = GlobalServiceLocator.getInstance().clidsStorage
        verify(clidsStorage).updateIfNeeded(candidateCaptor.capture())
        ObjectPropertyAssertions(candidateCaptor.firstValue)
            .checkField("clids", clids)
            .checkField("source", DistributionSource.RETAIL)
            .checkAll()
    }
}
