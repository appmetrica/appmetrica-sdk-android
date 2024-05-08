package io.appmetrica.analytics.logger.impl;

import java.util.Arrays;
import java.util.Collection;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class YLoggerImplTest {

    private final boolean enabled;
    private final int times;

    public YLoggerImplTest(boolean enabled) {
        this.enabled = enabled;
        times = enabled ? 1 : 0;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true},
                {false}
        });
    }

    @Mock
    private BaseLogger mBaseLogger;
    @Mock
    private MessageLogConsumerProvider mConsumerProvider;
    @Mock
    private IMessageLogConsumer<String> mInfoConsumer;
    @Mock
    private IMessageLogConsumer<String> mDebugConsumer;
    @Mock
    private IMessageLogConsumer<String> mWarningConsumer;
    @Mock
    private IMessageLogConsumer<JSONObject> mJsonInfoConsumer;
    @Mock
    private Throwable mThrowable;
    @Mock
    private JSONObject mJSONObject;

    private final String tag = "tag";
    private final String message = "message";
    private final Object[] args = new Object[] {new Object(), new Object()};

    private YLoggerImpl mYLogger;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mConsumerProvider.getDebugLogConsumer()).thenReturn(mDebugConsumer);
        when(mConsumerProvider.getInfoLogConsumer()).thenReturn(mInfoConsumer);
        when(mConsumerProvider.getWarningMessageLogConsumer()).thenReturn(mWarningConsumer);
        when(mConsumerProvider.getJsonInfoLogConsumer()).thenReturn(mJsonInfoConsumer);

        mYLogger = new YLoggerImpl(mBaseLogger, enabled, mConsumerProvider);
    }

    @Test
    public void debug() {
        mYLogger.debug(tag, message, args);
        verify(mDebugConsumer, times(times)).consumeWithTag(tag, message, args);
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    public void info() {
        mYLogger.info(tag, message, args);
        verify(mInfoConsumer, times(times)).consumeWithTag(tag, message, args);
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    public void warning() {
        mYLogger.warning(tag, message, args);
        verify(mWarningConsumer, times(times)).consumeWithTag(tag, message, args);
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    public void errorWithoutThrowable() {
        mYLogger.error(tag, message, args);
        verify(mBaseLogger, times(times)).fe(tag + message, args);
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    public void errorWithThrowable() {
        mYLogger.error(tag, mThrowable, message, args);
        verify(mBaseLogger, times(times)).fe(mThrowable, tag + message, args);
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    public void dumpJson() {
        mYLogger.dumpJson(tag, mJSONObject);
        verify(mJsonInfoConsumer, times(times)).consumeWithTag(tag, mJSONObject);
        verifyNoMoreInteractionsWithMocks();
    }

    private void verifyNoMoreInteractionsWithMocks() {
        verifyNoMoreInteractions(mBaseLogger, mDebugConsumer, mInfoConsumer, mWarningConsumer, mJsonInfoConsumer);
    }
}
