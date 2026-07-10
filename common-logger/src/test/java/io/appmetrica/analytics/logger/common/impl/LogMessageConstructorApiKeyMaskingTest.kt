package io.appmetrica.analytics.logger.common.impl

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Locale

internal class LogMessageConstructorApiKeyMaskingTest : CommonTest() {

    private val publicLoggerConstructor = LogMessageConstructor(maskApiKeysInLog = true)
    private val defaultConstructor = LogMessageConstructor()

    @Test
    fun masksApiKeyAfterFormat() {
        val constructedMessage = publicLoggerConstructor.construct("[tag]", "apiKey=%s", FULL_API_KEY)
        assertThat(constructedMessage).contains(PARTIAL_API_KEY)
        assertThat(constructedMessage).doesNotContain(FULL_API_KEY)
    }

    @Test
    fun masksApiKeyInPlainMessage() {
        val constructedMessage = publicLoggerConstructor.construct("[tag]", "config $FULL_API_KEY")
        assertThat(constructedMessage).contains(PARTIAL_API_KEY)
        assertThat(constructedMessage).isEqualTo(
            String.format(
                Locale.US,
                "%s [%d-%s] %s",
                "[tag]",
                Thread.currentThread().getId(),
                Thread.currentThread().name,
                "config $PARTIAL_API_KEY"
            )
        )
    }

    @Test
    fun leavesMessageWithoutApiKeyUnchanged() {
        val constructedMessage = publicLoggerConstructor.construct("[tag]", "Event received: purchase")
        assertThat(constructedMessage).isEqualTo(
            String.format(
                Locale.US,
                "%s [%d-%s] %s",
                "[tag]",
                Thread.currentThread().getId(),
                Thread.currentThread().name,
                "Event received: purchase"
            )
        )
    }

    @Test
    fun masksApiKeyInFormattedMessageWithMultipleArgs() {
        val secondKey = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
        val secondPartial = "aaaaaaaa-xxxx-xxxx-xxxx-xxxxxxxxeeee"
        val constructedMessage = publicLoggerConstructor.construct("[tag]", "keys: %s, %s", FULL_API_KEY, secondKey)
        assertThat(constructedMessage).contains(PARTIAL_API_KEY, secondPartial)
        assertThat(constructedMessage).doesNotContain(FULL_API_KEY, secondKey)
    }

    @Test
    fun masksApiKeyInInvalidFormatErrorMessage() {
        val constructedMessage = publicLoggerConstructor.construct("[tag]", "%D45%DF", FULL_API_KEY)
        assertThat(constructedMessage).contains(PARTIAL_API_KEY)
        assertThat(constructedMessage).doesNotContain(FULL_API_KEY)
        assertThat(constructedMessage).contains("arguments: [$PARTIAL_API_KEY]")
    }

    @Test
    fun masksApiKeyInThrowableMessage() {
        val throwable = IllegalStateException("ignored")
        val constructedMessage = publicLoggerConstructor.construct("[tag]", throwable, "config $FULL_API_KEY")
        assertThat(constructedMessage).contains("config $PARTIAL_API_KEY")
        assertThat(constructedMessage).doesNotContain(FULL_API_KEY)
    }

    @Test
    fun handlesNullMessage() {
        val constructedMessage = publicLoggerConstructor.construct("[tag]", null)
        assertThat(constructedMessage).isEqualTo(
            String.format(
                Locale.US,
                "%s [%d-%s] %s",
                "[tag]",
                Thread.currentThread().getId(),
                Thread.currentThread().name,
                ""
            )
        )
    }

    @Test
    fun doesNotMaskApiKeyForDefaultConstructor() {
        val constructedMessage = defaultConstructor.construct("[tag]", "apiKey=%s", FULL_API_KEY)
        assertThat(constructedMessage).contains(FULL_API_KEY)
        assertThat(constructedMessage).doesNotContain(PARTIAL_API_KEY)
    }

    private companion object {
        private const val FULL_API_KEY = "5052c3cc-39a4-4dbc-92d1-89ebc17c0fa9"
        private const val PARTIAL_API_KEY = "5052c3cc-xxxx-xxxx-xxxx-xxxxxxxx0fa9"
    }
}
