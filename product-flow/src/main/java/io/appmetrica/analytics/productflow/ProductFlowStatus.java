package io.appmetrica.analytics.productflow;

/**
 * Final outcome of a product flow.
 */
public enum ProductFlowStatus {
    /** The product flow completed successfully. */
    SUCCESS,
    /** The product flow was declined by the system or counterparty. */
    DECLINED,
    /** The product flow is awaiting a decision. */
    PENDING,
    /** The product flow was cancelled by the user. */
    CANCELLED,
    /** The product flow expired before completion. */
    EXPIRED,
    /** The product flow failed due to a technical error. */
    FAIL
}
