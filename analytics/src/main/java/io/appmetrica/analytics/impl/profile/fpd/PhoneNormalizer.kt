package io.appmetrica.analytics.impl.profile.fpd

// see https://nda.ya.ru/t/0rX8ReVs7MpdUK
internal class PhoneNormalizer : AttributeValueNormalizer {

    private val phoneMinValidDigitCount = 10
    private val phoneMaxValidDigitCount = 13
    private val allowedCharactersRegex = "^[0-9()\\-+\\s]+$".toRegex()

    override fun normalize(value: String): String? {
        val digits = value.filter { it.isDigit() }
        val digitsCount = digits.length
        val firstLetter = value.firstOrNull()
        val firstDigit = digits.firstOrNull()

        if (
            digitsCount < phoneMinValidDigitCount ||
            digitsCount > phoneMaxValidDigitCount ||
            firstDigit == '0' ||
            !allowedCharactersRegex.matches(value)
        ) {
            return null
        }

        if (digitsCount == 10 && firstLetter != '+') {
            return "7$digits"
        }

        if (digitsCount == 11) {
            if (firstLetter == '+' && firstDigit == '8') {
                return null
            }

            if (firstDigit == '8') {
                return "7${digits.substring(1)}"
            }
        }

        if (digitsCount >= 12 && firstLetter == '+' && firstDigit == '7') {
            return null
        }
        return digits
    }
}
