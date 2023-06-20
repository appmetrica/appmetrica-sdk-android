package io.appmetrica.analytics.impl.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import org.json.JSONException;

public class MeasuredJsonMap extends HashMap<String, String> {

    private int mKeysAndValuesSymbolsCount = 0;

    public MeasuredJsonMap() {
        super();
    }

    public MeasuredJsonMap(@NonNull String json) throws JSONException {
        super(JsonHelper.jsonToMapUnsafe(json));
        for (String key : keySet()) {
            final String value = get(key);
            mKeysAndValuesSymbolsCount += key.length() + (value == null ? 0 : value.length());
        }
    }

    @Override
    @Nullable
    public String put(@NonNull String name, @Nullable String value) {
        if (containsKey(name)) {
            if (value == null) {
                return remove(name);
            } else {
                final String containedValue = get(name);
                mKeysAndValuesSymbolsCount += value.length() - (containedValue == null ? 0 : containedValue.length());
                return super.put(name, value);
            }
        } else if (value != null) {
            mKeysAndValuesSymbolsCount += name.length() + value.length();
            return super.put(name, value);
        }
        return null;
    }

    @Override
    @Nullable
    public String remove(@NonNull Object key) {
        if (containsKey(key)) {
            final String containedValue = get(key);
            mKeysAndValuesSymbolsCount -= ((String) key).length() +
                    (containedValue == null ? 0 : containedValue.length());
        }
        return super.remove(key);
    }

    public int getKeysAndValuesSymbolsCount() {
        return mKeysAndValuesSymbolsCount;
    }
}
