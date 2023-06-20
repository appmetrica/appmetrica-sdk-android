package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ReporterEnvironmentTest extends CommonTest {

    private static final String USER_PROFILE_ID = "user_profile_id";

    public static ReporterEnvironment createStubbedEnvironment() {
        return new ReporterEnvironment(
                new ProcessConfiguration(RuntimeEnvironment.getApplication(), mock(DataResultReceiver.class)),
                new CounterConfiguration(),
                USER_PROFILE_ID
        );
    }

    private ReporterEnvironment mReporterEnvironment;
    private ErrorEnvironment mErrorEnvironment;
    @Mock
    private ProcessConfiguration processConfiguration;
    @Mock
    private CounterConfiguration counterConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mReporterEnvironment = new ReporterEnvironment(
                processConfiguration,
                counterConfiguration,
                USER_PROFILE_ID
        );
        mErrorEnvironment = mock(ErrorEnvironment.class);
        mReporterEnvironment.setErrorEnvironment(mErrorEnvironment);
    }

    @Test
    public void constuctor() throws Exception {
        ObjectPropertyAssertions(
                new ReporterEnvironment(processConfiguration, counterConfiguration, USER_PROFILE_ID)
        )
                .withPrivateFields(true)
                .withFinalFieldOnly(false)
                .checkField("mErrorEnvironment", (ErrorEnvironment) null)
                .checkField("mPreloadInfoWrapper", null)
                .checkField("initialUserProfileID", USER_PROFILE_ID)
                .checkField("misSessionPaused", true)
                .checkAll();
    }

    @Test
    public void testSetCrashEnvironmentValueShouldForwardsToCrashEnvironment() {
        mReporterEnvironment.putErrorEnvironmentValue(TestsData.TEST_ENVIRONMENT_KEY, TestsData.TEST_ENVIRONMENT_VALUE);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

        verify(mErrorEnvironment, times(1)).put(arg1.capture(), arg2.capture());

        assertThat(arg1.getValue()).isEqualTo(TestsData.TEST_ENVIRONMENT_KEY);
        assertThat(arg2.getValue()).isEqualTo(TestsData.TEST_ENVIRONMENT_VALUE);
    }

    @Test
    public void testIsSessionPausedInitialState() {
        assertThat(mReporterEnvironment.isForegroundSessionPaused()).isTrue();
    }

    @Test
    public void testResumeSession() {
        mReporterEnvironment.onResumeForegroundSession();
        assertThat(mReporterEnvironment.isForegroundSessionPaused()).isFalse();
    }

    @Test
    public void testResumeThenPauseSession() {
        mReporterEnvironment.onResumeForegroundSession();
        mReporterEnvironment.onPauseForegroundSession();
        assertThat(mReporterEnvironment.isForegroundSessionPaused()).isTrue();
    }
}
