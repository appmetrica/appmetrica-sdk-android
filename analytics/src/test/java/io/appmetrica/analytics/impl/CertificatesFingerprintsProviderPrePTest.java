package io.appmetrica.analytics.impl;

import android.content.pm.PackageManager;
import android.os.Build;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O)
public class CertificatesFingerprintsProviderPrePTest extends CertificatesFingerprintsProviderTest {

    @Before
    public void setUp() {
        super.setUp();
        when(mPackageManager.getPackageInfo(mContext, mPackageName, PackageManager.GET_SIGNATURES)).thenReturn(mPackageInfo);
    }

    @Test
    public void testGetSha1WhenGotFromSystemSavedToPreferences() {
        mPackageInfo.signatures = mSignatures;
        List<String> value = mProvider.getSha1();
        InOrder inOrder = inOrder(mPreferences, mPreferences);
        inOrder.verify(mPreferences).putCertificatesSha1Fingerprints(value);
        inOrder.verify(mPreferences).commit();
    }

    @Test
    public void testGetSha1FromSystemBothOk() {
        mPackageInfo.signatures = mSignatures;
        assertThat(mProvider.getSha1()).hasSize(2).allMatch(mValidSha1);
    }

    @Test
    public void testGetSha1FromSystemOnlyOneOk() {
        mPackageInfo.signatures = mSignatures;
        when(mSignature1.toByteArray()).thenThrow(new RuntimeException());
        assertThat(mProvider.getSha1()).hasSize(1).allMatch(mValidSha1);
    }

    @Test
    public void testGetSha1FromSystemThrows() {
        mPackageInfo.signatures = null; // trigger NPE
        assertThat(mProvider.getSha1()).isEmpty();
    }

}
