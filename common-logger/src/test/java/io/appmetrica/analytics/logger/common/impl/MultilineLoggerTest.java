package io.appmetrica.analytics.logger.common.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultilineLoggerTest extends CommonTest {

    @Mock
    private SystemLogger logger;
    @Mock
    private LogMessageConstructor constructor;
    @Mock
    private LogMessageSplitter splitter;

    @NonNull
    private final String tag = "some_tag";
    @NonNull
    private final String message = "some_message";
    @NonNull
    private final String firstArg = "first_arg";
    @NonNull
    private final String secondArg = "second_arg";
    @NonNull
    private final String constructedMessage = "some_constructed_message";
    @NonNull
    private final List<String> messageLines = Arrays.asList("first_line", "second_line");

    private MultilineLogger multilineLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        multilineLogger = new MultilineLogger(logger, constructor, splitter);

        when(constructor.construct(tag, message, firstArg, secondArg)).thenReturn(constructedMessage);
        when(splitter.split(constructedMessage)).thenReturn(messageLines);
    }

    @Test
    public void info() {
        multilineLogger.info(tag, message, firstArg, secondArg);

        for (String messageLine : messageLines) {
            verify(logger).info(messageLine);
        }
    }

    @Test
    public void warning() {
        multilineLogger.warning(tag, message, firstArg, secondArg);

        for (String messageLine : messageLines) {
            verify(logger).warning(messageLine);
        }
    }

    @Test
    public void error() {
        multilineLogger.error(tag, message, firstArg, secondArg);

        for (String messageLine : messageLines) {
            verify(logger).error(messageLine);
        }
    }

    @Test
    public void errorWithThrowable() {
        Throwable throwable = new IllegalAccessError();
        when(constructor.construct(tag, throwable, message, firstArg, secondArg)).thenReturn(constructedMessage);

        multilineLogger.error(tag, throwable, message, firstArg, secondArg);

        for (String messageLine : messageLines) {
            verify(logger).error(messageLine);
        }
    }
}
