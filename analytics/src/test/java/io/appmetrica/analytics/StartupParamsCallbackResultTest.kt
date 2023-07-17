package io.appmetrica.analytics

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StartupParamsCallbackResultTest {

    @Test
    fun allFieldsAreNullIfEmptyParameters() {
        val result = StartupParamsCallback.Result(mapOf())
        SoftAssertions().apply {
            assertThat(result.uuid).isNull()
            assertThat(result.deviceId).isNull()
            assertThat(result.deviceIdHash).isNull()
            assertThat(result.parameterForKey("some_key")).isNull()
        }.assertAll()
    }

    @Test
    fun allFieldsAreNotNullIfFilledParameters() {
        val result = StartupParamsCallback.Result(mapOf(
            StartupParamsCallback.APPMETRICA_UUID to IdentifiersResult("some_uuid", IdentifierStatus.OK, null),
            StartupParamsCallback.APPMETRICA_DEVICE_ID to IdentifiersResult("some_deviceId", IdentifierStatus.OK, null),
            StartupParamsCallback.APPMETRICA_DEVICE_ID_HASH to IdentifiersResult("some_deviceIdHash", IdentifierStatus.OK, null),
            "some_startup_key" to IdentifiersResult("some_startupValue", IdentifierStatus.OK, null),
        ))
        SoftAssertions().apply {
            assertThat(result.uuid).isEqualTo("some_uuid")
            assertThat(result.deviceId).isEqualTo("some_deviceId")
            assertThat(result.deviceIdHash).isEqualTo("some_deviceIdHash")
            assertThat(result.parameterForKey("some_startup_key")).isEqualTo("some_startupValue")
        }.assertAll()
    }
}
