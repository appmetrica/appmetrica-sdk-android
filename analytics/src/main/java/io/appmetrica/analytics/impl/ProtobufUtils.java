package io.appmetrica.analytics.impl;

import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.preparer.BytesValueComposer;
import io.appmetrica.analytics.impl.preparer.DummyLocationInfoComposer;
import io.appmetrica.analytics.impl.preparer.DummyNetworkInfoComposer;
import io.appmetrica.analytics.impl.preparer.EmptyNameComposer;
import io.appmetrica.analytics.impl.preparer.EmptyValueComposer;
import io.appmetrica.analytics.impl.preparer.EncryptedStringValueComposer;
import io.appmetrica.analytics.impl.preparer.EventFromDbModel;
import io.appmetrica.analytics.impl.preparer.EventPreparer;
import io.appmetrica.analytics.impl.preparer.EventTypeComposer;
import io.appmetrica.analytics.impl.preparer.NameComposer;
import io.appmetrica.analytics.impl.preparer.ProtobufNativeCrashComposer;
import io.appmetrica.analytics.impl.preparer.UnGzipBytesValueComposer;
import io.appmetrica.analytics.impl.preparer.ValueComposer;
import io.appmetrica.analytics.impl.preparer.ValueWithPreloadInfoComposer;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.protobuf.backend.Referrer;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.telephony.SimInfo;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage;
import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session;
import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc;

public final class ProtobufUtils {

    private static final String TAG = "[ProtobufUtils]";

    private ProtobufUtils() {
    }

    private static Map<SessionType, Integer> INTERNAL_TO_PROTOBUF_SESSION_TYPES_MAPPING;
    private static SparseArray<SessionType> PROTOBUF_TO_INTERNAL_SESSION_TYPES_MAPPING;
    private static final Map<InternalEvents, Integer> INTERNAL_TO_PROTOBUF_TYPES_MAPPING;
    private static final Map<InternalEvents, EventPreparer> INTERNAL_TO_PROTOBUF_EVENTS_MAPPING;

    static {
        HashMap<SessionType, Integer> internalSessionTypeToProto = new HashMap<SessionType, Integer>();
        internalSessionTypeToProto.put(SessionType.FOREGROUND, SessionDesc.SESSION_FOREGROUND);
        internalSessionTypeToProto.put(SessionType.BACKGROUND, SessionDesc.SESSION_BACKGROUND);
        INTERNAL_TO_PROTOBUF_SESSION_TYPES_MAPPING = Collections.unmodifiableMap(internalSessionTypeToProto);

        SparseArray<SessionType> protoSessionTypeToInternal = new SparseArray<SessionType>();
        protoSessionTypeToInternal.put(SessionDesc.SESSION_FOREGROUND, SessionType.FOREGROUND);
        protoSessionTypeToInternal.put(SessionDesc.SESSION_BACKGROUND, SessionType.BACKGROUND);
        PROTOBUF_TO_INTERNAL_SESSION_TYPES_MAPPING = protoSessionTypeToInternal;

        Map<InternalEvents, Integer> typesMapping = new HashMap<InternalEvents, Integer>();
        typesMapping.put(
                InternalEvents.EVENT_TYPE_INIT,
                EventProto.ReportMessage.Session.Event.EVENT_INIT
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_REGULAR,
                EventProto.ReportMessage.Session.Event.EVENT_CLIENT
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_SEND_REFERRER,
                EventProto.ReportMessage.Session.Event.EVENT_REFERRER
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_ALIVE,
                EventProto.ReportMessage.Session.Event.EVENT_ALIVE
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
                EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
                EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
                EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_ANR,
                EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ANR
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
                Session.Event.EVENT_PROTOBUF_CRASH
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
                Session.Event.EVENT_PROTOBUF_CRASH
        );

        typesMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
                EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ERROR
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
                EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ERROR
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
                EventProto.ReportMessage.Session.Event.EVENT_FIRST
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_START,
                EventProto.ReportMessage.Session.Event.EVENT_START
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_APP_OPEN,
                EventProto.ReportMessage.Session.Event.EVENT_OPEN
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_APP_UPDATE,
                EventProto.ReportMessage.Session.Event.EVENT_UPDATE
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_PERMISSIONS,
                EventProto.ReportMessage.Session.Event.EVENT_PERMISSIONS
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_APP_FEATURES,
                EventProto.ReportMessage.Session.Event.EVENT_APP_FEATURES
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
                EventProto.ReportMessage.Session.Event.EVENT_PROFILE
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
                EventProto.ReportMessage.Session.Event.EVENT_REVENUE
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
                EventProto.ReportMessage.Session.Event.EVENT_AD_REVENUE
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
                Session.Event.EVENT_ECOMMERCE
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_CLEANUP,
                EventProto.ReportMessage.Session.Event.EVENT_CLEANUP
        );
        typesMapping.put(
                InternalEvents.EVENT_TYPE_WEBVIEW_SYNC,
                Session.Event.EVENT_WEBVIEW_SYNC
        );
        typesMapping.put(
            InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION,
            Session.Event.EVENT_CLIENT_EXTERNAL_ATTRIBUTION
        );
        INTERNAL_TO_PROTOBUF_TYPES_MAPPING = Collections.unmodifiableMap(typesMapping);

        Map<InternalEvents, EventPreparer> eventsMapping = new HashMap<InternalEvents, EventPreparer>();
        NameComposer emptyNameComposer = new EmptyNameComposer();
        ValueComposer encryptedValueComposer = new EncryptedStringValueComposer();
        ValueComposer emptyValueComposer = new EmptyValueComposer();
        ValueComposer base64DecodedValueComposer = new BytesValueComposer();
        ValueComposer unGzippedBase64DecodedValueComposer = new UnGzipBytesValueComposer();
        ProtobufNativeCrashComposer protobufNativeCrashComposer = new ProtobufNativeCrashComposer();
        EventPreparer nativeCrashPreparer = EventPreparer.builderWithDefaults()
                .withValueComposer(protobufNativeCrashComposer)
                .withEncodingTypeProvider(protobufNativeCrashComposer)
                .build();
        EventPreparer preparerWithEncryptedValue =
                EventPreparer.builderWithDefaults().withValueComposer(encryptedValueComposer).build();
        EventPreparer preparerWithBase64DecodedValue =
                EventPreparer.builderWithDefaults().withValueComposer(base64DecodedValueComposer).build();
        EventPreparer preparerWithUnGzippedBase64DecodedValue = EventPreparer.builderWithDefaults()
                .withValueComposer(unGzippedBase64DecodedValueComposer).build();
        EventPreparer preparerWithoutName =
                EventPreparer.builderWithDefaults().withNameComposer(emptyNameComposer).build();
        EventPreparer preparerWithPreloadInfoHandling = EventPreparer.builderWithDefaults()
                .withValueComposer(new ValueWithPreloadInfoComposer()).build();
        eventsMapping.put(InternalEvents.EVENT_TYPE_REGULAR, preparerWithEncryptedValue);
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_SEND_REFERRER,
                EventPreparer.builderWithDefaults().withValueComposer(new ValueComposer() {
                    @NonNull
                    @Override
                    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
                        if (TextUtils.isEmpty(event.getValue()) == false) {
                            try {
                                ReferrerInfo info = ReferrerInfo.parseFrom(Base64.decode(event.getValue(), 0));
                                Referrer referrer = new Referrer();
                                referrer.referrer = info.installReferrer == null ?
                                        new byte[]{} : info.installReferrer.getBytes();
                                referrer.clickTimestamp = info.referrerClickTimestampSeconds;
                                referrer.installBeginTimestamp = info.installBeginTimestampSeconds;
                                referrer.source = sourceToProto(info.source);
                                return MessageNano.toByteArray(referrer);
                            } catch (Throwable e) {
                                DebugLogger.INSTANCE.error(
                                    TAG,
                                    e,
                                    "Something went wrong while serializing referrer event."
                                );
                            }
                        }
                        return new byte[0];
                    }
                }).build()
        );
        eventsMapping.put(
            InternalEvents.EVENT_TYPE_ALIVE,
            EventPreparer.builderWithDefaults()
                .withNameComposer(emptyNameComposer)
                .withValueComposer(emptyValueComposer)
                .withLocationInfoComposer(new DummyLocationInfoComposer())
                .withNetworkInfoComposer(new DummyNetworkInfoComposer())
                .build()
        );
        eventsMapping.put(InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF, nativeCrashPreparer);
        eventsMapping.put(InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF, nativeCrashPreparer);
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
                preparerWithBase64DecodedValue
        );
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
                preparerWithBase64DecodedValue
        );
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
                preparerWithBase64DecodedValue
        );
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
                preparerWithBase64DecodedValue
        );
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
                preparerWithBase64DecodedValue
        );
        eventsMapping.put(InternalEvents.EVENT_TYPE_ANR, preparerWithBase64DecodedValue);
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_START,
                EventPreparer.builderWithDefaults()
                        .withNameComposer(new EmptyNameComposer())
                        .withValueComposer(base64DecodedValueComposer)
                        .build()
        );
        eventsMapping.put(
                InternalEvents.EVENT_TYPE_CUSTOM_EVENT,
                EventPreparer.builderWithDefaults().withEventTypeComposer(new EventTypeComposer() {
                    @Nullable
                    @Override
                    public Integer getEventType(@NonNull EventFromDbModel event) {
                        return event.getCustomType();
                    }
                }).build()
        );
        eventsMapping.put(InternalEvents.EVENT_TYPE_APP_OPEN, preparerWithEncryptedValue);
        eventsMapping.put(InternalEvents.EVENT_TYPE_PERMISSIONS, preparerWithoutName);
        eventsMapping.put(InternalEvents.EVENT_TYPE_APP_FEATURES, preparerWithoutName);
        eventsMapping.put(InternalEvents.EVENT_TYPE_SEND_USER_PROFILE, preparerWithBase64DecodedValue);
        eventsMapping.put(InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT, preparerWithBase64DecodedValue);
        eventsMapping.put(InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT, preparerWithBase64DecodedValue);
        eventsMapping.put(InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT, preparerWithUnGzippedBase64DecodedValue);
        eventsMapping.put(InternalEvents.EVENT_TYPE_INIT, preparerWithPreloadInfoHandling);
        eventsMapping.put(InternalEvents.EVENT_TYPE_APP_UPDATE, preparerWithPreloadInfoHandling);
        eventsMapping.put(InternalEvents.EVENT_TYPE_FIRST_ACTIVATION, preparerWithEncryptedValue);
        eventsMapping.put(InternalEvents.EVENT_TYPE_WEBVIEW_SYNC, preparerWithEncryptedValue);
        eventsMapping.put(InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION, preparerWithBase64DecodedValue);
        INTERNAL_TO_PROTOBUF_EVENTS_MAPPING = Collections.unmodifiableMap(eventsMapping);
    }

    public static ReportMessage.SimInfo buildSimInfo(final SimInfo input) {
        ReportMessage.SimInfo simInfo = new ReportMessage.SimInfo();
        if (input.getSimCountryCode() != null) {
            simInfo.countryCode = input.getSimCountryCode();
        }
        if (input.getSimNetworkCode() != null) {
            simInfo.operatorId = input.getSimNetworkCode();
        }
        if (TextUtils.isEmpty(input.getOperatorName()) == false) {
            simInfo.operatorName = input.getOperatorName();
        }
        simInfo.dataRoaming = input.isNetworkRoaming();

        return simInfo;
    }

    @NonNull
    static SessionType sessionTypeToInternal(final int protoSessionType) {
        SessionType sessionType = PROTOBUF_TO_INTERNAL_SESSION_TYPES_MAPPING.get(protoSessionType);
        return sessionType == null ? SessionType.FOREGROUND : sessionType;
    }

    private static EventProto.ReportMessage.Time buildTime(@Nullable final Long timestamp) {
        EventProto.ReportMessage.Time metricaTime = new EventProto.ReportMessage.Time();
        if (timestamp != null) {
            metricaTime.timestamp = timestamp;
            metricaTime.timeZone = TimeUtils.getTimeZoneOffsetSec(timestamp);
        }
        return metricaTime;
    }

    @NonNull
    public static EventProto.ReportMessage.Time buildTime(@Nullable final Long timestamp,
                                                          @Nullable final Long offset,
                                                          @Nullable final Boolean obtainedBeforeSynchronization) {
        EventProto.ReportMessage.Time metricaTime = buildTime(timestamp);
        if (offset != null) {
            metricaTime.serverTimeOffset = offset;
        }
        if (obtainedBeforeSynchronization != null) {
            metricaTime.obtainedBeforeFirstSynchronization = obtainedBeforeSynchronization;
        }
        return metricaTime;
    }

    @NonNull
    public static SessionDesc buildSessionDesc(@NonNull final String locale,
                                               @Nullable final SessionType sessionType,
                                               @NonNull final EventProto.ReportMessage.Time time) {
        final SessionDesc sessionDesc = new SessionDesc();

        sessionDesc.startTime = time;
        sessionDesc.locale = locale;
        if (sessionType != null) {
            sessionDesc.sessionType = buildSessionType(sessionType);
        }

        return sessionDesc;
    }

    static int buildSessionType(@NonNull final SessionType sessionType) {
        final Integer protoSessionType = INTERNAL_TO_PROTOBUF_SESSION_TYPES_MAPPING.get(sessionType);
        return protoSessionType != null ? protoSessionType : SessionDesc.SESSION_FOREGROUND;
    }

    public static void logSessionEvents(final Session session) {
        final Session.Event[] events = session.events;
        StringBuilder eventsIds = new StringBuilder(
            "Session events' (numberInSession, globalNumber, numberOfType): "
        );

        if (null != events) {
            for (Session.Event event : events) {
                if (event != null) {
                    eventsIds.append("(" + event.numberInSession + ", " +
                        event.globalNumber + ", " + event.numberOfType + ") ");
                }
            }
        }

        DebugLogger.INSTANCE.info(TAG, eventsIds.toString());
    }

    @NonNull
    public static EventPreparer getEventPreparer(@Nullable InternalEvents internalEvent) {
        EventPreparer eventPreparer = null;
        if (internalEvent != null) {
            eventPreparer = INTERNAL_TO_PROTOBUF_EVENTS_MAPPING.get(internalEvent);
        }
        if (eventPreparer == null) {
            eventPreparer = EventPreparer.defaultPreparer();
        }
        return eventPreparer;
    }

    @Nullable
    public static Integer internalEventToProto(@Nullable InternalEvents internalEvents) {
        return internalEvents == null ? null : INTERNAL_TO_PROTOBUF_TYPES_MAPPING.get(internalEvents);
    }

    private static int sourceToProto(@NonNull ReferrerInfo.Source source) {
        switch (source) {
            case GP:
                return Referrer.GP;
            case HMS:
                return Referrer.HMS;
            default:
                return Referrer.UNKNOWN;
        }
    }

}
