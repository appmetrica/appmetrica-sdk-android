package io.appmetrica.analytics.billingv6.impl.storage

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BillingInfoManagerImplTest : CommonTest() {

    private val billingInfoStorage: BillingInfoStorage = mock()

    private lateinit var billingInfoManager: BillingInfoManagerImpl

    @Test
    fun testUpdate() {
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        val history = mapOf(
            "sku1" to BillingInfo(
                ProductType.INAPP,
                "sku1",
                "token1",
                1,
                2
            ),
            "sku2" to BillingInfo(
                ProductType.INAPP,
                "sku2",
                "token2",
                3,
                4
            )
        )
        billingInfoManager.update(history)
        verify(billingInfoStorage).saveInfo(eq(history.values.toList()), eq(false))
    }

    @Test
    fun updateIfFirstInappCheckOccurred() {
        whenever(billingInfoStorage.isFirstInappCheckOccurred).thenReturn(true)
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        val history = mapOf(
            "sku1" to BillingInfo(
                ProductType.INAPP,
                "sku1",
                "token1",
                1,
                2
            ),
            "sku2" to BillingInfo(
                ProductType.INAPP,
                "sku2",
                "token2",
                3,
                4
            )
        )
        billingInfoManager.update(history)
        verify(billingInfoStorage).saveInfo(eq(history.values.toList()), eq(true))
    }

    @Test
    fun markFirstInappCheckOccurred() {
        whenever(billingInfoStorage.isFirstInappCheckOccurred).thenReturn(false)
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        assertThat(billingInfoManager.isFirstInappCheckOccurred).isFalse
        billingInfoManager.markFirstInappCheckOccurred()
        assertThat(billingInfoManager.isFirstInappCheckOccurred).isTrue
        verify(billingInfoStorage).saveInfo(anyList(), eq(true))
    }

    @Test
    fun markFirstInappCheckOccurredAfterUpdate() {
        whenever(billingInfoStorage.isFirstInappCheckOccurred).thenReturn(false)
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        val history = mapOf(
            "sku1" to BillingInfo(
                ProductType.INAPP,
                "sku1",
                "token1",
                1,
                2
            ),
            "sku2" to BillingInfo(
                ProductType.INAPP,
                "sku2",
                "token2",
                3,
                4
            )
        )
        billingInfoManager.update(history)
        billingInfoManager.markFirstInappCheckOccurred()
        assertThat(billingInfoManager.isFirstInappCheckOccurred).isTrue
        verify(billingInfoStorage).saveInfo(eq(history.values.toList()), eq(true))
    }

    @Test
    fun get() {
        val billingInfos = listOf(
            BillingInfo(
                ProductType.INAPP,
                "sku",
                "token1",
                1,
                2
            )
        )
        whenever(billingInfoStorage.billingInfo).thenReturn(billingInfos)
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        assertThat(billingInfoManager.get("wrong_sku")).isNull()
        assertThat(billingInfoManager.get("sku")).isEqualTo(billingInfos[0])
    }

    @Test
    fun getAfterUpdate() {
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        val history = mapOf(
            "sku1" to BillingInfo(
                ProductType.INAPP,
                "sku1",
                "token1",
                1,
                2
            )
        )
        assertThat(billingInfoManager.get("sku1")).isNull()
        billingInfoManager.update(history)
        assertThat(billingInfoManager.get("sku1")).isEqualTo(history["sku1"])
    }
}
