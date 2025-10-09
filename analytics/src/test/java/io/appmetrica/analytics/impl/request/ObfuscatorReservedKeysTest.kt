package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ObfuscatorReservedKeysTest(
    private val obfuscatedKey: String,
    private val description: String
) : CommonTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Reserved key \"{0}\" - {1}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf("a", ""),
                arrayOf("nc", ""),
                arrayOf("sm", ""),
                arrayOf("om", ""),
                arrayOf("ap", ""),
                arrayOf("wu", ""),
                arrayOf("s", "METRIKALIB-8173"),
                arrayOf("d", "METRIKALIB-8322"),
                arrayOf("mak", "METRIKALIB-8377"),
                arrayOf("up", "METRIKALIB-8381"),
                arrayOf("ucfb", "METRIKALIB-8381"),
                arrayOf("ues", "METRIKALIB-8381"),
                arrayOf("ures", "METRIKALIB-8381"),
                arrayOf("lc", "METRIKALIB-8372"),
                arrayOf("lbs", "METRIKALIB-8372"),
                arrayOf("wa", "METRIKALIB-8372"),
                arrayOf("wc", "METRIKALIB-8372"),
                arrayOf("ca", "METRIKALIB-8372"),
                arrayOf("cai", "METRIKALIB-8372"),
                arrayOf("caico", "METRIKALIB-8372"),
                arrayOf("gplc", "METRIKALIB-8372"),
                arrayOf("tht", "METRIKALIB-8372"),
                arrayOf("eg", "METRIKALIB-8596"),
                arrayOf("sl", "METRIKALIB-8634"),
                arrayOf("ilc", "METRIKALIB-8470"),
                arrayOf("ec", "METRIKALIB-8791"),
                arrayOf("pi", "METRIKALIB-8791"),
                arrayOf("aic", "METRIKALIB-10393"),
            )
        }
    }

    @Test
    fun reservedFeature() {
        assertThat(Obfuscator().obfuscationKeys).doesNotContain(obfuscatedKey)
    }
}
