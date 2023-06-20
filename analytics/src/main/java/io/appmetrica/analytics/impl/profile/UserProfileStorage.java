package io.appmetrica.analytics.impl.profile;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile.Profile.Attribute;
import java.util.ArrayList;
import java.util.HashMap;

public class UserProfileStorage {

    private static final int[] CUSTOM_ATTRIBUTE_TYPES = new int[] {
            Attribute.STRING,
            Attribute.NUMBER,
            Attribute.COUNTER,
            Attribute.BOOL
    };

    private final SparseArray<HashMap<String, Attribute>> mAttributesNamespaces
            = new SparseArray<HashMap<String, Attribute>>();

    private int mLimitedAttributeCount = 0;

    public UserProfileStorage() {
        this(CUSTOM_ATTRIBUTE_TYPES);
    }

    @VisibleForTesting
    UserProfileStorage(int[] types) {
        for (int type : types) {
            mAttributesNamespaces.put(type, new HashMap<String, Attribute>());
        }
    }

    @VisibleForTesting
    SparseArray<?> getAttributesNamespaces() {
        return mAttributesNamespaces;
    }

    @Nullable
    public Attribute get(int type, @NonNull String key) {
        return mAttributesNamespaces.get(type).get(key);
    }

    void put(@NonNull Attribute attribute) {
        mAttributesNamespaces.get(attribute.type).put(new String(attribute.name), attribute);
    }

    public int getLimitedAttributeCount() {
        return mLimitedAttributeCount;
    }

    public void incrementLimitedAttributeCount() {
        mLimitedAttributeCount++;
    }

    @NonNull
    public Userprofile.Profile toProtobuf() {
        Userprofile.Profile profile = new Userprofile.Profile();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < mAttributesNamespaces.size(); i++) {
            HashMap<String, Attribute> namespace = mAttributesNamespaces.get(mAttributesNamespaces.keyAt(i));
            for (Attribute attribute: namespace.values()) {
                attributes.add(attribute);
            }
        }
        profile.attributes = attributes.toArray(new Attribute[attributes.size()]);
        return profile;
    }
}
