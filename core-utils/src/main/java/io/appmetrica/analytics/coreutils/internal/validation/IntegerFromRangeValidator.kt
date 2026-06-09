package io.appmetrica.analytics.coreutils.internal.validation

class IntegerFromRangeValidator(
    private val description: String,
    private val possibleValues: List<Int>
) : Validator<Int?> {

    override fun validate(data: Int?): ValidationResult = when (data) {
        null -> ValidationResult.failed(this, "${description}is null")

        !in possibleValues -> ValidationResult.failed(
            this,
            "$description(value = $data) not in range of possible values: $possibleValues"
        )

        else -> ValidationResult.successful(this)
    }
}
