package io.appmetrica.analytics.impl.utils.encryption;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.utils.MapWithDefault;

public class EventEncrypterProvider {

    @NonNull
    private final MapWithDefault<EventEncryptionMode, EventEncrypter> mEncrypters;
    @NonNull
    private final MapWithDefault<InternalEvents, EventEncrypter> mEncrypterToEventTypeMapping;

    public EventEncrypterProvider() {
        this(
                new DummyEventEncrypter(),
                new ExternallyEncryptedEventCrypter(),
                new AESEventEncrypter()
        );
    }

    public EventEncrypterProvider(@NonNull final EventEncrypter dummyEventEncrypter,
                                  @NonNull final EventEncrypter aesRsaWithDecryptionOnBackendEncrypter,
                                  @NonNull final EventEncrypter aesEventEncrypter) {

        mEncrypters = new MapWithDefault<EventEncryptionMode, EventEncrypter>(dummyEventEncrypter);

        mEncrypters.put(EventEncryptionMode.NONE, dummyEventEncrypter);
        mEncrypters.put(EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER,
                aesRsaWithDecryptionOnBackendEncrypter);
        mEncrypters.put(EventEncryptionMode.AES_VALUE_ENCRYPTION, aesEventEncrypter);

        mEncrypterToEventTypeMapping =
                new MapWithDefault<InternalEvents, EventEncrypter>(dummyEventEncrypter);
    }

    @NonNull
    public EventEncrypter getEventEncrypter(EventEncryptionMode eventEncryptionMode) {
        return mEncrypters.get(eventEncryptionMode);
    }

    @NonNull
    public EventEncrypter getEventEncrypter(@NonNull final CounterReport counterReport) {
        int eventTypeCode = counterReport.getType();
        InternalEvents eventType = InternalEvents.valueOf(eventTypeCode);
        return getEventEncrypter(eventType);
    }

    @NonNull
    private EventEncrypter getEventEncrypter(@NonNull final InternalEvents eventType) {
        return mEncrypterToEventTypeMapping.get(eventType);
    }
}
