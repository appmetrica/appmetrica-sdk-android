package io.appmetrica.analytics.impl;

public class Limits {

    public static final int EVENT_VALUE_FOR_LOGS_LIMIT = 500;

    /**
     * Fixed per-item overhead counted toward the CRS queue budget: 4 KiB.
     * Covers Callable / Handler message / reporter environment without a precise estimator.
     */
    public static final long CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES = 4L * 1024;
}
