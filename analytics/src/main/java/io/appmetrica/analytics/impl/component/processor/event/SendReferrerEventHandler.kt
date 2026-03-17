package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class SendReferrerEventHandler(component: ComponentUnit) : ReportComponentHandler(component) {
    private val tag = "[SendReferrerEventHandler]"

    override fun process(reportData: CounterReport): Boolean {
        if (!isAlreadySend()) {
            DebugLogger.info(tag, "Request referrer for send event")
            GlobalServiceLocator.getInstance().getReferrerManager()
                .requestReferrer(ReferrerEventSender())
        }
        return false
    }

    private fun isAlreadySend(): Boolean {
        return component.vitalComponentDataProvider.referrerHandled
    }

    private fun onReferrerHandled() {
        component.vitalComponentDataProvider.referrerHandled = true
    }

    private inner class ReferrerEventSender : ReferrerListener {
        private val tag = "[SendReferrerEventHandler.ReferrerEventSender]"

        override fun onResult(result: ReferrerResult) {
            DebugLogger.info(tag, "Received referrer result: %s", result)

            val referrer = result.referrerInfo
            if (referrer == null) {
                DebugLogger.info(tag, "Skip sending referrer. The referrer was not found")
                return
            }

            if (isAlreadySend()) {
                DebugLogger.info(tag, "Skip sending referrer. It has already been sent")
                return
            }

            try {
                sendReferrer(referrer)
                DebugLogger.info(tag, "Successful referrer sending")

                // we mark that the referrer is handled only after successful sending.
                onReferrerHandled()
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, "Failed to send referrer")
            }
        }

        fun sendReferrer(referrer: ReferrerInfo) {
            val referrerReport = CounterReport()
            referrerReport.valueBytes = referrer.toProto()
            referrerReport.type = InternalEvents.EVENT_TYPE_SEND_REFERRER.typeId
            component.handleReport(referrerReport)
        }
    }
}
