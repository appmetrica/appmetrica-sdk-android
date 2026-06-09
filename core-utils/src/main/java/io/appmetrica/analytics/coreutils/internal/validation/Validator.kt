package io.appmetrica.analytics.coreutils.internal.validation

interface Validator<T> {

    fun validate(data: T): ValidationResult
}
