package io.appmetrica.analytics.coreutils.internal.validation

class NonNullValidator<T>(
    val objectDescription: String
) : Validator<T?> {

    override fun validate(data: T?): ValidationResult =
        if (data == null) {
            ValidationResult.failed(this, "$objectDescription is null.")
        } else {
            ValidationResult.successful(this)
        }
}
