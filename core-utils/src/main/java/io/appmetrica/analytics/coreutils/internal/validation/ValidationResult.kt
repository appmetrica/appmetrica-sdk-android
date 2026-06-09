package io.appmetrica.analytics.coreutils.internal.validation

class ValidationResult private constructor(
    validator: Validator<*>,
    val isValid: Boolean,
    val description: String
) {
    val validatorClass = validator.javaClass

    companion object {

        @JvmStatic
        fun successful(validator: Validator<*>): ValidationResult {
            return ValidationResult(validator, true, "")
        }

        @JvmStatic
        fun failed(validator: Validator<*>, description: String): ValidationResult {
            return ValidationResult(validator, false, description)
        }
    }
}
