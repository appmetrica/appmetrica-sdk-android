package io.appmetrica.analytics.logger.common.impl;

import android.util.Log;
import androidx.annotation.NonNull;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.rules.MockedStaticRule;
import java.util.Locale;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LogMessageConstructorTest extends CommonTest {

    @Rule
    public MockedStaticRule<Log> logRule = new MockedStaticRule<>(Log.class);

    @NonNull
    private final String tag = "[some_tag]";
    @NonNull
    private final String message = "message %s";
    @NonNull
    private final String arg = "arg";
    @NonNull
    private final String fullMessage = "message arg";

    private final LogMessageConstructor constructor = new LogMessageConstructor();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void construct() {
        String constructedMessage = constructor.construct(tag, message, arg);
        assertThat(constructedMessage).isEqualTo(String.format(
            Locale.US,
            "%s [%d-%s] %s",
            tag,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            fullMessage
        ));
    }

    @Test
    public void constructWithThrowable() {
        Throwable throwable = new IllegalStateException().fillInStackTrace();
        String throwableString = "some_string";
        when(Log.getStackTraceString(throwable)).thenReturn(throwableString);

        String constructedMessage = constructor.construct(tag, throwable, message, arg);
        assertThat(constructedMessage).isEqualTo(String.format(
            Locale.US,
            "%s [%d-%s] %s\n%s",
            tag,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            fullMessage,
            throwableString
        ));
    }

    @Test
    public void constructWithPercentInMessage() {
        String constructedMessage = constructor.construct(tag, "%D45%DF");
        assertThat(constructedMessage).isEqualTo(String.format(
            Locale.US,
            "%s [%d-%s] %s",
            tag,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            "%D45%DF"
        ));
    }

    @Test
    public void constructWithPercentInConstructedMessage() {
        String constructedMessage = constructor.construct(tag, "%D45%DF", arg);
        assertThat(constructedMessage).isEqualTo(String.format(
            Locale.US,
            "%s [%d-%s] %s",
            tag,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            "Attention!!! Invalid log format. See exception details above. Message: %D45%DF; arguments: [arg]"
        ));
    }

    @Test
    public void constructWithInvalidFormatAndMaskingMasksApiKeyInReturnedString() {
        String apiKey = "12345678-1234-1234-1234-123456789012";
        LogMessageConstructor maskingConstructor = new LogMessageConstructor(true);

        String constructedMessage = maskingConstructor.construct(tag, "%D45%DF", apiKey);

        assertThat(constructedMessage).doesNotContain(apiKey);
        assertThat(constructedMessage).contains("12345678-xxxx-xxxx-xxxx-xxxxxxxx9012");
    }

    @Test
    public void constructWithInvalidFormatAndMaskingMasksApiKeyInLogE() {
        String apiKey = "12345678-1234-1234-1234-123456789012";
        String maskedApiKey = "12345678-xxxx-xxxx-xxxx-xxxxxxxx9012";
        LogMessageConstructor maskingConstructor = new LogMessageConstructor(true);

        maskingConstructor.construct(tag, "%D45%DF", apiKey);

        final String expectedErrorMessage = "Attention!!! Invalid log format. See exception details above. " +
            "Message: %D45%DF; arguments: [" + maskedApiKey + "]";
        logRule.getStaticMock().verify(
            () -> Log.e(
                org.mockito.ArgumentMatchers.eq("[LogMessageConstructor]"),
                org.mockito.ArgumentMatchers.eq(expectedErrorMessage),
                org.mockito.ArgumentMatchers.any(Throwable.class)
            )
        );
    }

    @Test
    public void constructWithPercentInConstructedMessageArgument() {
        String constructedMessage = constructor.construct(tag, "%s", "argu%DF2ment");
        assertThat(constructedMessage).isEqualTo(String.format(
            Locale.US,
            "%s [%d-%s] %s",
            tag,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            "argu%DF2ment"
        ));
    }
}
