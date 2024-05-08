package io.appmetrica.analytics.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CertificatesFingerprintsProvider {

    private static final String TAG = "[CertificatesFingerprintsProvider]";
    private static final String SHA1 = "SHA1";

    @NonNull
    private final Context mContext;
    @NonNull
    private final PreferencesComponentDbStorage mPreferencesComponentDbStorage;
    @NonNull
    private final String mPackageName;
    @NonNull
    private final SafePackageManager mPackageManager;

    public CertificatesFingerprintsProvider(@NonNull Context context,
                                            @NonNull PreferencesComponentDbStorage preferencesComponentDbStorage) {
        this(context, preferencesComponentDbStorage, context.getPackageName(), new SafePackageManager());
    }

    @VisibleForTesting
    CertificatesFingerprintsProvider(@NonNull Context context,
                                     @NonNull PreferencesComponentDbStorage preferencesComponentDbStorage,
                                     @NonNull String packageName,
                                     @NonNull SafePackageManager packageManager) {
        mContext = context;
        mPreferencesComponentDbStorage = preferencesComponentDbStorage;
        mPackageName = packageName;
        mPackageManager = packageManager;
    }

    @NonNull
    public List<String> getSha1() {
        List<String> fingerprints = getFromPreferences();
        if (fingerprints.isEmpty()) {
            fingerprints = getFromSystem();
            if (fingerprints.isEmpty() == false) {
                saveToPreferences(fingerprints);
            }
        }
        return fingerprints;
    }

    @NonNull
    private List<String> getFromPreferences() {
        return mPreferencesComponentDbStorage.getCertificatesSha1Fingerprints();
    }

    @NonNull
    private List<String> getFromSystem() {
        List<String> fingerprints = new ArrayList<String>();
        try {
            final Signature[] signatures;
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.P)) {
                signatures = getSignaturesP();
            } else {
                final PackageInfo packageInfo = mPackageManager
                        .getPackageInfo(mContext, mPackageName, PackageManager.GET_SIGNATURES);
                signatures = packageInfo.signatures;
            }
            if (signatures != null) {
                for (Signature signature : signatures) {
                    final String sha1 = getSha1OfSignature(signature);
                    if (sha1 != null) {
                        fingerprints.add(sha1);
                    }
                }
            }
        } catch (Throwable th) {
            YLogger.error(TAG, th);
        }
        Collections.sort(fingerprints);
        return fingerprints;
    }

    @TargetApi(Build.VERSION_CODES.P)
    @Nullable
    private Signature[] getSignaturesP() {
        final PackageInfo packageInfo = mPackageManager
                .getPackageInfo(mContext, mPackageName, PackageManager.GET_SIGNING_CERTIFICATES);
        SigningInfo signingInfo = packageInfo.signingInfo;
        if (signingInfo.hasMultipleSigners()) {
            return signingInfo.getApkContentsSigners();
        } else {
            return signingInfo.getSigningCertificateHistory();
        }
    }

    @Nullable
    private String getSha1OfSignature(@NonNull Signature signature) {
        try {
            return StringUtils.formatSha1(
                    MessageDigest.getInstance(SHA1).digest(signature.toByteArray())
            );
        } catch (Throwable th) {
            YLogger.error(TAG, th);
            return null;
        }
    }

    private void saveToPreferences(@NonNull List<String> sha1s) {
        mPreferencesComponentDbStorage.putCertificatesSha1Fingerprints(sha1s).commit();
    }
}
