package io.appmetrica.analytics.coreutils.internal.validation

class NonBlankStringValidator(
    private val objectDescription: String
) : Validator<String?> {

    override fun validate(data: String?): ValidationResult =
        if (data.isNullOrBlank()) {
            ValidationResult.failed(this, "$objectDescription is null, empty or blank.")
        } else {
            ValidationResult.successful(this)
        }
}
