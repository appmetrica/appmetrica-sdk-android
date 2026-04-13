package io.appmetrica.analytics.impl.telephony

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

internal class SimInfoTest : CommonTest() {

    @Test
    fun filledFields() {
        val simCountryCode = 232
        val simNetworkCode = 775
        val roaming = true
        val operatorName = "Operator name"

        ObjectPropertyAssertions(SimInfo(simCountryCode, simNetworkCode, roaming, operatorName))
            .checkField("simCountryCode", "getSimCountryCode", simCountryCode)
            .checkField("simNetworkCode", "getSimNetworkCode", simNetworkCode)
            .checkField("isNetworkRoaming", "isNetworkRoaming", roaming)
            .checkField("operatorName", "getOperatorName", operatorName)
            .checkAll()
    }
}
