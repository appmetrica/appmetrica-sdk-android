package io.appmetrica.analytics.billingv4.impl.update

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdatePolicyImplTest {

    @Mock
    private lateinit var billingInfoManager: BillingInfoManager
    @Mock
    private lateinit var systemTimeProvider: SystemTimeProvider

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
    private val billingConfig = BillingConfig(10000, 10000)
    private lateinit var updatePolicy: UpdatePolicyImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(billingInfoManager.get(storedInapp.sku)).thenReturn(storedInapp)
        `when`(billingInfoManager.get(storedSubs.sku)).thenReturn(storedSubs)
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(true)
        `when`(systemTimeProvider.currentTimeMillis()).thenReturn(now)

        updatePolicy = UpdatePolicyImpl(systemTimeProvider)
    }

    @Test
    fun ignoreSkuIfStored() {
        val history = mapOf(
            storedInapp.sku to BillingInfo(
                storedInapp.type,
                storedInapp.sku,
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
            storedInapp.sku to BillingInfo(
                storedInapp.type,
                storedInapp.sku,
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
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
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
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
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
            storedSubs.sku to BillingInfo(
                ProductType.SUBS,
                storedSubs.sku,
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
            storedSubs.sku to BillingInfo(
                storedSubs.type,
                storedSubs.sku,
                storedSubs.purchaseToken,
                now,
                now
            )
        )
        `when`(billingInfoManager.get(storedSubs.sku)).thenReturn(
            BillingInfo(
                ProductType.SUBS,
                storedSubs.sku,
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
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
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
