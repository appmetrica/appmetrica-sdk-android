package io.appmetrica.analytics.impl.id.reflection;

import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String PROVIDER = "io.appmetrica.analytics.identifiers.extra.PROVIDER";
    public static final String ID = "io.appmetrica.analytics.identifiers.extra.ID";
    public static final String LIMITED = "io.appmetrica.analytics.identifiers.extra.LIMITED";

    public static final String TRACKING_INFO = "io.appmetrica.analytics.identifiers.extra.TRACKING_INFO";
    public static final String STATUS = "io.appmetrica.analytics.identifiers.extra.STATUS";
    public static final String ERROR_MESSAGE = "io.appmetrica.analytics.identifiers.extra.ERROR_MESSAGE";

    public static class Providers {
        public static final String GOOGLE = "google";
        public static final String HUAWEI = "huawei";
        public static final String YANDEX = "yandex";
    }

    public static final Map<String, AdTrackingInfo.Provider> PROVIDER_MAP;

    static {
        HashMap<String, AdTrackingInfo.Provider> map = new HashMap<>();
        map.put(Providers.GOOGLE, AdTrackingInfo.Provider.GOOGLE);
        map.put(Providers.HUAWEI, AdTrackingInfo.Provider.HMS);
        map.put(Providers.YANDEX, AdTrackingInfo.Provider.YANDEX);
        PROVIDER_MAP = Collections.unmodifiableMap(map);
    }
}
