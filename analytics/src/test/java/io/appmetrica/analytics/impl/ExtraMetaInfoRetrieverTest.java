package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ExtraMetaInfoRetrieverTest extends CommonTest {

    @Mock
    private StringResourceRetriever mStringResourceRetriever;
    @Mock
    private BooleanResourceRetriever mBooleanResourceRetriever;
    private ExtraMetaInfoRetriever mExtraMetaInfoRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mExtraMetaInfoRetriever = new ExtraMetaInfoRetriever(mStringResourceRetriever, mBooleanResourceRetriever);
    }

    @Test
    public void testNoResources() {
        when(mStringResourceRetriever.getResource()).thenReturn(null);
        when(mBooleanResourceRetriever.getResource()).thenReturn(null);
        assertThat(mExtraMetaInfoRetriever.getBuildId()).isNull();
        assertThat(mExtraMetaInfoRetriever.isOffline()).isNull();
    }

    @Test
    public void testHasResources() {
        final String buildId = "1234567890";
        final boolean isOffline = false;
        when(mStringResourceRetriever.getResource()).thenReturn(buildId);
        when(mBooleanResourceRetriever.getResource()).thenReturn(isOffline);
        assertThat(mExtraMetaInfoRetriever.getBuildId()).isEqualTo(buildId);
        assertThat(mExtraMetaInfoRetriever.isOffline()).isEqualTo(isOffline);
    }
}
