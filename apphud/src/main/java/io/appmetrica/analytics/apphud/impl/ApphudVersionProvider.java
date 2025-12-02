package io.appmetrica.analytics.apphud.impl;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ApphudVersionProvider {

    private static final String TAG = "[ApphudVersionProvider]";

    public static ApphudVersion getApphudVersion() {
        String version = getApphudVersionString();
        if (version == null) {
            DebugLogger.INSTANCE.info(TAG, "Cannot determine Apphud version, assuming V3");
            return ApphudVersion.APPHUD_V3;
        }

        try {
            int majorVersion = parseMajorVersion(version);
            DebugLogger.INSTANCE.info(
                TAG,
                "Apphud version is %s",
                version
            );
            if (majorVersion >= 3) {
                return ApphudVersion.APPHUD_V3;
            } else {
                return ApphudVersion.APPHUD_V2;
            }
        } catch (NumberFormatException e) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Failed to parse Apphud version %s: %s, assuming V3",
                version,
                e.getMessage()
            );
            return ApphudVersion.APPHUD_V3;
        }
    }

    private static int parseMajorVersion(String version) throws NumberFormatException {
        String[] parts = version.split("\\.");
        if (parts.length == 0) {
            throw new NumberFormatException("Invalid version format: " + version);
        }
        // Remove any non-numeric suffix from major version (e.g., "3-beta" -> "3")
        String majorPart = parts[0].split("-")[0];
        return Integer.parseInt(majorPart);
    }

    @Nullable
    private static String getApphudVersionString() {
        try {
            Class<?> buildConfigClass = ReflectionUtils
                .findClass("com.apphud.sdk.BuildConfig");
            if (buildConfigClass == null) {
                return null;
            }
            String version = (String) buildConfigClass.getField("VERSION_NAME").get(null);
            DebugLogger.INSTANCE.info(TAG, "Apphud version: %s", version);
            return version;
        } catch (Throwable e) {
            DebugLogger.INSTANCE.info(TAG, "Failed to get Apphud version with error %s", e.getMessage());
        }
        return null;
    }
}
