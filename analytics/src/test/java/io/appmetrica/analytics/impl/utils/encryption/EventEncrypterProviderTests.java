package io.appmetrica.analytics.impl.utils.encryption;

import android.content.Context;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EventEncrypterProviderTests extends CommonTest {

    public static class BaseEventEncrypterProviderTests extends CommonTest {

        @Mock
        private GlobalServiceLocator globalServiceLocator;

        protected Context mContext;
        protected EventEncrypter mDummyEventEncrypter;
        protected EventEncrypter mAesRsaWithDecryptionOnBackendEncrypter;
        protected EventEncrypter mAesEncrypter;

        protected EventEncrypterProvider mEventEncrypterProvider;

        private static final String PACKAGE_NAME = "com.yandex.test.package.name";

        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.openMocks(this);
            mContext = TestUtils.createMockedContext();
            when(mContext.getPackageName()).thenReturn(PACKAGE_NAME);
            when(globalServiceLocator.getContext()).thenReturn(mContext);

            GlobalServiceLocator.setInstance(globalServiceLocator);

            mDummyEventEncrypter = new DummyEventEncrypter();
            mAesRsaWithDecryptionOnBackendEncrypter = new ExternallyEncryptedEventCrypter();
            mAesEncrypter = new AESEventEncrypter();

            mEventEncrypterProvider = new EventEncrypterProvider(mDummyEventEncrypter,
                mAesRsaWithDecryptionOnBackendEncrypter, mAesEncrypter);
        }

        @After
        public void tearDown() {
            GlobalServiceLocator.setInstance(null);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class GetEventEncrypterByEncryptionModeTests extends BaseEventEncrypterProviderTests {

        private final Integer mEventEncryptionModeId;
        private final String mEventEncrypterClassName;
        private final Integer mExpectedEventEncryptionModeId;

        private EventEncryptionMode mEventEncryptionMode;
        private EventEncryptionMode mExpectedEventEncryptionMode;

        @ParameterizedRobolectricTestRunner.Parameters(name = "Return {1} for encryptionMode = {3}")
        public static Collection<Object[]> data() {

            return Arrays.asList(new Object[][]{
                {EventEncryptionMode.NONE.getModeId(),
                    DummyEventEncrypter.class.getName(),
                    EventEncryptionMode.NONE.getModeId(),
                    EventEncryptionMode.NONE.name()},

                {EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.getModeId(),
                    ExternallyEncryptedEventCrypter.class.getName(),
                    EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.getModeId(),
                    EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.name()},
                {EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId(),
                    AESEventEncrypter.class.getName(),
                    EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId(),
                    EventEncryptionMode.AES_VALUE_ENCRYPTION.name()},

                {null, DummyEventEncrypter.class.getName(), EventEncryptionMode.NONE.getModeId(), "null"}
            });
        }

        public GetEventEncrypterByEncryptionModeTests(Integer eventEncryptionModeId,
                                                      String eventEncrypterClassName,
                                                      Integer expectedEventEncryptionModeId,
                                                      String eventEncryptionModeCaption) {
            mEventEncryptionModeId = eventEncryptionModeId;
            mEventEncrypterClassName = eventEncrypterClassName;
            mExpectedEventEncryptionModeId = expectedEventEncryptionModeId;
        }

        @Before
        public void setUp() throws Exception {
            super.setUp();
            mEventEncryptionMode = mEventEncryptionModeId == null ? null :
                EventEncryptionMode.valueOf(mEventEncryptionModeId);
            mExpectedEventEncryptionMode = mExpectedEventEncryptionModeId == null ? null :
                EventEncryptionMode.valueOf(mExpectedEventEncryptionModeId);
        }

        @Test
        public void testReturnExpectedEncrypter() {
            assertThat(mEventEncrypterProvider.getEventEncrypter(mEventEncryptionMode).getClass().getName())
                .isEqualTo(mEventEncrypterClassName);
        }

        @Test
        public void testReturnEncrypterWithExpectedMode() {
            assertThat(mEventEncrypterProvider.getEventEncrypter(mEventEncryptionMode).getEncryptionMode())
                .isEqualTo(mExpectedEventEncryptionMode);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class GetEventEncrypterByEventTypeTests extends BaseEventEncrypterProviderTests {

        private final int mEventTypeId;
        private final String mEventEncrypterClassName;

        @ParameterizedRobolectricTestRunner.Parameters(name = "Return {1} for eventType = {2}")
        public static Collection<Object[]> data() {
            Map<InternalEvents, String> eventTypeToEncrypterMapping =
                new HashMap<InternalEvents, String>();

            Collection<Object[]> data = new ArrayList<Object[]>();
            for (InternalEvents eventType : InternalEvents.values()) {
                String encrypterClassName = eventTypeToEncrypterMapping.get(eventType);
                data.add(new Object[]{
                    eventType.getTypeId(),
                    encrypterClassName == null ? DummyEventEncrypter.class.getName() : encrypterClassName,
                    eventType.name()
                });
            }
            return data;
        }

        public GetEventEncrypterByEventTypeTests(int eventTypeId, String eventEncrypterClassName,
                                                 String eventTypeCaption) {
            mEventTypeId = eventTypeId;
            mEventEncrypterClassName = eventEncrypterClassName;
        }

        @Test
        public void testReturnExpectedEncryption() {
            CounterReport counterReport = new CounterReport();
            counterReport.setType(mEventTypeId);
            assertThat(mEventEncrypterProvider.getEventEncrypter(counterReport).getClass().getName())
                .isEqualTo(mEventEncrypterClassName);
        }
    }
}
