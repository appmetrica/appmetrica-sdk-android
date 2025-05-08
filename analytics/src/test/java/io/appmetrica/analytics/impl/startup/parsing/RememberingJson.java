package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

class RememberingJson extends JsonHelper.OptJSONObject {

    private final List<String> requestedKeys = new ArrayList<String>();

    @Nullable
    @Override
    public Object opt(@Nullable String name) {
        requestedKeys.add(name);
        return super.opt(name);
    }

    @Override
    public Object get(String name, Object defValue) {
        requestedKeys.add(name);
        return super.get(name, defValue);
    }

    @NonNull
    @Override
    public Object get(@NonNull String name) throws JSONException {
        requestedKeys.add(name);
        return super.get(name);
    }

    public List<String> getRequestedKeys() {
        return requestedKeys;
    }
}
