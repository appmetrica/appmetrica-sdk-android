package io.appmetrica.analytics.impl

/**
 * CRS enqueue category.
 *
 * - [DATA] — subject to the client reports queue byte budget.
 * - [CONTROL] — session/lifecycle typed commands; never counted toward the DATA budget.
 * - [FATAL] — process-fatal crash path ([ReportsSender.sendCrash]); never counted toward the DATA budget.
 */
internal enum class ClientReportsQueueCategory {
    DATA,
    CONTROL,
    FATAL,
}
