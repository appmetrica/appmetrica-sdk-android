package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class BillingInfoStorageImplTest(
    private val billingInfos: List<BillingInfo>,
    private val firstInappCheckOccurred: Boolean
) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}")
        @JvmStatic
        fun data(): List<Array<Any>> {
            val emptyList = emptyList<BillingInfo>()
            val filledList = listOf(
                BillingInfo(ProductType.INAPP, "sku", "purchaseToken", 0, 0)
            )
            return listOf(
                arrayOf(emptyList, true),
                arrayOf(emptyList, false),
                arrayOf(filledList, true),
                arrayOf(filledList, false)
            )
        }
    }

    private val autoInappCollectingInfo = AutoInappCollectingInfo(billingInfos, firstInappCheckOccurred)
    private val storage: ProtobufStateStorage<AutoInappCollectingInfo> = mock {
        on { read() }.thenReturn(autoInappCollectingInfo)
    }
    private var billingInfoStorage = BillingInfoStorageImpl(storage)

    @Test
    fun saveInfo() {
        val argument = argumentCaptor<AutoInappCollectingInfo>()

        billingInfoStorage.saveInfo(billingInfos, firstInappCheckOccurred)

        verify(storage).save(argument.capture())
        assertThat(argument.firstValue.billingInfos).isEqualTo(billingInfos)
        assertThat(argument.firstValue.firstInappCheckOccurred).isEqualTo(firstInappCheckOccurred)
    }

    @Test
    fun loadInfo() {
        val infos = billingInfoStorage.getBillingInfo()

        verify(storage).read()
        assertThat(infos).isEqualTo(billingInfos)
    }

    @Test
    fun isFirstInappCheckOccurred() {
        assertThat(billingInfoStorage.isFirstInappCheckOccurred).isEqualTo(firstInappCheckOccurred)
    }
}
