package io.appmetrica.analytics.impl.utils;

import org.json.JSONException;

public interface JSONable {

    String toJSONString() throws JSONException;

}
