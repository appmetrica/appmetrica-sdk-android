package io.appmetrica.analytics.logger.common.impl;

import android.util.Log;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
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
