package io.appmetrica.analytics.coreutils.internal.logger;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class YLoggerTests {

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
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void d() {
        YLogger.d(message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void info() {
        YLogger.info(tag, message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void i() {
        YLogger.i(message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void warning() {
        YLogger.warning(tag, message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void w() {
        YLogger.w(message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void errorWithoutThrowable() {
        YLogger.error(tag, message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void eWithoutThrowable() {
        YLogger.e(message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void errorWithThrowable() {
        YLogger.error(tag, mThrowable, message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void eWithThrowable() {
        YLogger.e(mThrowable, message, args);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void errorWithThrowableNoMessage() {
        YLogger.error(tag, mThrowable);
        verifyZeroInteractions(mYLogger);
    }

    @Test
    public void dumpJson() {
        YLogger.dumpJson(tag, mJSONObject);
        verifyZeroInteractions(mYLogger);
    }

}
