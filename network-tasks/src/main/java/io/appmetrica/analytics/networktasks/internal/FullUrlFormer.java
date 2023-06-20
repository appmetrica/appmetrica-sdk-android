package io.appmetrica.analytics.networktasks.internal;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.networktasks.impl.ForcedHttpsUrlProvider;
import java.util.ArrayList;
import java.util.List;

public class FullUrlFormer<T> {

    private static final String TAG = "[FullUrlFormer]";

    @NonNull
    private List<String> hosts = new ArrayList<>();
    private int attemptNumber = -1;
    @Nullable
    private String url;
    @NonNull
    private final IParamsAppender<T> paramsAppender;
    @NonNull
    private final ConfigProvider<T> configProvider;

    public FullUrlFormer(@NonNull IParamsAppender<T> paramsAppender,
                         @NonNull ConfigProvider<T> configProvider) {
        this.paramsAppender = paramsAppender;
        this.configProvider = configProvider;
    }

    @Nullable
    public List<String> getAllHosts() {
        return hosts;
    }

    public void setHosts(@Nullable List<String> hosts) {
        YLogger.info(TAG, "set hosts to %s", hosts);
        this.hosts = hosts == null ? new ArrayList<String>() : hosts;
    }

    public void incrementAttemptNumber() {
        attemptNumber++;
        YLogger.info(TAG, "increment attempt number to %d", attemptNumber);
    }

    @NonNull
    private String getCurrentHost() {
        return hosts.get(attemptNumber);
    }

    public boolean hasMoreHosts() {
        return attemptNumber + 1 < hosts.size();
    }

    public void buildAndSetFullHostUrl() {
        final Uri.Builder uriBuilder = Uri.parse(getCurrentHost()).buildUpon();
        T config = configProvider.getConfig();
        paramsAppender.appendParams(uriBuilder, config);
        String url = uriBuilder.build().toString();
        YLogger.info(TAG, "Full url list: %s, attemptNumber: %d", hosts, attemptNumber);
        YLogger.info(TAG, "Request full url: %s", url);
        this.url = url;
    }

    @Nullable
    public String getUrl() {
        return new ForcedHttpsUrlProvider(url).getUrl();
    }
}
