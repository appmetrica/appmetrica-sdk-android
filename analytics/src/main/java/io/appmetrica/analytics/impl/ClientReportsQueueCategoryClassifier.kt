package io.appmetrica.analytics.impl

import android.os.Bundle

/**
 * Classifies CRS enqueue items.
 *
 * Entry points decide [ClientReportsQueueCategory.FATAL] (`sendCrash`) and session
 * CONTROL (`queueResumeUserSession` / `queuePauseUserSession`).
 * [ReportsSender.queueReport] uses [ofEventType] on InternalEvents typeId.
 * Typed [ReportsSender.reportData] uses [ofTypedReport]: InternalEvents allowlist,
 * then transport/action contract for modules (e.g. RTM UPDATE_CONFIG).
 * FATAL is never returned from a type id.
 */
internal object ClientReportsQueueCategoryClassifier {

    /**
     * Transport reporter id for RTM (`RtmServiceDataReporter.TYPE_RTM` /
     * `Constants.CODE_RTM`). Kept here to avoid analytics → rtm dependency.
     */
    private const val TRANSPORT_TYPE_RTM = 2

    /**
     * Must stay in sync with `io.appmetrica.analytics.rtm.internal.Constants.ACTION_UPDATE_RTM_CONFIG`.
     */
    private const val RTM_ACTION_UPDATE_CONFIG =
        "io.appmetrica.analytics.rtm.UPDATE_RTM_CONFIGURATION"

    private const val RTM_BUNDLE_KEY_ACTION = "action"

    private val CONTROL_TYPED_REPORT_TYPES: Set<Int> = setOf(
        InternalEvents.EVENT_TYPE_ACTIVATION.typeId,
        InternalEvents.EVENT_TYPE_STARTUP.typeId,
        InternalEvents.EVENT_TYPE_REQUEST_REFERRER.typeId,
        InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId,
        InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED.typeId,
        InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED.typeId,
        InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA.typeId,
        InternalEvents.EVENT_TYPE_START.typeId,
        InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME.typeId,
        InternalEvents.EVENT_TYPE_PURGE_BUFFER.typeId,
        InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.typeId,
        InternalEvents.EVENT_TYPE_WEBVIEW_SYNC.typeId,
    )

    fun ofEventType(type: Int): ClientReportsQueueCategory =
        if (type in CONTROL_TYPED_REPORT_TYPES) {
            ClientReportsQueueCategory.CONTROL
        } else {
            ClientReportsQueueCategory.DATA
        }

    /**
     * Workaround: [reportData] reuses one `type` int for both ServiceDataReporter ids
     * (e.g. RTM=2) and InternalEvents typeIds, so CONTROL for RTM UPDATE_CONFIG cannot be
     * expressed by type alone and is detected via [bundle] action.
     *
     * @param type transport reporter id or InternalEvents typeId (tests / legacy callers)
     * @param bundle typed payload; used for transport-specific CONTROL actions (RTM config)
     */
    fun ofTypedReport(type: Int, bundle: Bundle): ClientReportsQueueCategory {
        if (type in CONTROL_TYPED_REPORT_TYPES) {
            return ClientReportsQueueCategory.CONTROL
        }
        if (type == TRANSPORT_TYPE_RTM &&
            bundle.getString(RTM_BUNDLE_KEY_ACTION) == RTM_ACTION_UPDATE_CONFIG
        ) {
            return ClientReportsQueueCategory.CONTROL
        }
        return ClientReportsQueueCategory.DATA
    }
}
