package io.appmetrica.analytics.coreutils.internal.logger;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class SnapshotYLoggerTests {

    @Mock
    private YLoggerImpl mYLogger;
    @Mock
    private Throwable mThrowable;
    @Mock
    private JSONObject mJSONObject;

    private final String tag = "tag";
    private final String message = "message";
    private final Object[] args = new Object[] {new Object(), new Object()};

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        YLogger.setImpl(mYLogger);
    }

    @Test
    public void debug() {
        YLogger.debug(tag, message, args);
        verify(mYLogger).debug(tag, message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void d() {
        YLogger.d(message, args);
        verify(mYLogger).d(message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void info() {
        YLogger.info(tag, message, args);
        verify(mYLogger).info(tag, message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void i() {
        YLogger.i(message, args);
        verify(mYLogger).i(message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void warning() {
        YLogger.warning(tag, message, args);
        verify(mYLogger).warning(tag, message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void w() {
        YLogger.w(message, args);
        verify(mYLogger).w(message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void errorWithoutThrowable() {
        YLogger.error(tag, message, args);
        verify(mYLogger).error(tag, message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void eWithoutThrowable() {
        YLogger.e(message, args);
        verify(mYLogger).e(message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void errorWithThrowable() {
        YLogger.error(tag, mThrowable, message, args);
        verify(mYLogger).error(tag, mThrowable, message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void eWithThrowable() {
        YLogger.e(mThrowable, message, args);
        verify(mYLogger).e(mThrowable, message, args);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void errorWithThrowableNoMessage() {
        YLogger.error(tag, mThrowable);
        verify(mYLogger).error(tag, mThrowable, null);
        verifyNoMoreInteractions(mYLogger);
    }

    @Test
    public void dumpJson() {
        YLogger.dumpJson(tag, mJSONObject);
        verify(mYLogger).dumpJson(tag, mJSONObject);
        verifyNoMoreInteractions(mYLogger);
    }
}
