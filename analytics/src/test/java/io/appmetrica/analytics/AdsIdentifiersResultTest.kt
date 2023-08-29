package io.appmetrica.analytics

import io.appmetrica.analytics.AdsIdentifiersResult.AdvId
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdsIdentifiersResultTest : CommonTest() {

    @Test
    fun constructor() {
        val gaid = mock<AdvId>()
        val hoaid = mock<AdvId>()
        val yandex = mock<AdvId>()
        val result = AdsIdentifiersResult(gaid, hoaid, yandex)
        ObjectPropertyAssertions(result)
            .checkField("googleAdvId", gaid)
            .checkField("huaweiAdvId", hoaid)
            .checkField("yandexAdvId", yandex)
            .checkAll()
    }
}
