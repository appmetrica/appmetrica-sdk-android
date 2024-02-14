package io.appmetrica.analytics.logger.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MultilineMessageLogConsumerTest {
    @Mock
    private IMessageLogConsumer<String> mConsumer;
    @Mock
    private ILogMessageSplitter mSplitter;

    private MultilineMessageLogConsumer mMultilineMessageLogConsumer;

    private final String tag = "tag";
    private final String message = "message";
    private final String formattedMessage = "%s-%s";
    private final Object[] formattedMessageArgs = new String[] {"first", "second"};
    private final String formatMessageResult = "first-second";

    private final String firstLine = "first line";
    private final String secondLine = "second line";
    private final String thirdLine = "third line";

    private final List<String> splitLines = Arrays.asList(firstLine, secondLine, thirdLine);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mMultilineMessageLogConsumer = new MultilineMessageLogConsumer(mConsumer, mSplitter);
    }

    @Test
    public void consumeForSingleLine() {
        when(mSplitter.split(message)).thenReturn(Collections.singletonList(message));
        mMultilineMessageLogConsumer.consume(message);
        verify(mConsumer).consume(message);
        verifyNoMoreInteractions(mConsumer);
    }

    @Test
    public void consumeForSingleFormattedLine() {
        when(mSplitter.split(formatMessageResult)).thenReturn(Collections.singletonList(formatMessageResult));
        mMultilineMessageLogConsumer.consume(formattedMessage, formattedMessageArgs);
        verify(mConsumer).consume(formatMessageResult);
        verifyNoMoreInteractions(mConsumer);
    }

    @Test
    public void consumeWithTagForSingleLine() {
        when(mSplitter.split(message)).thenReturn(Collections.singletonList(message));
        mMultilineMessageLogConsumer.consumeWithTag(tag, message);
        verify(mConsumer).consumeWithTag(tag, message);
        verifyNoMoreInteractions(mConsumer);
    }

    @Test
    public void consumeWithTagForSingleFormattedLine() {
        when(mSplitter.split(formatMessageResult)).thenReturn(Collections.singletonList(formatMessageResult));
        mMultilineMessageLogConsumer.consumeWithTag(tag, formattedMessage, formattedMessageArgs);
        verify(mConsumer).consumeWithTag(tag, formatMessageResult);
        verifyNoMoreInteractions(mConsumer);
    }

    @Test
    public void consumeForMultiline() {
        when(mSplitter.split(message)).thenReturn(splitLines);

        mMultilineMessageLogConsumer.consume(message);

        verifyMultilineConsuming();
    }

    @Test
    public void consumeForMultilineFormattedMessage() {
        when(mSplitter.split(formatMessageResult)).thenReturn(splitLines);

        mMultilineMessageLogConsumer.consume(formattedMessage, formattedMessageArgs);

        verifyMultilineConsuming();
    }

    @Test
    public void consumeWithTagForMultiline() {
        when(mSplitter.split(message)).thenReturn(splitLines);

        mMultilineMessageLogConsumer.consumeWithTag(tag, message);

        verifyMultilineConsumingWithTag();
    }

    @Test
    public void consumeWithTagForMultilineFormattedMessage() {
        when(mSplitter.split(formatMessageResult)).thenReturn(splitLines);

        mMultilineMessageLogConsumer.consumeWithTag(tag, formattedMessage, formattedMessageArgs);

        verifyMultilineConsumingWithTag();
    }

    private void verifyMultilineConsuming() {
        InOrder inOrder = inOrder(mConsumer);
        inOrder.verify(mConsumer).consume(firstLine);
        inOrder.verify(mConsumer).consume(secondLine);
        inOrder.verify(mConsumer).consume(thirdLine);
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyMultilineConsumingWithTag() {
        InOrder inOrder = inOrder(mConsumer);
        inOrder.verify(mConsumer).consumeWithTag(tag, firstLine);
        inOrder.verify(mConsumer).consumeWithTag(tag, secondLine);
        inOrder.verify(mConsumer).consumeWithTag(tag, thirdLine);
        inOrder.verifyNoMoreInteractions();
    }
}
