package io.appmetrica.analytics.coreutils.internal.proto

import java.math.BigDecimal
import java.math.BigInteger

data class DecimalProtoModel(
    val mantissa: Long,
    val exponent: Int,
) {
    companion object {

        private val MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE)
        private val MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE)

        @JvmStatic
        fun fromDecimal(value: BigDecimal): DecimalProtoModel {
            val pair = convert(value)
            return DecimalProtoModel(pair.first, pair.second)
        }

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
    }
}
