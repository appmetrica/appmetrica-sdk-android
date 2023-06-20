package io.appmetrica.analytics.impl.utils;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;

import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.Event;

public class PublicLogger extends LoggerWithApiKey {

    private static final int[] EVENTS_SUITABLE_FOR_LOGS = new int[]{
            Event.EVENT_CRASH, Event.EVENT_ERROR, Event.EVENT_CLIENT
    };

    private static final PublicLogger ANONYMOUS_INSTANCE = new PublicLogger();

    public PublicLogger(@Nullable String fullApiKey) {
        super(fullApiKey);
    }

    public PublicLogger() {
        this(StringUtils.EMPTY);
    }

    public static PublicLogger getAnonymousInstance() {
        return ANONYMOUS_INSTANCE;
    }

    @Override
    public String getTag() {
        return SdkUtils.APPMETRICA_TAG;
    }

    public void logEvent(final CounterReport reportData, String msg) {
        if (EventsManager.isPublicForLogs(reportData.getType())) {
            StringBuilder builder = new StringBuilder(msg);
            builder.append(": ");
            builder.append(reportData.getName());
            if (EventsManager.shouldLogValue(reportData.getType())
                    && TextUtils.isEmpty(reportData.getValue()) == false) {
                builder.append(" with value ");
                builder.append(reportData.getValue());
            }
            i(builder.toString());
        }
    }

    public void logEvent(final Event event, String msg) {
        if (isSuitableForLogs(event)) {
            i(msg + ": " + getLogValue(event));
        }
    }

    private boolean isSuitableForLogs(final Event event) {
        for (int type : EVENTS_SUITABLE_FOR_LOGS) {
            if (event.type == type) return true;
        }
        return false;
    }

    private String getLogValue(final Event event) {
        if (event.type == Event.EVENT_CRASH && TextUtils.isEmpty(event.name)) {
            return "Native crash of app";
        } else if (event.type == Event.EVENT_CLIENT) {
            StringBuilder logValue = new StringBuilder(event.name);
            if (event.value != null) {
                String value = new String(event.value);
                if (TextUtils.isEmpty(value) == false) {
                    logValue.append(" with value ");
                    logValue.append(value);
                }
            }
            return logValue.toString();
        } else {
            return event.name;
        }
    }

    public void logSessionEvents(@NonNull final EventProto.ReportMessage.Session session, final String msg) {
        for (Event event : session.events) {
            if (event != null) {
                logEvent(event, msg);
            }
        }
    }
}
