package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.networktasks.impl.utils.TimeUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RequestDataHolder {

    @NonNull
    private NetworkTask.Method method = NetworkTask.Method.GET;
    @NonNull
    private final Map<String, List<String>> headers = new HashMap<>();
    @Nullable
    private byte[] postData = null;
    @Nullable
    private Long sendTimestamp;
    @Nullable
    private Integer sendTimezoneSec;

    @Nullable
    public byte[] getPostData() {
        return postData;
    }

    public void setPostData(@Nullable byte[] data) {
        method = NetworkTask.Method.POST;
        postData = data;
    }

    @NonNull
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeader(@NonNull String name, @NonNull String... values) {
        headers.put(name, Arrays.asList(values));
    }

    @NonNull
    public NetworkTask.Method getMethod() {
        return method;
    }

    public void applySendTime(final long timestamp) {
        sendTimestamp = timestamp;
        sendTimezoneSec = TimeUtils.getTimeZoneOffsetSec(TimeUnit.MILLISECONDS.toSeconds(timestamp));
    }

    @Nullable
    public Long getSendTimestamp() {
        return sendTimestamp;
    }

    @Nullable
    public Integer getSendTimezoneSec() {
        return sendTimezoneSec;
    }
}
