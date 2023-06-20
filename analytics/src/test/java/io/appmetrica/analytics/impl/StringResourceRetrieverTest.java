package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.res.Resources;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.UUID;
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
public class StringResourceRetrieverTest extends CommonTest {

    private final String mResourceName = "mResourceName";

    @Mock
    private Resources mResources;
    private Context mContext;

    private StringResourceRetriever mStringResourceRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(mContext.getResources()).thenReturn(mResources);
        mStringResourceRetriever = new StringResourceRetriever(mContext, mResourceName);
    }

    @Test
    public void testHasResource() {
        when(mResources.getIdentifier(eq(mResourceName), eq("string"), anyString())).thenReturn(0);
        assertThat(mStringResourceRetriever.getResource()).isNull();
    }

    @Test
    public void testResourceOK() {
        final int resId = 10;
        final String buildId = UUID.randomUUID().toString();
        when(mResources.getIdentifier(eq(mResourceName), eq("string"), anyString())).thenReturn(resId);
        when(mContext.getString(resId)).thenReturn(String.valueOf(buildId));
        assertThat(mStringResourceRetriever.getResource()).isEqualTo(buildId);
    }

}
