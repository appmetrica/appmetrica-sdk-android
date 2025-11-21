package io.appmetrica.analytics.impl.profile.fpd

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelegramLoginNormalizerTest : CommonTest() {

    private val normalizer = TelegramLoginNormalizer()

    @Test
    fun normalize() {
        assertThat(normalizer.normalize("login")).isEqualTo("login")
    }
}
