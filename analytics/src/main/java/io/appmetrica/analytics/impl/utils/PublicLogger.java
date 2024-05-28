package io.appmetrica.analytics.impl.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.logger.common.BaseReleaseLogger;

import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.Event;

public class PublicLogger extends BaseReleaseLogger {

    private static final PublicLogger ANONYMOUS_INSTANCE = new PublicLogger();

    public PublicLogger(@Nullable String fullApiKey) {
        super(SdkUtils.APPMETRICA_TAG, "[" + Utils.createPartialApiKey(fullApiKey) + "]");
    }

    public PublicLogger() {
        this(StringUtils.EMPTY);
    }

    public static PublicLogger getAnonymousInstance() {
        return ANONYMOUS_INSTANCE;
    }

    public void logEvent(final CounterReport reportData, String msg) {
        String log = PublicLogConstructor.constructCounterReportLog(reportData, msg);
        if (log != null) {
            info(log);
        }
    }

    public void logEvent(final Event event, String msg) {
        String log = PublicLogConstructor.constructEventLog(event, msg);
        if (log != null) {
            info(log);
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
