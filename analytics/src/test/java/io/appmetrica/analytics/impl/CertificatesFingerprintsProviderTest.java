package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class CertificatesFingerprintsProviderTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Mock
    SafePackageManager mPackageManager;
    @Mock
    Signature mSignature1;
    @Mock
    Signature mSignature2;
    @Mock
    PreferencesComponentDbStorage mPreferences;
    PackageInfo mPackageInfo;
    Signature[] mSignatures;
    Context mContext;
    String mPackageName = "io.appmetrica.analytics";
    List<String> mCertificatesFromPreferences = Arrays.asList("cert1", "cert2");
    CertificatesFingerprintsProvider mProvider;
    Predicate<String> mValidSha1 = new Predicate<String>() {
        @Override
        public boolean test(String s) {
            return s.matches("(.{2}:)+(.{2})");
        }
    };

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = contextRule.getContext();
        when(mSignature1.toByteArray()).thenReturn("Yandex certificate1".getBytes());
        when(mSignature2.toByteArray()).thenReturn("Yandex certificate2".getBytes());
        when(mPreferences.putCertificatesSha1Fingerprints(any(List.class))).thenReturn(mPreferences);
        mPackageInfo = new PackageInfo();
        mSignatures = new Signature[]{mSignature1, mSignature2};
        mProvider = new CertificatesFingerprintsProvider(mContext, mPreferences, mPackageName, mPackageManager);
    }

    @Test
    public void testGetSha1FromPreferences() {
        when(mPreferences.getCertificatesSha1Fingerprints()).thenReturn(mCertificatesFromPreferences);
        assertThat(mProvider.getSha1()).isEqualTo(mCertificatesFromPreferences);
    }

}
