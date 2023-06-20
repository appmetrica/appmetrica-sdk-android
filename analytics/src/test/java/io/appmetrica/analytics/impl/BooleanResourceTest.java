package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.res.Resources;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BooleanResourceTest extends CommonTest {

    private final String mResourceName = "mResourceName";

    @Mock
    private Resources mResources;
    private Context mContext;

    private BooleanResourceRetriever mBooleanResourceRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(mContext.getResources()).thenReturn(mResources);
        mBooleanResourceRetriever = new BooleanResourceRetriever(mContext, mResourceName);
    }

    @Test
    public void testHasResource() {
        when(mResources.getIdentifier(eq(mResourceName), eq("bool"), anyString())).thenReturn(0);
        assertThat(mBooleanResourceRetriever.getResource()).isNull();
    }

    @Test
    public void testResourceOK() {
        final int resId = 10;
        final boolean isOffline = true;
        when(mResources.getIdentifier(eq(mResourceName), eq("bool"), anyString())).thenReturn(resId);
        when(mResources.getBoolean(resId)).thenReturn(isOffline);
        assertThat(mBooleanResourceRetriever.getResource()).isEqualTo(isOffline);
    }
}
