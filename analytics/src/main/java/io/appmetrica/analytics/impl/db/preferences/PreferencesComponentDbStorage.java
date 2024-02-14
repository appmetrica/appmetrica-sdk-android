package io.appmetrica.analytics.impl.db.preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.component.session.SessionDefaults;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.VitalDataSource;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class PreferencesComponentDbStorage extends NameSpacedPreferenceDbStorage implements VitalDataSource {

    public static final long DEFAULT_PERMISSIONS_CHECK_TIME = SessionDefaults.REGULAR_EVENT_NOT_SENT;
    public static final int DEFAULT_LAST_APP_VERSION_WITH_FEATURES = -1;
    public static final String DEFAULT_APPLICATION_FEATURES = StringUtils.EMPTY;
    public static final String DEFAULT_SESSION_PARAMETERS = StringUtils.EMPTY;
    private static final PreferencesItem PERMISSIONS_CHECK_TIME = new PreferencesItem("PERMISSIONS_CHECK_TIME");
    private static final PreferencesItem PROFILE_ID = new PreferencesItem("PROFILE_ID");

    private static final PreferencesItem APP_ENVIRONMENT = new PreferencesItem("APP_ENVIRONMENT");
    private static final PreferencesItem APP_ENVIRONMENT_REVISION = new PreferencesItem("APP_ENVIRONMENT_REVISION");
    private static final PreferencesItem LAST_APP_VERSION_WITH_FEATURES =
            new PreferencesItem("LAST_APP_VERSION_WITH_FEATURES");
    private static final PreferencesItem APPLICATION_FEATURES = new PreferencesItem("APPLICATION_FEATURES");
    private static final PreferencesItem CERTIFICATES_SHA1_FINGERPRINTS =
            new PreferencesItem("CERTIFICATES_SHA1_FINGERPRINTS");
    private static final PreferencesItem VITAL_DATA = new PreferencesItem("VITAL_DATA");

    private static final PreferencesItem SENT_EXTERNAL_ATTRIBUTIONS = new PreferencesItem("SENT_EXTERNAL_ATTRIBUTIONS");

    public static final String SESSION_KEY = "SESSION_";

    public PreferencesComponentDbStorage(final IKeyValueTableDbHelper dbStorage) {
        super(dbStorage);
    }

    public long getPermissionsEventSendTime() {
        return readLong(PERMISSIONS_CHECK_TIME.fullKey(), DEFAULT_PERMISSIONS_CHECK_TIME);
    }

    public int getLastAppVersionWithFeatures() {
        return readInt(LAST_APP_VERSION_WITH_FEATURES.fullKey(), DEFAULT_LAST_APP_VERSION_WITH_FEATURES);
    }

    public AppEnvironment.EnvironmentRevision getAppEnvironmentRevision() {
        synchronized (this) {
            return new AppEnvironment.EnvironmentRevision(
                    readString(APP_ENVIRONMENT.fullKey(), AppEnvironment.DEFAULT_ENVIRONMENT_JSON_STRING),
                    readLong(APP_ENVIRONMENT_REVISION.fullKey(), AppEnvironment.DEFAULT_ENVIRONMENT_REVISION)
            );
        }
    }

    public String getApplicationFeatures() {
        return readString(APPLICATION_FEATURES.fullKey(), DEFAULT_APPLICATION_FEATURES);
    }

    public PreferencesComponentDbStorage putAppEnvironmentRevision(AppEnvironment.EnvironmentRevision revision) {
        synchronized (this) {
            writeString(APP_ENVIRONMENT.fullKey(), revision.value);
            writeLong(APP_ENVIRONMENT_REVISION.fullKey(), revision.revisionNumber);
        }
        return this;
    }

    public PreferencesComponentDbStorage putPermissionsCheckTime(final long value) {
        return writeLong(PERMISSIONS_CHECK_TIME.fullKey(), value);
    }

    public PreferencesComponentDbStorage putLastAppVersionWithFeatures(int value) {
        return writeInt(LAST_APP_VERSION_WITH_FEATURES.fullKey(), value);
    }

    public PreferencesComponentDbStorage putApplicationFeatures(String value) {
        return writeString(APPLICATION_FEATURES.fullKey(), value);
    }

    public PreferencesComponentDbStorage putSessionParameters(String sessionTag, String value) {
        return writeString(new PreferencesItem(SESSION_KEY, sessionTag).fullKey(), value);
    }

    public String getSessionParameters(String sessionTag) {
        return readString(new PreferencesItem(SESSION_KEY, sessionTag).fullKey(), DEFAULT_SESSION_PARAMETERS);
    }

    @Nullable
    public String getProfileID() {
        return readString(PROFILE_ID.fullKey());
    }

    public PreferencesComponentDbStorage putProfileID(@Nullable String profileID) {
        return writeString(PROFILE_ID.fullKey(), profileID);
    }

    @NonNull
    public List<String> getCertificatesSha1Fingerprints() {
        return readStringList(CERTIFICATES_SHA1_FINGERPRINTS.fullKey(), Collections.<String>emptyList());
    }

    public PreferencesComponentDbStorage putCertificatesSha1Fingerprints(List<String> sha1s) {
        return writeStringList(CERTIFICATES_SHA1_FINGERPRINTS.fullKey(), sha1s);
    }

    @Nullable
    @Override
    public String getVitalData() {
        return readString(VITAL_DATA.fullKey(), null);
    }

    @Override
    public void putVitalData(@NonNull String data) {
        writeString(VITAL_DATA.fullKey(), data);
    }

    @NonNull
    public Map<Integer, String> getSentExternalAttributions() {
        final Map<Integer, String> result = new HashMap<>();
        try {
            final String jsonString = readString(SENT_EXTERNAL_ATTRIBUTIONS.fullKey());
            if (jsonString != null) {
                final JSONObject json = new JSONObject(jsonString);
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String key = it.next();
                    result.put(Integer.parseInt(key), json.getString(key));
                }
            }
        } catch (Throwable e) {
            YLogger.error(TAG, e);
        }
        return result;
    }

    public void putSentExternalAttributions(
        @NonNull final Map<Integer, String> data
    ) {
        final JSONObject json = new JSONObject();
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            try {
                json.put(entry.getKey().toString(), entry.getValue());
            } catch (Throwable e) {
                YLogger.error(TAG, e);
            }
        }
        writeString(SENT_EXTERNAL_ATTRIBUTIONS.fullKey(), json.toString());
    }

    @NonNull
    @Override
    protected String prepareKey(@NonNull String key) {
        return new PreferencesItem(key).fullKey();
    }

    @NonNull
    @Override
    public Set<String> keys() {
        return super.keys();
    }
}
