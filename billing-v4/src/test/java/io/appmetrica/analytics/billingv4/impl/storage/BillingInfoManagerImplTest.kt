package io.appmetrica.analytics.billingv4.impl.storage

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BillingInfoManagerImplTest {

    @Mock
    private lateinit var billingInfoStorage: BillingInfoStorage

    private lateinit var billingInfoManager: BillingInfoManagerImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

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
    fun testUpdateIfFirstInappCheckOccured() {
        `when`(billingInfoStorage.isFirstInappCheckOccurred).thenReturn(true)
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
    fun testMarkFirstInappCheckOccured() {
        `when`(billingInfoStorage.isFirstInappCheckOccurred).thenReturn(false)
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        assertThat(billingInfoManager.isFirstInappCheckOccurred).isFalse
        billingInfoManager.markFirstInappCheckOccurred()
        assertThat(billingInfoManager.isFirstInappCheckOccurred).isTrue
        verify(billingInfoStorage).saveInfo(anyList(), eq(true))
    }

    @Test
    fun testMarkFirstInappCheckOccuredAfterUpdate() {
        `when`(billingInfoStorage.isFirstInappCheckOccurred).thenReturn(false)
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
    fun testGet() {
        val billingInfos = listOf(
            BillingInfo(
                ProductType.INAPP,
                "sku",
                "token1",
                1,
                2
            )
        )
        `when`(billingInfoStorage.billingInfo).thenReturn(billingInfos)
        billingInfoManager = BillingInfoManagerImpl(billingInfoStorage)
        assertThat(billingInfoManager.get("wrong_sku")).isNull()
        assertThat(billingInfoManager.get("sku")).isEqualTo(billingInfos[0])
    }

    @Test
    fun testGetAfterUpdate() {
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
