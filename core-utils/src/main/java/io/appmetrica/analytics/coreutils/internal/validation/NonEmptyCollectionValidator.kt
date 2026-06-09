package io.appmetrica.analytics.coreutils.internal.validation

class NonEmptyCollectionValidator<T>(
    private val objectDescription: String
) : Validator<Collection<T>?> {

    override fun validate(data: Collection<T>?): ValidationResult =
        if (data.isNullOrEmpty()) {
            ValidationResult.failed(this, "$objectDescription is null or empty.")
        } else {
            ValidationResult.successful(this)
        }
}
