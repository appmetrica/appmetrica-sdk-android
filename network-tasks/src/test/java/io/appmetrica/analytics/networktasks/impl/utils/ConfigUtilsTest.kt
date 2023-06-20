package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID

class ConfigUtilsTest {

    private val uuid = UUID.randomUUID().toString()
    private val deviceId = UUID.randomUUID().toString()
    private val deviceIdHash = UUID.randomUUID().toString()

    @Test
    fun `areMainIdentifiersValid for filledIdentifiers object`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(uuid, deviceId, deviceIdHash)))
            .isTrue()
    }

    @Test
    fun `areMainIdentifiersValid for null uuid`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(null, deviceId, deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for empty uuid`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers("", deviceId, deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for null deviceId`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(uuid, null, deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for empty deviceId`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(uuid, "", deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for null deviceIdHash`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(uuid, deviceId, null)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifierValid for empty deviceIdHash`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(uuid, deviceId, "")))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for empty identifiers object`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(Identifiers(null, null, null)))
            .isFalse()
    }
}
