package io.appmetrica.analytics.impl.crash.client;

import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UnhandledExceptionTest extends CommonTest {

    @Mock
    private AllThreads mAllThreads;
    @Mock
    private List<StackTraceItemInternal> mMethodCallStacktrace;
    private final String platform = "unity";
    private final String virtualMachineVersion = "3.4.5";
    private final Map<String, String> environment = new HashMap<>();
    @Mock
    private ThrowableModel mThrowable;
    private final String mBuildId = "qwerty12345";
    private final boolean mIsOffline = true;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNulls() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        UnhandledException unhandledException = new UnhandledException(null, null, null, null, null, null, null, null);
        ObjectPropertyAssertions(unhandledException)
                .checkFieldsAreNull("exception", "allThreads", "methodCallStacktrace", "buildId", "isOffline",
                        "platform", "virtualMachineVersion", "pluginEnvironment")
                .checkAll();
    }

    @Test
    public void testFilled() throws IllegalAccessException {
        UnhandledException unhandledException = new UnhandledException(
                mThrowable,
                mAllThreads,
                mMethodCallStacktrace,
                platform,
                virtualMachineVersion,
                environment,
                mBuildId,
                mIsOffline
        );
        ObjectPropertyAssertions(unhandledException)
                .checkField("exception", mThrowable)
                .checkField("allThreads", mAllThreads)
                .checkField("methodCallStacktrace", mMethodCallStacktrace)
                .checkField("platform", platform)
                .checkField("virtualMachineVersion", virtualMachineVersion)
                .checkField("pluginEnvironment", environment)
                .checkField("buildId", mBuildId)
                .checkField("isOffline", mIsOffline)
                .checkAll();
    }

    @Test
    public void testToStringFilled() {
        String value = new UnhandledException(
                mThrowable,
                mAllThreads,
                mMethodCallStacktrace,
                platform,
                virtualMachineVersion,
                environment,
                mBuildId,
                mIsOffline
        ).toString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(value).contains("errorName='java.lang.RuntimeException'", "exception=java.lang.RuntimeException");
        for (StackTraceItemInternal element : mThrowable.getStacktrace()) {
            softly.assertThat(value).contains(
                    String.valueOf(element.getClassName()),
                    String.valueOf(element.getMethodName()),
                    String.valueOf(element.getFileName()),
                    String.valueOf(element.getLine())
            );
        }
    }

    @Test
    public void testToStringNulls() {
        final String unhandledException = new UnhandledException(null, null, null, null, null, null,null, null).toString();
        assertThat(unhandledException).contains("exception=null");
    }

    @Test
    public void getErrorHasException() {
        ThrowableModel throwableModel = mock(ThrowableModel.class);
        String errorName = "some exception";
        when(throwableModel.getExceptionClass()).thenReturn(errorName);
        UnhandledException unhandledException = new UnhandledException(throwableModel, null, null, null, null, null, null, null);
        assertThat(UnhandledException.getErrorName(unhandledException)).isEqualTo(errorName);
    }

    @Test
    public void getErrorNoException() {
        UnhandledException unhandledException = new UnhandledException(null, null, null, null, null, null, null, null);
        assertThat(UnhandledException.getErrorName(unhandledException)).isEqualTo("");
    }
}
