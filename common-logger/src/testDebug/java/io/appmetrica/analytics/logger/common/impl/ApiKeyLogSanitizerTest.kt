package io.appmetrica.analytics.logger.common.impl

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ApiKeyLogSanitizerTest : CommonTest() {

    @Test
    fun maskFullApiKey() {
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog("apiKey = $FULL_API_KEY"))
            .isEqualTo("apiKey = $PARTIAL_API_KEY")
    }

    @Test
    fun maskApiKeyInJson() {
        val json = """{"apiKey":"$FULL_API_KEY","logs":true}"""
        val expected = """{"apiKey":"$PARTIAL_API_KEY","logs":true}"""
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog(json)).isEqualTo(expected)
    }

    @Test
    fun maskMultipleApiKeys() {
        val secondKey = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
        val secondPartial = "aaaaaaaa-xxxx-xxxx-xxxx-xxxxxxxxeeee"
        assertThat(
            ApiKeyLogSanitizer.maskApiKeysInLog("keys: $FULL_API_KEY, $secondKey")
        ).isEqualTo("keys: $PARTIAL_API_KEY, $secondPartial")
    }

    @Test
    fun leavesAlreadyMaskedApiKeyUnchanged() {
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog("prefix $PARTIAL_API_KEY"))
            .isEqualTo("prefix $PARTIAL_API_KEY")
    }

    @Test
    fun leavesMessageWithoutApiKeyUnchanged() {
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog("Event received: purchase"))
            .isEqualTo("Event received: purchase")
    }

    @Test
    fun handlesEmpty() {
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog("")).isEmpty()
    }

    @Test
    fun masksUppercaseApiKey() {
        val uppercaseKey = "5052C3CC-39A4-4DBC-92D1-89EBC17C0FA9"
        val uppercasePartial = "5052C3CC-xxxx-xxxx-xxxx-xxxxxxxx0FA9"
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog("apiKey = $uppercaseKey"))
            .isEqualTo("apiKey = $uppercasePartial")
    }

    @Test
    fun masksAdjacentApiKeysWithoutSeparator() {
        val adjacentKeys = FULL_API_KEY + FULL_API_KEY
        val expected = PARTIAL_API_KEY + PARTIAL_API_KEY
        assertThat(ApiKeyLogSanitizer.maskApiKeysInLog(adjacentKeys)).isEqualTo(expected)
    }

    @Test
    fun masksOnlyFullUuidWhenPartialAndFullPresent() {
        assertThat(
            ApiKeyLogSanitizer.maskApiKeysInLog("keys: $PARTIAL_API_KEY and $FULL_API_KEY")
        ).isEqualTo("keys: $PARTIAL_API_KEY and $PARTIAL_API_KEY")
    }

    @Test
    fun leavesInvalidUuidLikeStringsUnchanged() {
        val almostUuidStrings = listOf(
            "5052c3cc-39a4-4dbc-92d1",
            "5052c3cc-39a4-4dbc-92d1-89ebc17c0fa",
            "5052c3cc39a44dbc92d189ebc17c0fa9",
            "gggggggg-gggg-gggg-gggg-gggggggggggg",
        )
        almostUuidStrings.forEach { value ->
            assertThat(ApiKeyLogSanitizer.maskApiKeysInLog("value = $value"))
                .isEqualTo("value = $value")
        }
    }

    private companion object {
        private const val FULL_API_KEY = "5052c3cc-39a4-4dbc-92d1-89ebc17c0fa9"
        private const val PARTIAL_API_KEY = "5052c3cc-xxxx-xxxx-xxxx-xxxxxxxx0fa9"
    }
}
