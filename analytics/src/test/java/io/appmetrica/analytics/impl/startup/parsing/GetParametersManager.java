package io.appmetrica.analytics.impl.startup.parsing;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Condition;

public class GetParametersManager {

    private final Map<String, Condition<String>> keysToValue = new HashMap<String, Condition<String>>();
    private final Map<String, String> responseToParameter = new HashMap<String, String>();
    private final List<String> notParameters = Arrays.asList("features");

    public GetParametersManager() {
        keysToValue.put("deviceid", any());
        keysToValue.put("query_hosts", equal("2"));

        responseToParameter.put("device_id", "deviceid");
        responseToParameter.put("locale", "detect_locale");
    }

    public List<String> getBlocks(@NonNull Uri uri) {
        System.out.println(uri);
        List<String> requestedBlocks = new ArrayList<String>();
        for (String key : uri.getQueryParameterNames()) {
            if (isBlock(key, uri.getQueryParameter(key))) {
                requestedBlocks.add(key);
            }
        }
        return requestedBlocks;
    }

    public List<String> transformToParameters(List<String> parsedBlocks) {
        List<String> parameters = new ArrayList<String>();
        for (String response : parsedBlocks) {
            String parameter = responseToParameter.containsKey(response) ? responseToParameter.get(response) : response;
            if (!notParameters.contains(parameter)) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    private boolean isBlock(String key, @Nullable String value) {
        return getExpectedValue(key).matches(value);
    }

    private Condition<String> getExpectedValue(String key) {
        return keysToValue.containsKey(key) ? keysToValue.get(key) : equal("1");
    }

    private Condition<String> equal(final String expected) {
        return new Condition<String>() {
            @Override
            public boolean matches(String value) {
                return expected.equals(value);
            }
        };
    }

    private Condition<String> any() {
        return new Condition<String>() {
            @Override
            public boolean matches(String value) {
                return true;
            }
        };
    }
}
