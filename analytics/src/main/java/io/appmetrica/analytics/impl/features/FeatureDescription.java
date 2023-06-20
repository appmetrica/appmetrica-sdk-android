package io.appmetrica.analytics.impl.features;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public final class FeatureDescription {

    private static final int UNKNOWN_VERSION = -1;

    private static final String NAME = "name";
    private static final String REQUIRED = "required";
    private static final String VERSION = "version";

    public static final String OPEN_GL_FEATURE = "openGlFeature";

    private final String mName;
    private final int mVersion;
    private final boolean mRequired;

    public FeatureDescription(@NonNull JSONObject object) throws JSONException {
        mName = object.getString(NAME);
        mRequired = object.getBoolean(REQUIRED);
        mVersion = object.optInt(VERSION, UNKNOWN_VERSION);
    }

    public FeatureDescription(String name, int version, boolean required) {
        mName = name;
        mVersion = version;
        mRequired = required;
    }

    public FeatureDescription(String name, boolean required) {
        this(name, UNKNOWN_VERSION, required);
    }

    public String getName() {
        return mName;
    }

    public int getVersion() {
        return mVersion;
    }

    public boolean isRequired() {
        return mRequired;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject().put(NAME, mName).put(REQUIRED, mRequired);
        if (mVersion != UNKNOWN_VERSION) {
            json.put(VERSION, mVersion);
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureDescription that = (FeatureDescription) o;

        if (mVersion != that.mVersion) return false;
        if (mRequired != that.mRequired) return false;
        return mName != null ? mName.equals(that.mName) : that.mName == null;

    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + mVersion;
        result = 31 * result + (mRequired ? 1 : 0);
        return result;
    }
}
