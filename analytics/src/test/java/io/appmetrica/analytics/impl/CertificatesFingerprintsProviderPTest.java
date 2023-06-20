package io.appmetrica.analytics.impl;

import android.content.pm.PackageManager;
import android.content.pm.SigningInfo;
import android.os.Build;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class CertificatesFingerprintsProviderPTest extends CertificatesFingerprintsProviderTest {

    @Mock
    private SigningInfo mSigningInfo;

    @Before
    public void setUp() {
        super.setUp();
        when(mPackageManager.getPackageInfo(mContext, mPackageName, PackageManager.GET_SIGNING_CERTIFICATES)).thenReturn(mPackageInfo);
    }

    @Test
    public void testGetSha1WhenGotFromSystemSavedToPreferences() {
        mPackageInfo.signingInfo = mSigningInfo;
        when(mSigningInfo.getSigningCertificateHistory()).thenReturn(mSignatures);
        List<String> value = mProvider.getSha1();
        InOrder inOrder = inOrder(mPreferences, mPreferences);
        inOrder.verify(mPreferences).putCertificatesSha1Fingerprints(value);
        inOrder.verify(mPreferences).commit();
    }

    @Test
    public void testGetSha1MultipleSignersFromSystemBothOk() {
        mPackageInfo.signingInfo = mSigningInfo;
        when(mSigningInfo.hasMultipleSigners()).thenReturn(true);
        when(mSigningInfo.getApkContentsSigners()).thenReturn(mSignatures);
        assertThat(mProvider.getSha1()).hasSize(2).allMatch(mValidSha1);
    }

    @Test
    public void testGetSha1SingleSignerFromSystemBothOk() {
        mPackageInfo.signingInfo = mSigningInfo;
        when(mSigningInfo.hasMultipleSigners()).thenReturn(false);
        when(mSigningInfo.getSigningCertificateHistory()).thenReturn(mSignatures);
        assertThat(mProvider.getSha1()).hasSize(2).allMatch(mValidSha1);
    }

    @Test
    public void testGetSha1MultipleSignersFromSystemOnlyOneOk() {
        mPackageInfo.signingInfo = mSigningInfo;
        when(mSigningInfo.hasMultipleSigners()).thenReturn(true);
        when(mSigningInfo.getApkContentsSigners()).thenReturn(mSignatures);
        when(mSignature1.toByteArray()).thenThrow(new RuntimeException());
        assertThat(mProvider.getSha1()).hasSize(1).allMatch(mValidSha1);
    }

    @Test
    public void testGetSha1SingleSignerFromSystemOnlyOneOk() {
        mPackageInfo.signingInfo = mSigningInfo;
        when(mSigningInfo.hasMultipleSigners()).thenReturn(false);
        when(mSigningInfo.getSigningCertificateHistory()).thenReturn(mSignatures);
        when(mSignature1.toByteArray()).thenThrow(new RuntimeException());
        assertThat(mProvider.getSha1()).hasSize(1).allMatch(mValidSha1);
    }

    @Test
    public void testGetSha1FromSystemThrows() {
        when(mSigningInfo.getApkContentsSigners()).thenThrow(new RuntimeException());
        assertThat(mProvider.getSha1()).isEmpty();
    }
}
