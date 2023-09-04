package io.appmetrica.analytics

import io.appmetrica.analytics.AdvIdentifiersResult.AdvId
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdvIdentifiersResultTest : CommonTest() {

    @Test
    fun constructor() {
        val gaid = mock<AdvId>()
        val hoaid = mock<AdvId>()
        val yandex = mock<AdvId>()
        val result = AdvIdentifiersResult(gaid, hoaid, yandex)
        ObjectPropertyAssertions(result)
            .checkField("googleAdvId", gaid)
            .checkField("huaweiAdvId", hoaid)
            .checkField("yandexAdvId", yandex)
            .checkAll()
    }
}
