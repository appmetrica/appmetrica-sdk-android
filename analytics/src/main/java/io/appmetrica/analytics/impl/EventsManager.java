package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.InternalEvents;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Class for various kinds of event management.
 */
public final class EventsManager {

    private static final Set<Integer> SHOULD_USE_ERROR_ENVIRONMENT = CollectionUtils.unmodifiableSetOf(
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId(),
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId(),
        InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId(),
        InternalEvents.EVENT_TYPE_ANR.getTypeId()
    );
    private static final EnumSet<InternalEvents> SHOULD_NOT_UPDATE_APP_CONFIG = EnumSet.of
        (
            InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
            InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE,
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF
        );

    private static final EnumSet<InternalEvents> PUBLIC_FOR_LOGS = EnumSet.of(
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
        InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE,
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
            InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA,
            InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE,
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF
        );

    public static final String EVENT_OPEN_LINK_KEY = "link";
    public static final String EVENT_OPEN_TYPE_KEY = "type";
    public static final String EVENT_OPEN_AUTO_KEY = "auto";
    public static final String EVENT_OPEN_TYPE_OPEN = "open";
    public static final List<Integer> EVENTS_WITH_FIRST_HIGHEST_PRIORITY = Arrays.asList(
        InternalEvents.EVENT_TYPE_INIT.getTypeId(),
        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId(),
        InternalEvents.EVENT_TYPE_SEND_REFERRER.getTypeId(),
        InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId()
    );
    public static final List<Integer> EVENTS_WITH_SECOND_HIGHEST_PRIORITY = Arrays.asList(
        InternalEvents.EVENT_TYPE_CLEANUP.getTypeId()
    );

    // Prevent installation
    private EventsManager() {}

    public static boolean shouldUseErrorEnvironment(int eventType) {
        return SHOULD_USE_ERROR_ENVIRONMENT.contains(eventType);
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
        return !EVENTS_WITHOUT_GLOBAL_NUMBER.contains(InternalEvents.valueOf(eventType));
    }
}
