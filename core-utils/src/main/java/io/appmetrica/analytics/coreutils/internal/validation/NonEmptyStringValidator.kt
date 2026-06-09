package io.appmetrica.analytics.coreutils.internal.validation

class NonEmptyStringValidator(
    private val objectDescription: String
) : Validator<String?> {

    override fun validate(data: String?): ValidationResult =
        if (data.isNullOrEmpty()) {
            ValidationResult.failed(this, "$objectDescription is empty.")
        } else {
            ValidationResult.successful(this)
        }
}
