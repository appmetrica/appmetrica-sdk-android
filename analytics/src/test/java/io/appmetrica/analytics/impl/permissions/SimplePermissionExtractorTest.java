package io.appmetrica.analytics.impl.permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class SimplePermissionExtractorTest extends CommonTest {

    private Context mContext;
    @Mock
    private PermissionStrategy mShouldAskSystemForPermissionStrategy;

    private String mPermissionName;
    private SimplePermissionExtractor mPermissionExtractor;

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[]{Manifest.permission.ACCESS_COARSE_LOCATION},
            new Object[]{Manifest.permission.READ_PHONE_STATE},
            new Object[]{Manifest.permission.ACCESS_WIFI_STATE}
        );
    }

    public SimplePermissionExtractorTest(String permissionName) {
        mPermissionName = permissionName;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        mPermissionExtractor = new SimplePermissionExtractor(
            mShouldAskSystemForPermissionStrategy
        );
    }

    @Test
    public void testIfShouldAskSystemIsInvoked() throws Exception {
        invokePermissionExtractorCurrentMethod();
        verify(mShouldAskSystemForPermissionStrategy, times(1)).forbidUsePermission(mPermissionName);
    }

    @Test
    public void testDoNotCallSystemWhenForbidden() throws Exception {
        when(mShouldAskSystemForPermissionStrategy.forbidUsePermission(mPermissionName)).thenReturn(true);
        boolean actual = invokePermissionExtractorCurrentMethod();
        verifyNoMoreInteractions(mContext);
        assertThat(actual).isFalse();
    }

    @Test
    public void testWhenAllowedToAskAndDisabledInSystem() throws Exception {
        when(mShouldAskSystemForPermissionStrategy.forbidUsePermission(mPermissionName)).thenReturn(true);
        when(mContext.checkCallingOrSelfPermission(mPermissionName)).thenReturn(PackageManager.PERMISSION_DENIED);
        assertThat(invokePermissionExtractorCurrentMethod()).isFalse();
    }

    @Test
    public void testWhenAllowedToAskAndEnabledInSystem() throws Exception {
        when(mShouldAskSystemForPermissionStrategy.forbidUsePermission(mPermissionName)).thenReturn(true);
        when(mContext.checkCallingOrSelfPermission(mPermissionName)).thenReturn(PackageManager.PERMISSION_GRANTED);
        assertThat(invokePermissionExtractorCurrentMethod()).isFalse();
    }

    private Boolean invokePermissionExtractorCurrentMethod() throws Exception {
        return mPermissionExtractor.hasPermission(mContext, mPermissionName);
    }

}
