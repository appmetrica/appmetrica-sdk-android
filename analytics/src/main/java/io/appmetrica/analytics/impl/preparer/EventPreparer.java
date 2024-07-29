package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.FirstOccurrenceStatus;
import io.appmetrica.analytics.impl.crash.jvm.converter.CrashOptionalBoolConverter;
import io.appmetrica.analytics.impl.db.event.DbLocationModel;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventPreparer {

    public static class Builder {

        @NonNull
        private NameComposer nameComposer;
        @NonNull
        private ValueComposer valueComposer;
        @NonNull
        private EncodingTypeProvider encodingTypeProvider;
        @NonNull
        private EventTypeComposer eventTypeComposer;
        @NonNull
        private LocationInfoComposer locationInfoComposer;
        @NonNull
        private NetworkInfoComposer networkInfoComposer;
        @NonNull
        private ExtrasComposer extrasComposer;

        private Builder(@NonNull EventPreparer source) {
            nameComposer = source.mNameComposer;
            valueComposer = source.mValueComposer;
            encodingTypeProvider = source.mEncodingTypeProvider;
            eventTypeComposer = source.mEventTypeComposer;
            locationInfoComposer = source.locationInfoComposer;
            networkInfoComposer = source.networkInfoComposer;
            extrasComposer = source.extrasComposer;
        }

        @NonNull
        public Builder withNameComposer(@NonNull NameComposer nameComposer) {
            this.nameComposer = nameComposer;
            return this;
        }

        @NonNull
        public Builder withValueComposer(@NonNull ValueComposer valueComposer) {
            this.valueComposer = valueComposer;
            return this;
        }

        @NonNull
        public Builder withEncodingTypeProvider(@NonNull EncodingTypeProvider encodingTypeProvider) {
            this.encodingTypeProvider = encodingTypeProvider;
            return this;
        }

        @NonNull
        public Builder withEventTypeComposer(@NonNull EventTypeComposer eventTypeComposer) {
            this.eventTypeComposer = eventTypeComposer;
            return this;
        }

        @NonNull
        public Builder withLocationInfoComposer(@NonNull LocationInfoComposer locationInfoComposer) {
            this.locationInfoComposer = locationInfoComposer;
            return this;
        }

        @NonNull
        public Builder withNetworkInfoComposer(@NonNull NetworkInfoComposer networkInfoComposer) {
            this.networkInfoComposer = networkInfoComposer;
            return this;
        }

        @NonNull
        public Builder withExtrasComposer(@NonNull ExtrasComposer extrasComposer) {
            this.extrasComposer = extrasComposer;
            return this;
        }

        public EventPreparer build() {
            return new EventPreparer(this);
        }
    }

    private static Map<FirstOccurrenceStatus, Integer> UNIQUENESS_STATUS_TO_PROTOBUF_MAPPING;

    static {
        HashMap<FirstOccurrenceStatus, Integer> uniquenessStatusToProtobufMapping =
                new HashMap<FirstOccurrenceStatus, Integer>();
        uniquenessStatusToProtobufMapping.put(FirstOccurrenceStatus.FIRST_OCCURRENCE,
                EventProto.ReportMessage.OPTIONAL_BOOL_TRUE);
        uniquenessStatusToProtobufMapping.put(FirstOccurrenceStatus.NON_FIRST_OCCURENCE,
                EventProto.ReportMessage.OPTIONAL_BOOL_FALSE);
        uniquenessStatusToProtobufMapping.put(
                FirstOccurrenceStatus.UNKNOWN,
                EventProto.ReportMessage.OPTIONAL_BOOL_UNDEFINED
        );
        UNIQUENESS_STATUS_TO_PROTOBUF_MAPPING = Collections.unmodifiableMap(uniquenessStatusToProtobufMapping);
    }

    @NonNull
    private final NameComposer mNameComposer;
    @NonNull
    private final ValueComposer mValueComposer;
    @NonNull
    private final EncodingTypeProvider mEncodingTypeProvider;
    @NonNull
    private final EventTypeComposer mEventTypeComposer;
    @NonNull
    private final LocationInfoComposer locationInfoComposer;
    @NonNull
    private final NetworkInfoComposer networkInfoComposer;
    @NonNull
    private final ExtrasComposer extrasComposer;

    private final static EventPreparer DEFAULT_PREPARER = new EventPreparer(
        new SameNameComposer(),
        new StringValueComposer(),
        new NoneEncodingTypeProvider(),
        new SameEventTypeComposer(),
        new FullLocationInfoComposer(),
        new FullNetworkInfoComposer(),
        new FullExtrasComposer()
    );

    private EventPreparer(@NonNull Builder builder) {
        this(
            builder.nameComposer,
            builder.valueComposer,
            builder.encodingTypeProvider,
            builder.eventTypeComposer,
            builder.locationInfoComposer,
            builder.networkInfoComposer,
            builder.extrasComposer
        );
    }

    private EventPreparer(@NonNull NameComposer nameComposer,
                          @NonNull ValueComposer valueComposer,
                          @NonNull EncodingTypeProvider encodingTypeProvider,
                          @NonNull EventTypeComposer eventTypeComposer,
                          @NonNull LocationInfoComposer locationInfoComposer,
                          @NonNull NetworkInfoComposer networkInfoComposer,
                          @NonNull ExtrasComposer extrasComposer) {
        mNameComposer = nameComposer;
        mValueComposer = valueComposer;
        mEncodingTypeProvider = encodingTypeProvider;
        mEventTypeComposer = eventTypeComposer;
        this.locationInfoComposer = locationInfoComposer;
        this.networkInfoComposer = networkInfoComposer;
        this.extrasComposer = extrasComposer;
    }

    @SuppressWarnings("checkstyle:methodLength")
    @NonNull
    public EventProto.ReportMessage.Session.Event toSessionEvent(@NonNull EventFromDbModel value,
                                                                 @NonNull ReportRequestConfig config) {
        final EventProto.ReportMessage.Session.Event eventBuilder = new EventProto.ReportMessage.Session.Event();

        final EventProto.ReportMessage.Session.Event.NetworkInfo networkInfo = networkInfoComposer.getNetworkInfo(
                value.getConnectionType(),
                value.getCellularConnectionType()
        );
        final EventProto.ReportMessage.Location locationInfo =
            locationInfoComposer.getLocation(value.getLocationData());

        if (null != networkInfo) {
            eventBuilder.networkInfo = networkInfo;
        }
        if (null != locationInfo) {
            eventBuilder.location = locationInfo;
        }

        final String name = mNameComposer.getName(value.getName());
        if (null != name) {
            eventBuilder.name = name;
        }
        eventBuilder.value = mValueComposer.getValue(value, config);
        if (null != value.getEventEnvironment()) {
            eventBuilder.environment = value.getEventEnvironment();
        }
        final Integer eventType = mEventTypeComposer.getEventType(value);
        if (eventType != null) {
            eventBuilder.type = eventType;
        }
        if (value.getIndex() != null) {
            eventBuilder.numberInSession = value.getIndex();
        }
        if (value.getGlobalNumber() != null) {
            eventBuilder.globalNumber = value.getGlobalNumber();
        }
        if (value.getNumberOfType() != null) {
            eventBuilder.numberOfType = value.getNumberOfType();
        }
        if (value.getTime() != null) {
            eventBuilder.time = value.getTime();
        }
        if (value.getBytesTruncated() != null) {
            eventBuilder.bytesTruncated = value.getBytesTruncated();
        }
        eventBuilder.encodingType = mEncodingTypeProvider.getEncodingType(value.getEventEncryptionMode());
        eventBuilder.locationTrackingEnabled = getTrackLocationEnabled(value.getLocationData());
        if (value.getProfileID() != null) {
            eventBuilder.profileId = value.getProfileID().getBytes();
        }
        Integer uniquenessStatusCode = null;
        if (value.getFirstOccurrenceStatus() != null) {
            uniquenessStatusCode = UNIQUENESS_STATUS_TO_PROTOBUF_MAPPING.get(value.getFirstOccurrenceStatus());
        }
        if (uniquenessStatusCode != null) {
            eventBuilder.firstOccurrence = uniquenessStatusCode;
        }

        if (value.getSource() != null) {
            eventBuilder.source = value.getSource().code;
        }
        if (value.getAttributionIdChanged() != null) {
            eventBuilder.attributionIdChanged = value.getAttributionIdChanged();
        }
        if (value.getOpenId() != null) {
            eventBuilder.openId = value.getOpenId();
        }
        eventBuilder.extras = extrasComposer.getExtras(value.getExtras());

        return eventBuilder;
    }

    public static EventPreparer defaultPreparer() {
        return DEFAULT_PREPARER;
    }

    public static Builder builderWithDefaults() {
        return new Builder(DEFAULT_PREPARER);
    }

    @VisibleForTesting
    int getTrackLocationEnabled(@Nullable final DbLocationModel locationData) {
        if (locationData != null) {
            return new CrashOptionalBoolConverter().toProto(locationData.getEnabled());
        }
        return EventProto.ReportMessage.OPTIONAL_BOOL_UNDEFINED;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    NameComposer getNameComposer() {
        return mNameComposer;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    ValueComposer getValueComposer() {
        return mValueComposer;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    EncodingTypeProvider getEncodingTypeProvider() {
        return mEncodingTypeProvider;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    EventTypeComposer getEventTypeComposer() {
        return mEventTypeComposer;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public LocationInfoComposer getLocationInfoComposer() {
        return locationInfoComposer;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public NetworkInfoComposer getNetworkInfoComposer() {
        return networkInfoComposer;
    }
}
