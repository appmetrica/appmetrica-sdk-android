package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import java.util.List;
import java.util.Map;

public interface IStartupUnit {

    void onRequestComplete(@NonNull StartupResult result,
                           @NonNull StartupRequestConfig requestConfig,
                           @Nullable Map<String, List<String>> responseHeaders);

    void onRequestError(@NonNull StartupError cause);
}
