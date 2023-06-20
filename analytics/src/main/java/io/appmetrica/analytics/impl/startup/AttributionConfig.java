package io.appmetrica.analytics.impl.startup;

import android.util.Pair;
import androidx.annotation.NonNull;
import java.util.List;

public class AttributionConfig {

    public static class Filter {

        @NonNull
        public final String value;

        public Filter(@NonNull String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @NonNull
    public final List<Pair<String, Filter>> deeplinkConditions;

    public AttributionConfig(@NonNull List<Pair<String, Filter>> deeplinkConditions) {
        this.deeplinkConditions = deeplinkConditions;
    }

    @Override
    public String toString() {
        return "AttributionConfig{" +
                "deeplinkConditions=" + deeplinkConditions +
                '}';
    }
}
