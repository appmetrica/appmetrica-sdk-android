package io.appmetrica.analytics.impl;

import android.os.Bundle;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class SelfProcessReporterTest extends CommonTest {

    @Mock
    private AppMetricaServiceCore mAppMetricaServiceCore;
    @Mock
    private Bundle mBundle;

    private SelfProcessReporter mSelfProcessReporter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mSelfProcessReporter = new SelfProcessReporter(mAppMetricaServiceCore);
    }

    @Test
    public void testResumeUserSession() {
        mSelfProcessReporter.resumeUserSession(mBundle);
        verify(mAppMetricaServiceCore).resumeUserSession(mBundle);
    }

    @Test
    public void testPauseUserSession() {
        mSelfProcessReporter.pauseUserSession(mBundle);
        verify(mAppMetricaServiceCore).pauseUserSession(mBundle);
    }

    @Test
    public void reportData() {
        mSelfProcessReporter.reportData(2, mBundle);
        verify(mAppMetricaServiceCore).reportData(2, mBundle);
    }
}
