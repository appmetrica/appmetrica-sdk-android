package io.appmetrica.analytics.coreutils.internal.logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SingleWarningMessageLogConsumerTest {

    @Mock
    private BaseLogger mBaseLogger;

    private SingleWarningMessageLogConsumer mConsumer;

    private final String tag = "tag";
    private final String message = "message";
    private final Object[] args = new Object[] {new Object(), new Object()};

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mConsumer = new SingleWarningMessageLogConsumer(mBaseLogger);
    }

    @Test
    public void consume() {
        mConsumer.consume(message, args);
        verify(mBaseLogger).fw(message, args);
    }

    @Test
    public void consumeWithTag() {
        mConsumer.consumeWithTag(tag, message, args);
        verify(mBaseLogger).fw(tag + message, args);
    }
}
