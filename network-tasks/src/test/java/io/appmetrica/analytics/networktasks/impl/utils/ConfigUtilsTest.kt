package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID

class ConfigUtilsTest {

    private val uuid = UUID.randomUUID().toString()
    private val deviceId = UUID.randomUUID().toString()
    private val deviceIdHash = UUID.randomUUID().toString()

    @Test
    fun `areMainIdentifiersValid for filledIdentifiers object`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(uuid, deviceId, deviceIdHash)))
            .isTrue()
    }

    @Test
    fun `areMainIdentifiersValid for null uuid`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(null, deviceId, deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for empty uuid`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers("", deviceId, deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for null deviceId`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(uuid, null, deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for empty deviceId`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(uuid, "", deviceIdHash)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for null deviceIdHash`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(uuid, deviceId, null)))
            .isFalse()
    }

    @Test
    fun `areMainIdentifierValid for empty deviceIdHash`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(uuid, deviceId, "")))
            .isFalse()
    }

    @Test
    fun `areMainIdentifiersValid for empty identifiers object`() {
        assertThat(ConfigUtils.areMainIdentifiersValid(SdkIdentifiers(null, null, null)))
            .isFalse()
    }
}
