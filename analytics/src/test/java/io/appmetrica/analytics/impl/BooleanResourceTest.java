package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.res.Resources;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BooleanResourceTest extends CommonTest {

    private final String mResourceName = "mResourceName";

    @Mock
    private Resources mResources;
    private Context mContext;

    @Rule
    public ContextRule contextRule = new ContextRule();

    private BooleanResourceRetriever mBooleanResourceRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = contextRule.getContext();
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
