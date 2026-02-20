package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.impl.TestsData.TEST_ENVIRONMENT_KEY;
import static io.appmetrica.analytics.impl.TestsData.TEST_ENVIRONMENT_VALUE;
import static org.mockito.Mockito.verify;

public class ErrorEnvironmentTest extends CommonTest {

    @Mock
    private SimpleMapLimitation mSimpleMapLimitation;
    private ErrorEnvironment mErrorEnvironment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mErrorEnvironment = new ErrorEnvironment(mSimpleMapLimitation);
    }

    @Test
    public void testPutShouldUseLimitation() {
        mErrorEnvironment.put(TEST_ENVIRONMENT_KEY, TEST_ENVIRONMENT_VALUE);
        verify(mSimpleMapLimitation).tryToAddValue(mErrorEnvironment.getEnvironmentValues(), TEST_ENVIRONMENT_KEY, TEST_ENVIRONMENT_VALUE);
    }
}
