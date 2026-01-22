package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleReporter
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleCounterReport
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
internal class BillingInfoSenderImplTest : CommonTest() {

    private val firstProductInfo =
        ProductInfo(
            ProductType.INAPP,
            "sku1",
            2,
            1,
            "by",
            3,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature1",
            "token1",
            10,
            true,
            "{\"productId\":\"sku1\", \"autoRenewing\":\"true\"}"
        )
    private val firstValueBytes = "first value bytes".toByteArray()

    private val secondProductInfo =
        ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "by",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            11,
            false,
            ""
        )
    private val secondValueBytes = "second value bytes".toByteArray()

    private val productInfos = listOf(
        firstProductInfo,
        secondProductInfo
    )

    private val reporter: ServiceComponentModuleReporter = mock()
    private val executor: Executor = mock {
        on { execute(any()) } doAnswer { it.getArgument<Runnable>(0).run() }
    }
    private val converter: ProductInfoConverter = mock {
        on { fromModel(firstProductInfo) } doAnswer { firstValueBytes }
        on { fromModel(secondProductInfo) } doAnswer { secondValueBytes }
    }

    private val billingInfoSender = BillingInfoSenderImpl(
        reporter,
        executor,
        converter
    )

    @Test
    fun sendInfo() {
        billingInfoSender.sendInfo(productInfos)

        verify(reporter).handleReport(
            ServiceModuleCounterReport.Companion.newBuilder()
                .withType(Constants.Events.TYPE)
                .withValueBytes(firstValueBytes)
                .build()
        )
        verify(reporter).handleReport(
            ServiceModuleCounterReport.Companion.newBuilder()
                .withType(Constants.Events.TYPE)
                .withValueBytes(secondValueBytes)
                .build()
        )
        verifyNoMoreInteractions(reporter)
    }
}
