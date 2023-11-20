package io.appmetrica.analytics.billingv6.impl.update

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdatePolicyImplTest : CommonTest() {

    private val now = SystemTimeProvider().currentTimeMillis()
    private val storedInapp = BillingInfo(
        ProductType.INAPP,
        "stored_inapp",
        "stored_inapp_token",
        1,
        2
    )
    private val storedSubs = BillingInfo(
        ProductType.SUBS,
        "stored_subs",
        "stored_subs_token",
        1,
        2
    )

    private val billingInfoManager: BillingInfoManager = mock {
        on { get(storedInapp.productId) } doReturn storedInapp
        on { get(storedSubs.productId) } doReturn storedSubs
        on { isFirstInappCheckOccurred } doReturn true
    }
    private val systemTimeProvider: SystemTimeProvider = mock {
        on { currentTimeMillis() } doReturn now
    }

    private val billingConfig = BillingConfig(10000, 10000)
    private val updatePolicy = UpdatePolicyImpl(systemTimeProvider)

    @Test
    fun ignoreSkuIfStored() {
        val history = mapOf(
            storedInapp.productId to BillingInfo(
                storedInapp.type,
                storedInapp.productId,
                storedInapp.purchaseToken,
                3,
                4
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEmpty()
    }

    @Test
    fun updateSkuIfStoredWithOtherToken() {
        val history = mapOf(
            storedInapp.productId to BillingInfo(
                storedInapp.type,
                storedInapp.productId,
                "other_token",
                3,
                4
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEqualTo(history)
    }

    @Test
    fun updateSkuIfNotStored() {
        val history = mapOf(
            "sku" to BillingInfo(
                ProductType.INAPP,
                "sku",
                "token",
                3,
                4
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEqualTo(history)
    }

    @Test
    fun ignoreOldSku() {
        whenever(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        val history = mapOf(
            "sku" to BillingInfo(
                ProductType.INAPP,
                "sku",
                "token",
                3,
                4
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEmpty()
    }

    @Test
    fun updateSkuOnFirstWrite() {
        whenever(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        val history = mapOf(
            "sku" to BillingInfo(
                ProductType.INAPP,
                "sku",
                "token",
                now,
                4
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEqualTo(history)
    }

    @Test
    fun updateOldSubs() {
        val history = mapOf(
            storedSubs.productId to BillingInfo(
                ProductType.SUBS,
                storedSubs.productId,
                "token",
                3,
                4
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEqualTo(history)
    }

    @Test
    fun ignoreNewSubsIfStored() {
        val history = mapOf(
            storedSubs.productId to BillingInfo(
                storedSubs.type,
                storedSubs.productId,
                storedSubs.purchaseToken,
                now,
                now
            )
        )
        whenever(billingInfoManager.get(storedSubs.productId)).thenReturn(
            BillingInfo(
                ProductType.SUBS,
                storedSubs.productId,
                storedSubs.purchaseToken,
                storedSubs.purchaseTime,
                now
            )
        )
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEmpty()
    }

    @Test
    fun ignorePartOfSkus() {
        whenever(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        val goodHistory = mapOf(
            "sku1" to BillingInfo(
                ProductType.INAPP,
                "sku1",
                "token1",
                now,
                4
            ),
            "sku2" to BillingInfo(
                ProductType.INAPP,
                "sku2",
                "token2",
                now,
                4
            )
        )
        val badHistory = mapOf(
            "sku3" to BillingInfo(
                ProductType.INAPP,
                "sku3",
                "token3",
                3,
                4
            ),
            "sku4" to BillingInfo(
                ProductType.INAPP,
                "sku4",
                "token4",
                3,
                4
            )
        )
        val history = goodHistory + badHistory
        val result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager)
        assertThat(result).isEqualTo(goodHistory)
    }
}
