package io.appmetrica.analytics.coreutils.internal.validation

class ValidationResultsChain : Validator<@JvmSuppressWildcards List<ValidationResult>> {

    override fun validate(data: List<ValidationResult>): ValidationResult {
        val errors = data.filter { !it.isValid }.map { it.description }
        return if (errors.isEmpty()) {
            ValidationResult.successful(this)
        } else {
            ValidationResult.failed(this, errors.joinToString(", "))
        }
    }
}
