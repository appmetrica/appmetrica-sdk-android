package io.appmetrica.analytics.coreutils.internal.validation

class DummyValidator<T> : Validator<T?> {

    override fun validate(data: T?): ValidationResult = ValidationResult.successful(this)
}
