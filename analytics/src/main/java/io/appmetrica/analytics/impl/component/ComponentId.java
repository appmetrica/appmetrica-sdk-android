package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.Utils;

public class ComponentId {

    // Delimiter between package & apiKey
    // CAUTION: Don't change this value!
    static final String DELIMITER = "_";

    //todo (avitenko) inverse hierarchy, because some successors reduce functionality and do not use apiKey
    // https://nda.ya.ru/t/kB0DViHI6Njj6L
    @NonNull private final String mPackageName;
    @Nullable private final String mApiKey;

    public ComponentId(@NonNull final String packageName, @Nullable final String apiKey) {
        mPackageName = packageName;
        mApiKey = apiKey;
    }

    @Nullable
    public String getApiKey() {
        return mApiKey;
    }

    public boolean isMain() {
        return false;
    }

    public String getPackage() {
        return mPackageName;
    }

    @Override
    public String toString() {
        return mPackageName + DELIMITER + mApiKey;
    }

    public String toStringAnonymized() {
        return mPackageName + DELIMITER + Utils.createPartialApiKey(mApiKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentId that = (ComponentId) o;

        if (mPackageName != null ? !mPackageName.equals(that.mPackageName) : that.mPackageName != null)
            return false;
        return mApiKey != null ? mApiKey.equals(that.mApiKey) : that.mApiKey == null;
    }

    @Override
    public int hashCode() {
        int result = mPackageName != null ? mPackageName.hashCode() : 0;
        result = 31 * result + (mApiKey != null ? mApiKey.hashCode() : 0);
        return result;
    }
}
