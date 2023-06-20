package io.appmetrica.analytics.impl.profile;

import android.util.SparseArray;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public class TypesToNameMapping {

    private static SparseArray<String> sTypesToName = new SparseArray<String>();

    static {
        sTypesToName.put(Userprofile.Profile.Attribute.STRING, "String");
        sTypesToName.put(Userprofile.Profile.Attribute.NUMBER, "Number");
        sTypesToName.put(Userprofile.Profile.Attribute.COUNTER, "Counter");
    }

    static String getTypeName(int type) {
        return sTypesToName.get(type);
    }

}
