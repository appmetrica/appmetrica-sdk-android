package io.appmetrica.analytics.identifiers.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdvIdInfoTest : CommonTest() {

    @Test
    fun fullInfo() {
        val provider = "IUUIJI"
        val id = "dsofhiuwfewieifjidfhi"
        val limited = false

        val data = AdvIdInfo(provider, id, limited).toBundle()

        val soft = SoftAssertions()

        soft.assertThat(data.getString(Constants.PROVIDER)).`as`("provider").isEqualTo(provider)
        soft.assertThat(data.getString(Constants.ID)).`as`("id").isEqualTo(id)
        soft.assertThat(data.getBoolean(Constants.LIMITED)).`as`("limited").isEqualTo(limited)
        soft.assertThat(data.size()).`as`("size").isEqualTo(3)

        soft.assertAll()
    }

    @Test
    fun emptyInfo() {
        val provider = "IUUIJI"

        val data = AdvIdInfo(provider).toBundle()

        val soft = SoftAssertions()

        soft.assertThat(data.getString(Constants.PROVIDER)).`as`("provider").isEqualTo(provider)
        soft.assertThat(data.getString(Constants.ID)).`as`("id").isNull()
        soft.assertThat(data.containsKey(Constants.LIMITED)).`as`("limited").isFalse
        soft.assertThat(data.size()).`as`("size").isEqualTo(2)

        soft.assertAll()
    }
}
