package io.appmetrica.analytics.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 * Class for various kinds of event management.
 */
public final class EventsManager {

    private static final Set<Integer> SHOULD_USE_ERROR_ENVIRONMENT = CollectionUtils.unmodifiableSetOf(
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT.getTypeId(),
        InternalEvents.EVENT_TYPE_ANR.getTypeId()
    );
    private static final EnumSet<InternalEvents> DO_NOT_AFFECT_SESSION_STATE = EnumSet.of
        (
            InternalEvents.EVENT_TYPE_UNDEFINED,
            InternalEvents.EVENT_TYPE_PURGE_BUFFER,
            InternalEvents.EVENT_TYPE_SEND_REFERRER,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED,
            InternalEvents.EVENT_TYPE_ACTIVATION,
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA
        );

    private static final EnumSet<InternalEvents> SHOULD_NOT_UPDATE_APP_CONFIG = EnumSet.of
        (
            InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE
        );

    private static final EnumSet<InternalEvents> PUBLIC_FOR_LOGS = EnumSet.of(
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
        InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
        InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
        InternalEvents.EVENT_TYPE_REGULAR,
        InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION,
        InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
        InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
        InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
        InternalEvents.EVENT_TYPE_PURGE_BUFFER,
        InternalEvents.EVENT_TYPE_INIT,
        InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
        InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID,
        InternalEvents.EVENT_TYPE_SEND_REFERRER,
        InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED,
        InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED,
        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
        InternalEvents.EVENT_TYPE_START,
        InternalEvents.EVENT_TYPE_APP_OPEN,
        InternalEvents.EVENT_TYPE_APP_UPDATE,
        InternalEvents.EVENT_TYPE_ANR
    );

    private static final EnumSet<InternalEvents> LOG_EVENT_VALUE = EnumSet.of(InternalEvents.EVENT_TYPE_REGULAR);

    private static final EnumSet<InternalEvents> LOG_EVENT_NAME = EnumSet.of(
        InternalEvents.EVENT_TYPE_REGULAR
    );

    private static final EnumSet<InternalEvents> EVENTS_WITHOUT_GLOBAL_NUMBER = EnumSet.of(
        InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF
    );

    private static final EnumSet<InternalEvents> SHOULD_NOT_APPLY_MODULE_HANDLERS =
        EnumSet.of(
            InternalEvents.EVENT_TYPE_ALIVE,
            InternalEvents.EVENT_TYPE_PURGE_BUFFER,
            InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA
        );

    public static final String EVENT_OPEN_LINK_KEY = "link";
    public static final String EVENT_OPEN_TYPE_KEY = "type";
    public static final String EVENT_OPEN_AUTO_KEY = "auto";
    public static final String EVENT_OPEN_TYPE_OPEN = "open";
    public static final String EVENT_OPEN_TYPE_REFERRAL = "referral";
    public static final List<Integer> EVENTS_WITH_FIRST_HIGHEST_PRIORITY = Arrays.asList(
        InternalEvents.EVENT_TYPE_INIT.getTypeId(),
        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId(),
        InternalEvents.EVENT_TYPE_SEND_REFERRER.getTypeId(),
        InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId()
    );
    public static final List<Integer> EVENTS_WITH_SECOND_HIGHEST_PRIORITY = Arrays.asList(
        InternalEvents.EVENT_TYPE_CLEANUP.getTypeId()
    );

    public static final String PAYLOAD_CRASH_ID = "payload_crash_id";

    // Prevent installation
    private EventsManager() {}

    public static boolean shouldUseErrorEnvironment(int eventType) {
        return SHOULD_USE_ERROR_ENVIRONMENT.contains(eventType);
    }

    public static boolean affectSessionState(InternalEvents eventType) {
        return !DO_NOT_AFFECT_SESSION_STATE.contains(eventType);
    }

    public static boolean isEventWithoutAppConfigUpdate(final int typeID) {
        return SHOULD_NOT_UPDATE_APP_CONFIG.contains(InternalEvents.valueOf(typeID));
    }

    public static boolean shouldApplyModuleHandlers(@NonNull InternalEvents eventType) {
        return !SHOULD_NOT_APPLY_MODULE_HANDLERS.contains(eventType);
    }

    public static boolean isPublicForLogs(int event) {
        return PUBLIC_FOR_LOGS.contains(InternalEvents.valueOf(event));
    }

    public static boolean shouldLogName(InternalEvents eventType) {
        return LOG_EVENT_NAME.contains(eventType);
    }

    public static boolean shouldLogValue(InternalEvents event) {
        return LOG_EVENT_VALUE.contains(event);
    }

    public static boolean shouldGenerateGlobalNumber(int eventType) {
        return EVENTS_WITHOUT_GLOBAL_NUMBER.contains(InternalEvents.valueOf(eventType)) == false;
    }

    public static CounterReport currentSessionNativeCrashEntry(@NonNull String nativeCrash,
                                                               @NonNull String uuid,
                                                               @NonNull PublicLogger logger) {
        return nativeCrashEntry(
            InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
            nativeCrash,
            uuid,
            logger
        );
    }

    public static CounterReport prevSessionNativeCrashEntry(@NonNull String nativeCrash,
                                                            @NonNull String uuid,
                                                            @NonNull PublicLogger logger) {
        return nativeCrashEntry(
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            nativeCrash,
            uuid,
            logger
        );
    }

    private static CounterReport nativeCrashEntry(@NonNull InternalEvents eventType,
                                                  @NonNull String nativeCrash,
                                                  @NonNull String uuid,
                                                  @NonNull PublicLogger logger) {
        Bundle payload = new Bundle();
        payload.putString(PAYLOAD_CRASH_ID, uuid);
        CounterReport counterReport = nativeCrashEntry(
            nativeCrash,
            eventType,
            logger
        );
        counterReport.setPayload(payload);
        return counterReport;
    }

    private static CounterReport nativeCrashEntry(@Nullable String nativeCrash,
                                                  @NonNull InternalEvents eventType,
                                                  @NonNull PublicLogger logger) {
        ClientCounterReport report = new ClientCounterReport("", eventType.getTypeId(), logger);
        if (nativeCrash != null) {
            report.withExtendedValue(nativeCrash);
        }
        return report;
    }

    public static CounterReport reportEntry(InternalEvents eventType, @NonNull PublicLogger logger) {
        return new ClientCounterReport("", eventType.getTypeId(), logger);
    }

    public static CounterReport regularEventReportEntry(String eventName, @NonNull PublicLogger logger) {
        return new ClientCounterReport(eventName, InternalEvents.EVENT_TYPE_REGULAR.getTypeId(), logger);
    }

    static CounterReport regularEventReportEntry(String eventName, String extraData, @NonNull PublicLogger logger) {
        return new ClientCounterReport(extraData, eventName, InternalEvents.EVENT_TYPE_REGULAR.getTypeId(), logger);
    }

    static CounterReport anrEntry(byte[] value, @NonNull PublicLogger publicLogger) {
        return new ClientCounterReport(
            value,
            "",
            InternalEvents.EVENT_TYPE_ANR.getTypeId(),
            publicLogger
        );
    }

    static CounterReport regularErrorReportEntry(
        @Nullable String eventName,
        byte[] extraData,
        @NonNull PublicLogger logger
    ) {
        return new ClientCounterReport(extraData, eventName,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF.getTypeId(), logger);
    }

    static CounterReport customErrorReportEntry(
        @Nullable String eventName,
        @NonNull byte[] extraData,
        @NonNull PublicLogger logger
    ) {
        return new ClientCounterReport(extraData, eventName,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.getTypeId(), logger);
    }

    static CounterReport notifyServiceOnActivityStartReportEntry(String eventName, @NonNull PublicLogger logger) {
        return new ClientCounterReport(eventName, InternalEvents.EVENT_TYPE_START.getTypeId(), logger);
    }

    static CounterReport activityEndReportEntry(String eventName, @NonNull PublicLogger logger) {
        return new ClientCounterReport(eventName, InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME.getTypeId(), logger);
    }

    public static ClientCounterReport unhandledExceptionReportEntry(String eventName,
                                                                    byte[] value,
                                                                    @NonNull PublicLogger logger) {
        return unhandledExceptionReportEntry(
            value,
            eventName,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
            logger
        );
    }

    public static CounterReport unhandledExceptionFromFileReportEntry(
        String eventName,
        byte[] value,
        int bytesTruncated,
        @NonNull HashMap<ClientCounterReport.TrimmedField, Integer> trimmedFields,
        @Nullable String errorEnvironment,
        @NonNull PublicLogger logger) {
        ClientCounterReport result = unhandledExceptionReportEntry(
            value,
            eventName,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
            logger
        );
        result.withTrimmedFields(trimmedFields);
        result.setBytesTruncated(bytesTruncated);
        result.setEventEnvironment(errorEnvironment);

        return result;
    }

    private static ClientCounterReport unhandledExceptionReportEntry(byte[] value,
                                                                     String eventName,
                                                                     InternalEvents type,
                                                                     @NonNull PublicLogger logger) {
        return new ClientCounterReport(value, eventName, type.getTypeId(), logger);
    }

    public static CounterReport requestReferrerEntry(@NonNull PublicLogger logger) {
        return new ClientCounterReport(StringUtils.EMPTY, StringUtils.EMPTY,
            InternalEvents.EVENT_TYPE_REQUEST_REFERRER.getTypeId(), logger);
    }

    static CounterReport openAppReportEntry(final String value, boolean auto, @NonNull PublicLogger logger) {
        return eventOpenEntry(EVENT_OPEN_TYPE_OPEN, value, auto, logger);
    }

    static CounterReport referralUrlReportEntry(final String value, @NonNull PublicLogger logger) {
        return eventOpenEntry(EVENT_OPEN_TYPE_REFERRAL, value, false, logger);
    }

    static CounterReport eventOpenEntry(final String type,
                                        final String value,
                                        final boolean auto,
                                        @NonNull PublicLogger logger) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(EVENT_OPEN_TYPE_KEY, type);
        map.put(EVENT_OPEN_LINK_KEY, value);
        map.put(EVENT_OPEN_AUTO_KEY, auto);
        return new ClientCounterReport(JsonHelper.mapToJsonString(map), StringUtils.EMPTY,
            InternalEvents.EVENT_TYPE_APP_OPEN.getTypeId(), logger);
    }

    public static CounterReport activationEventReportEntry(@Nullable PreloadInfoWrapper preloadInfo,
                                                           @Nullable String userProfileID,
                                                           @NonNull PublicLogger logger) {
        JSONObject activationEventValue = new JSONObject();
        if (preloadInfo != null) {
            preloadInfo.addToEventValue(activationEventValue);
        }
        CounterReport counterReport = new ClientCounterReport(
            activationEventValue.toString(),
            "",
            InternalEvents.EVENT_TYPE_ACTIVATION.getTypeId(),
            logger
        );
        counterReport.setProfileID(userProfileID);
        return counterReport;
    }

    public static CounterReport cleanupEventReportEntry(@NonNull String value, @NonNull PublicLogger logger) {
        return new ClientCounterReport(value, "", InternalEvents.EVENT_TYPE_CLEANUP.getTypeId(), logger);
    }

    static CounterReport customEventReportEntry(int type,
                                                @Nullable String name,
                                                @Nullable String value,
                                                @Nullable final Map<String, Object> environment,
                                                @Nullable Map<String, byte[]> extras,
                                                @NonNull PublicLogger logger) {
        CounterReport report =
            new ClientCounterReport(value, name, InternalEvents.EVENT_TYPE_CUSTOM_EVENT.getTypeId(), type, logger);
        report.setEventEnvironment(JsonHelper.mapToJsonString(environment));
        if (extras != null) {
            report.setExtras(extras);
        }

        return report;
    }

    static CounterReport setSessionExtraReportEntry(@NonNull String key,
                                                    @Nullable byte[] value,
                                                    @NonNull PublicLogger publicLogger) {

        CounterReport counterReport =
            new ClientCounterReport(null, InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA.getTypeId(), publicLogger);
        counterReport.setExtras(Collections.singletonMap(key, value == null ? new byte[0] : value));

        return counterReport;
    }

    public static CounterReport clientExternalAttributionEntry(
        byte[] value,
        @NonNull PublicLogger publicLogger
    ) {
        return new ClientCounterReport(
            value,
            "",
            InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.getTypeId(),
            publicLogger
        );
    }
}
