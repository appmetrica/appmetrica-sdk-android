package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONObject;
import java.util.Map;
import io.appmetrica.analytics.impl.attribution.ExternalAttributionType;
import io.appmetrica.analytics.impl.attribution.JSONObjectExternalAttribution;
import io.appmetrica.analytics.impl.attribution.MapExternalAttribution;
import io.appmetrica.analytics.impl.attribution.NullExternalAttribution;
import io.appmetrica.analytics.impl.attribution.ObjectExternalAttribution;

/**
 * Class with implementations of {@link ExternalAttribution} interface.
 * Use it with {@link AppMetrica#reportExternalAttribution(ExternalAttribution)} method.
 */
public final class ExternalAttributions {

    private ExternalAttributions() {}

    /**
     * Creates AppsFlyer implementation of {@link ExternalAttribution} interface.
     *
     * @param value data from AppsFlyer library
     * @return AppsFlyer implementation of {@link ExternalAttribution} interface
     */
    @NonNull
    public static ExternalAttribution appsflyer(
        @Nullable final Map<String, Object> value
    ) {
        if (value == null) {
            return new NullExternalAttribution(ExternalAttributionType.APPSFLYER);
        }
        return new MapExternalAttribution(ExternalAttributionType.APPSFLYER, value);
    }

    /**
     * Creates Adjust implementation of {@link ExternalAttribution} interface.
     *
     * @param value data from Adjust library
     * @return Adjust implementation of {@link ExternalAttribution} interface
     */
    @NonNull
    public static ExternalAttribution adjust(
        @Nullable final Object value
    ) {
        if (value == null) {
            return new NullExternalAttribution(ExternalAttributionType.ADJUST);
        }
        return new ObjectExternalAttribution(ExternalAttributionType.ADJUST, value);
    }

    /**
     * Creates Kochava implementation of {@link ExternalAttribution} interface.
     *
     * @param value data from Kochava library
     * @return Kochava implementation of {@link ExternalAttribution} interface
     */
    @NonNull
    public static ExternalAttribution kochava(
        @Nullable final JSONObject value
    ) {
        if (value == null) {
            return new NullExternalAttribution(ExternalAttributionType.KOCHAVA);
        }
        return new JSONObjectExternalAttribution(ExternalAttributionType.KOCHAVA, value);
    }

    /**
     * Creates Tenjin implementation of {@link ExternalAttribution} interface.
     *
     * @param value data from Tenjin library
     * @return Tenjin implementation of {@link ExternalAttribution} interface
     */
    @NonNull
    public static ExternalAttribution tenjin(
        @Nullable final Map<String, String> value
    ) {
        if (value == null) {
            return new NullExternalAttribution(ExternalAttributionType.TENJIN);
        }
        return new MapExternalAttribution(ExternalAttributionType.TENJIN, value);
    }

    /**
     * Creates AirBridge implementation of {@link ExternalAttribution} interface.
     *
     * @param value data from AirBridge library
     * @return AirBridge implementation of {@link ExternalAttribution} interface
     */
    @NonNull
    public static ExternalAttribution airbridge(
        @Nullable final Map<String, String> value
    ) {
        if (value == null) {
            return new NullExternalAttribution(ExternalAttributionType.AIRBRIDGE);
        }
        return new MapExternalAttribution(ExternalAttributionType.AIRBRIDGE, value);
    }
}
