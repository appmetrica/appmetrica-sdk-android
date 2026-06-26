package io.appmetrica.analytics.productflow.impl.validation

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.productflow.IllegalProductFlowParametersException
import io.appmetrica.analytics.productflow.ProductFlowStatus

internal object Validation {

    @JvmStatic
    fun validateProductFlowStatus(status: ProductFlowStatus?) {
        if (status == null) {
            throw IllegalProductFlowParametersException("ProductFlowStatus must be NonNull")
        }
    }

    @JvmStatic
    fun validateProductId(productId: String?) {
        if (StringUtils.isNullOrEmpty(productId)) {
            throw IllegalProductFlowParametersException("ProductId must be NonNull and not empty")
        }
    }

    @JvmStatic
    fun validateProductOfferId(productOfferId: String?) {
        if (StringUtils.isNullOrEmpty(productOfferId)) {
            throw IllegalProductFlowParametersException("ProductOfferId must be NonNull and not empty")
        }
    }

    @JvmStatic
    fun validateProductIdAndProductOfferId(productId: String?, productOfferId: String?) {
        if (StringUtils.isNullOrEmpty(productId) && StringUtils.isNullOrEmpty(productOfferId)) {
            throw IllegalProductFlowParametersException(
                "One of ProductId and ProductOfferId must be NonNull and not empty"
            )
        }
    }

    @JvmStatic
    fun validateStepType(stepType: String?) {
        if (StringUtils.isNullOrEmpty(stepType)) {
            throw IllegalProductFlowParametersException("StepType must be NonNull and not empty")
        }
    }

    @JvmStatic
    fun validateOfferType(offerType: String?) {
        if (StringUtils.isNullOrEmpty(offerType)) {
            throw IllegalProductFlowParametersException("OfferType must be NonNull and not empty")
        }
    }
}
