package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.ProtobufUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class EventToPreparerGeneralTest extends CommonTest {

    private static class Composers {

        private final Class defaultNameComposerClass = SameNameComposer.class;
        private final Class defaultValueComposerClass = StringValueComposer.class;
        private final Class defaultEventTypeComposerClass = SameEventTypeComposer.class;
        private final Class defaultEncodingTypeComposerClass = NoneEncodingTypeProvider.class;
        private final Class defaultLocationInfoComposerClass = FullLocationInfoComposer.class;
        private final Class defaultNetworkInfoComposerClass = FullNetworkInfoComposer.class;
        private final Class defaultExtrasComposerClass = FullExtrasComposer.class;

        private Class nameComposerClass = defaultNameComposerClass;
        private Class valueComposerClass = defaultValueComposerClass;
        private Class eventTypeComposerClass = defaultEventTypeComposerClass;
        private Class encodingTypeProviderClass = defaultEncodingTypeComposerClass;
        private Class locationInfoComposerClass = defaultLocationInfoComposerClass;
        private Class networkInfoComposerClass = defaultNetworkInfoComposerClass;
        private Class extrasComposerClass = defaultExtrasComposerClass;

        Composers withCustomNameComposerClass(Class customClass) {
            nameComposerClass = customClass;
            return this;
        }

        Composers withCustomValueComposerClass(Class customClass) {
            this.valueComposerClass = customClass;
            return this;
        }

        Composers withCustomEventTypeComposerClass(Class customClass) {
            this.eventTypeComposerClass = customClass;
            return this;
        }

        Composers withCustomEncodingTypeProviderClass(Class customClass) {
            this.encodingTypeProviderClass = customClass;
            return this;
        }

        Composers withCustomLocationInfoComposerClass(Class customClass) {
            this.locationInfoComposerClass = customClass;
            return this;
        }

        Composers withCustomNetworkInfoComposerClass(Class customClass) {
            this.networkInfoComposerClass = customClass;
            return this;
        }

        Composers withCustomExtrasComposerClass(Class customClass) {
            this.extrasComposerClass = customClass;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("default composers");
            List<String> customComposers = new ArrayList<String>();
            if (nameComposerClass != defaultNameComposerClass) {
                customComposers.add(nameComposerClass.getName());
            }
            if (valueComposerClass != defaultValueComposerClass) {
                customComposers.add(valueComposerClass.getName());
            }
            if (eventTypeComposerClass != defaultEventTypeComposerClass) {
                customComposers.add(defaultEventTypeComposerClass.getName());
            }
            if (encodingTypeProviderClass != defaultEncodingTypeComposerClass) {
                customComposers.add(encodingTypeProviderClass.getName());
            }
            if (locationInfoComposerClass != defaultLocationInfoComposerClass) {
                customComposers.add(locationInfoComposerClass.getName());
            }
            if (networkInfoComposerClass != defaultNetworkInfoComposerClass) {
                customComposers.add(networkInfoComposerClass.getName());
            }
            if (extrasComposerClass != defaultExtrasComposerClass) {
                customComposers.add(extrasComposerClass.getName());
            }
            if (customComposers.size() > 0) {
                sb.append(" except ").append(customComposers);
            }
            return sb.toString();
        }
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {InternalEvents.EVENT_TYPE_ACTIVATION, defaultComposers()},
                {
                        InternalEvents.EVENT_TYPE_ALIVE,
                        defaultComposers()
                                .withCustomNameComposerClass(EmptyNameComposer.class)
                                .withCustomValueComposerClass(EmptyValueComposer.class)
                                .withCustomLocationInfoComposerClass(DummyLocationInfoComposer.class)
                                .withCustomNetworkInfoComposerClass(DummyNetworkInfoComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_ANR,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED,
                        defaultComposers()
                },
                {
                        InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED,
                        defaultComposers()
                },
                {
                        InternalEvents.EVENT_TYPE_APP_FEATURES,
                        defaultComposers().withCustomNameComposerClass(EmptyNameComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_APP_OPEN,
                        defaultComposers().withCustomValueComposerClass(EncryptedStringValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_APP_UPDATE,
                        defaultComposers().withCustomValueComposerClass(ValueWithPreloadInfoComposer.class)
                },
                {InternalEvents.EVENT_TYPE_CLEANUP, defaultComposers()},
                {
                        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
                        defaultComposers().withCustomValueComposerClass(EncryptedStringValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_INIT,
                        defaultComposers().withCustomValueComposerClass(ValueWithPreloadInfoComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_PERMISSIONS,
                        defaultComposers().withCustomNameComposerClass(EmptyNameComposer.class)
                },
                {InternalEvents.EVENT_TYPE_PURGE_BUFFER, defaultComposers()},
                {InternalEvents.EVENT_TYPE_REQUEST_REFERRER, defaultComposers()},
                {
                        InternalEvents.EVENT_TYPE_REGULAR,
                        defaultComposers().withCustomValueComposerClass(EncryptedStringValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
                        defaultComposers().withCustomValueComposerClass(UnGzipBytesValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
                        defaultComposers().withCustomValueComposerClass(BytesValueComposer.class)
                },
                {InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID, defaultComposers()},
                {
                        InternalEvents.EVENT_TYPE_START,
                        defaultComposers()
                                .withCustomNameComposerClass(EmptyNameComposer.class)
                                .withCustomValueComposerClass(BytesValueComposer.class)
                },
                {InternalEvents.EVENT_TYPE_STARTUP, defaultComposers()},
                {InternalEvents.EVENT_TYPE_UNDEFINED, defaultComposers()},
                {InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME, defaultComposers()},
                {InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG, defaultComposers()},
                {
                        InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
                        defaultComposers()
                                .withCustomValueComposerClass(ProtobufNativeCrashComposer.class)
                                .withCustomEncodingTypeProviderClass(ProtobufNativeCrashComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
                        defaultComposers()
                                .withCustomValueComposerClass(ProtobufNativeCrashComposer.class)
                                .withCustomEncodingTypeProviderClass(ProtobufNativeCrashComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
                        defaultComposers()
                                .withCustomValueComposerClass(ProtobufNativeCrashComposer.class)
                                .withCustomEncodingTypeProviderClass(ProtobufNativeCrashComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_WEBVIEW_SYNC,
                        defaultComposers().withCustomValueComposerClass(EncryptedStringValueComposer.class)
                },
                {
                        InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA,
                        defaultComposers()
                },
                {null, defaultComposers()},
        });
    }

    private static Composers defaultComposers() {
        return new Composers();
    }

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Nullable
    private final InternalEvents mInternalEvents;
    private Composers composers;

    public EventToPreparerGeneralTest(@Nullable InternalEvents internalEvents,
                                      Composers composers) {
        mInternalEvents = internalEvents;
        this.composers = composers;
    }

    @Test
    public void test() throws Exception {
        EventPreparer eventPreparer = ProtobufUtils.getEventPreparer(mInternalEvents);
        ObjectPropertyAssertions(eventPreparer)
            .withPrivateFields(true)
            .checkFieldIsInstanceOf("mNameComposer", composers.nameComposerClass)
            .checkFieldIsInstanceOf("mValueComposer", composers.valueComposerClass)
            .checkFieldIsInstanceOf("mEncodingTypeProvider", composers.encodingTypeProviderClass)
            .checkFieldIsInstanceOf("mEventTypeComposer", composers.eventTypeComposerClass)
            .checkFieldIsInstanceOf("locationInfoComposer", composers.locationInfoComposerClass)
            .checkFieldIsInstanceOf("networkInfoComposer", composers.networkInfoComposerClass)
            .checkFieldIsInstanceOf("extrasComposer", composers.extrasComposerClass)
            .checkAll();
    }
}
