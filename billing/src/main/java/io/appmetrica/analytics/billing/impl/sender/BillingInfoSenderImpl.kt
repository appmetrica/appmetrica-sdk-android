package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleReporter
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleCounterReport
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Executor

class BillingInfoSenderImpl(
    private val reporter: ServiceComponentModuleReporter,
    private val executor: Executor,
    private val converter: ProductInfoConverter = ProductInfoConverter(),
) : BillingInfoSender {

    private val tag = "[BillingInfoSenderImpl]"

    override fun sendInfo(productInfos: List<ProductInfo>) {
        DebugLogger.info(tag, "sendInfo: $productInfos")
        productInfos.forEach { productInfo ->
            executor.execute {
                reporter.handleReport(
                    ServiceModuleCounterReport.Companion.newBuilder()
                        .withType(Constants.Events.TYPE)
                        .withValueBytes(converter.fromModel(productInfo))
                        .build()
                )
            }
        }
    }
}
