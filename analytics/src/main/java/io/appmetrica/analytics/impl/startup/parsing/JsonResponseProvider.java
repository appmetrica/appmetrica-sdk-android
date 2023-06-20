package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.utils.JsonHelper;

public class JsonResponseProvider {

    @NonNull
    public JsonHelper.OptJSONObject jsonFromBytes(@NonNull byte[] raw) throws Exception {
        return new JsonHelper.OptJSONObject(new String(raw, IOUtils.UTF8_ENCODING));
    }
}
