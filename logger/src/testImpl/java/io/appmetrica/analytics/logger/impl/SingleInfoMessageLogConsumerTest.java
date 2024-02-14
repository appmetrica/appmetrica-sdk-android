package io.appmetrica.analytics.logger.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SingleInfoMessageLogConsumerTest {
    @Mock
    private BaseLogger mBaseLogger;

    private final String tag = "Tag";
    private final String message = "Message";
    private final Object[] args = new Object[] {new Object()};

    private SingleInfoMessageLogConsumer mConsumer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mConsumer = new SingleInfoMessageLogConsumer(mBaseLogger);
    }

    @Test
    public void consume() {
        mConsumer.consume(message, args);

        verify(mBaseLogger).fi(message, args);
    }

    @Test
    public void consumeWithTag() {
        mConsumer.consumeWithTag(tag, message, args);

        verify(mBaseLogger).fi(tag + message, args);
    }
}
