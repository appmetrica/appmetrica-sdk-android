package io.appmetrica.analytics.networktasks.impl;

import java.util.concurrent.TimeUnit;

public class Constants {

    public static final String STATUS_ACCEPTED = "accepted";

    public static final class Config {
        public static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
        public static final String TYPE_JSON = "application/json";
        public static final String ENCODING_ENCRYPTED = "encrypted";
        public static final String GZIP = "gzip";
    }

    public static final class Headers {
        public static final String ACCEPT = "Accept";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String USER_AGENT = "User-Agent";
        public static final String SEND_TIMESTAMP = "Send-Timestamp";
        public static final String SEND_TIMEZONE = "Send-Timezone";
    }
}
