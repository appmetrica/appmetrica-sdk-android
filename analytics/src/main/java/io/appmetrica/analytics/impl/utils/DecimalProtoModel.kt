package io.appmetrica.analytics.impl.utils

import java.math.BigDecimal
import java.math.BigInteger

private val MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE)
private val MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE)

private fun convert(value: BigDecimal): Pair<Long, Int> {
    var exponent: Int = -value.scale()
    var bigIntMantissa: BigInteger = value.unscaledValue()
    while (bigIntMantissa > MAX_LONG || bigIntMantissa < MIN_LONG) {
        bigIntMantissa = bigIntMantissa.divide(BigInteger.TEN)
        exponent++
    }
    val mantissa = bigIntMantissa.toLong()
    return mantissa to exponent
}

internal data class DecimalProtoModel(
    val mantissa: Long,
    val exponent: Int,
) {
    companion object {
        @JvmStatic
        fun fromDecimal(value: BigDecimal): DecimalProtoModel {
            val pair = convert(value)
            return DecimalProtoModel(pair.first, pair.second)
        }
    }
}
