package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class BillingInfoStorageImpl(
    private val storage: ProtobufStateStorage<AutoInappCollectingInfo>
) : BillingInfoStorage {

    private val tag = "[BillingInfoStorageImpl]"

    private var autoInappCollectingInfo: AutoInappCollectingInfo = storage.read().also {
        DebugLogger.info(tag, "Read initial state: $it")
    }

    override fun saveInfo(
        billingInfos: List<BillingInfo>,
        firstInappCheckOccurred: Boolean
    ) {
        DebugLogger.info(tag, "saveInfo: $billingInfos")
        billingInfos.forEach { info ->
            DebugLogger.info(tag, info.toString())
        }
        autoInappCollectingInfo = AutoInappCollectingInfo(billingInfos, firstInappCheckOccurred)
        storage.save(autoInappCollectingInfo)
    }

    override fun getBillingInfo(): List<BillingInfo> {
        return autoInappCollectingInfo.billingInfos.also {
            DebugLogger.info(tag, "getBillingInfo $it")
        }
    }

    override fun isFirstInappCheckOccurred(): Boolean {
        return autoInappCollectingInfo.firstInappCheckOccurred.also {
            DebugLogger.info(tag, "isFirstInappCheckOccurred $it")
        }
    }
}
