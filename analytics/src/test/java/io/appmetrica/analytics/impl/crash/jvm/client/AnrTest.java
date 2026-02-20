package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class AnrTest extends CommonTest {

    @Mock
    private AllThreads mAllThreads;
    private final String mBuildId = "buildId";
    private final Boolean mIsOffline = true;
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
