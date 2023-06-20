package io.appmetrica.analytics.impl;

import android.util.Base64;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.BytesTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.StringByBytesTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.HashMap;

public class ClientCounterReport extends CounterReport {

    public enum TrimmedField {
        NAME, VALUE
    }

    private HashMap<TrimmedField, Integer> mTrimmedFields = new HashMap<TrimmedField, Integer>();
    private Trimmer<String> mEventTrimmer;
    private Trimmer<String> mValueTrimmer;
    private Trimmer<String> extendedValueTrimmer;
    private Trimmer<byte[]> mValueBytesTrimmer;
    private Trimmer<String> mProfileIDTrimmer;

    @VisibleForTesting
    public ClientCounterReport(@NonNull PublicLogger logger) {
        super();
        setTrimmers(logger);
    }

    public ClientCounterReport(final String name, final int type, @NonNull PublicLogger logger) {
        this(StringUtils.EMPTY, name, type, logger);
    }

    public ClientCounterReport(final String value, final String name, final int type, @NonNull PublicLogger logger) {
        this(value, name, type, 0, logger);
    }

    public ClientCounterReport(final String value,
                               final String name,
                               final int type,
                               final int customType,
                               @NonNull PublicLogger logger) {
        super();
        setTrimmers(logger);
        this.value = trimValue(value);
        this.name = trimName(name);
        setType(type);
        setCustomType(customType);
    }

    public ClientCounterReport(final byte[] value,
                               @Nullable final String name,
                               final int type,
                               @NonNull PublicLogger logger) {
        super();
        setTrimmers(logger);
        setTrimmedValueBytes(value);
        this.name = trimName(name);
        setType(type);
    }

    public ClientCounterReport withTrimmedFields(
        @NonNull HashMap<ClientCounterReport.TrimmedField, Integer> trimmedFields) {
        mTrimmedFields = trimmedFields;
        return this;
    }

    public ClientCounterReport withExtendedValue(@NonNull String value) {
        super.setValue(trimExtendedValue(value));
        return this;
    }

    @NonNull
    public HashMap<ClientCounterReport.TrimmedField, Integer> getTrimmedFields() {
        return mTrimmedFields;
    }

    private void setTrimmers(@NonNull PublicLogger logger) {
        mEventTrimmer =
            new StringTrimmer(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH, "event name", logger);
        mValueTrimmer =
            new StringByBytesTrimmer(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE, "event value", logger);
        extendedValueTrimmer = new StringByBytesTrimmer(
            EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE,
            "event extended value",
            logger
        );
        mValueBytesTrimmer =
            new BytesTrimmer(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE, "event value bytes", logger);
        mProfileIDTrimmer =
            new StringTrimmer(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH, "user profile id", logger);
    }

    private void checkFieldForTrimming(@Nullable String originalValue,
                                       @Nullable String newValue,
                                       TrimmedField trimmedField) {
        if (EventLimitationProcessor.valueWasTrimmed(originalValue, newValue)) {
            mTrimmedFields.put(
                trimmedField,
                StringUtils.getUTF8Bytes(originalValue).length - StringUtils.getUTF8Bytes(newValue).length
            );
        } else {
            mTrimmedFields.remove(trimmedField);
        }
        refreshBytesTruncated();
    }

    private void checkFieldForTrimming(byte[] originalValue, byte[] newValue, TrimmedField trimmedField) {
        if (originalValue.length != newValue.length) {
            mTrimmedFields.put(trimmedField, originalValue.length - newValue.length);
        } else {
            mTrimmedFields.remove(trimmedField);
        }
        refreshBytesTruncated();
    }

    private void refreshBytesTruncated() {
        int bytesTruncated = 0;
        for (Integer value : mTrimmedFields.values()) {
            bytesTruncated += value;
        }
        setBytesTruncated(bytesTruncated);
    }

    private String trimName(@Nullable String event) {
        String newEvent = mEventTrimmer.trim(event);
        checkFieldForTrimming(event, newEvent, TrimmedField.NAME);
        return newEvent;
    }

    private String trimValue(String value) {
        String newValue = mValueTrimmer.trim(value);
        checkFieldForTrimming(value, newValue, TrimmedField.VALUE);
        return newValue;
    }

    private String trimExtendedValue(@NonNull String value) {
        String newValue = extendedValueTrimmer.trim(value);
        checkFieldForTrimming(value, newValue, TrimmedField.VALUE);
        return newValue;
    }

    private byte[] trimValue(byte[] value) {
        byte[] newValue = mValueBytesTrimmer.trim(value);
        checkFieldForTrimming(value, newValue, TrimmedField.VALUE);
        return newValue;
    }

    @Override
    public void setName(@Nullable String name) {
        this.name = trimName(name);
    }

    @Override
    public void setValue(@Nullable String value) {
        this.value = trimValue(value);
    }

    @Override
    @Nullable
    public final void setValueBytes(@Nullable byte[] bytes) {
        setTrimmedValueBytes(bytes);
    }

    private void setTrimmedValueBytes(@Nullable byte[] bytes) {
        super.setValueBytes(trimValue(bytes));
    }

    @Override
    @NonNull
    public void setProfileID(@Nullable String value) {
        super.setProfileID(mProfileIDTrimmer.trim(value));
    }

    public static CounterReport formAppEnvironmentChangedReport(String key, String value) {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED.getTypeId());
        counterReport.setAppEnvironment(key, value);
        return counterReport;
    }

    public static CounterReport formAppEnvironmentClearedReport() {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED.getTypeId());
        return counterReport;
    }

    public static CounterReport formUserProfileEvent() {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.getTypeId());
        return counterReport;
    }

    public static CounterReport formUserProfileEvent(
        @NonNull final Userprofile.Profile userProfile
    ) {
        CounterReport counterReport = formUserProfileEvent();
        counterReport.setValue(new String(Base64.encode(MessageNano.toByteArray(userProfile), 0)));
        return counterReport;
    }

    @NonNull
    static CounterReport formSetUserProfileIDEvent(
        @Nullable String userProfileID,
        @NonNull PublicLogger logger
    ) {
        CounterReport counterReport = new ClientCounterReport(logger);
        counterReport.setType(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.getTypeId());
        counterReport.setProfileID(userProfileID);
        counterReport.setValue(userProfileID);
        return counterReport;
    }

    @NonNull
    static CounterReport formRevenueEvent(
        @NonNull PublicLogger logger,
        @NonNull RevenueWrapper revenue
    ) {
        CounterReport counterReport = new ClientCounterReport(logger);
        counterReport.setType(InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.getTypeId());

        final Pair<byte[], Integer> result = revenue.getDataToSend();
        counterReport.setValue(new String(Base64.encode(result.first, 0)));
        counterReport.setBytesTruncated(result.second);

        return counterReport;
    }

    @NonNull
    static CounterReport formAdRevenueEvent(
        @NonNull PublicLogger logger,
        @NonNull AdRevenueWrapper adRevenue
    ) {
        CounterReport counterReport = new ClientCounterReport(logger);
        counterReport.setType(InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.getTypeId());

        final kotlin.Pair<byte[], Integer> result = adRevenue.getDataToSend();
        counterReport.setValue(new String(Base64.encode(result.getFirst(), 0)));
        counterReport.setBytesTruncated(result.getSecond());

        return counterReport;
    }

    static CounterReport formECommerceEvent(
        @NonNull PublicLogger logger,
        @NonNull Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider> result
    ) {
        CounterReport counterReport = new ClientCounterReport(logger);
        counterReport.setType(InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.getTypeId());

        final byte[] valueBytes = MessageNano.toByteArray(result.result);
        counterReport.setValue(Base64Utils.compressBase64(valueBytes));
        counterReport.setBytesTruncated(result.getBytesTruncated());

        return counterReport;
    }

    @NonNull
    static CounterReport formJsEvent(@NonNull String eventName,
                                     @Nullable String eventValue,
                                     @NonNull PublicLogger logger) {
        CounterReport counterReport = EventsManager.regularEventReportEntry(eventName, eventValue, logger);
        counterReport.setSource(EventSource.JS);
        return counterReport;
    }

}
