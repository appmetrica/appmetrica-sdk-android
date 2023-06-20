package io.appmetrica.analytics.impl.crash.client;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AnrTest extends CommonTest {

    @Mock
    private AllThreads mAllThreads;
    private String mBuildId = "buildId";
    private Boolean mIsOffline = true;
    private Anr mAnr;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mAnr = new Anr(mAllThreads, mBuildId, mIsOffline);
    }

    @Test
    public void testFields() {
        assertThat(mAnr.mAllThreads).isEqualTo(mAllThreads);
        assertThat(mAnr.mBuildId).isEqualTo(mBuildId);
        assertThat(mAnr.mIsOffline).isEqualTo(mIsOffline);
    }
}
