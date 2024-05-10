package io.appmetrica.analytics.impl.utils.encryption;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AESEventEncrypterTests extends CommonTest {

    public static class BaseAESEventEncrypterTests extends CommonTest {

        private Context mContext;

        protected static final String PACKAGE_NAME = "com.yandex.test.package.name";
        protected AESEventEncrypter mAESEventEncrypter;

        @Mock
        private GlobalServiceLocator globalServiceLocator;

        @Before
        public void setUp() throws Throwable {
            MockitoAnnotations.openMocks(this);
            GlobalServiceLocator.setInstance(globalServiceLocator);
            mContext = TestUtils.createMockedContext();
            when(mContext.getPackageName()).thenReturn(PACKAGE_NAME);
            when(globalServiceLocator.getContext()).thenReturn(mContext);
        }

        @After
        public void tearDown() {
            GlobalServiceLocator.setInstance(null);
        }
    }

    @RunWith(RobolectricTestRunner.class)
    public static class AESEventEncrypterConstructorTests extends BaseAESEventEncrypterTests {

        @Test
        public void testConstructorWithContext() {
            mAESEventEncrypter = new AESEventEncrypter();
            AESEncrypter aesEncrypter = mAESEventEncrypter.getAESEncrypter();
            assertThat(aesEncrypter.getAlgorithm()).isEqualTo(AESEncrypter.DEFAULT_ALGORITHM);
            assertThat(aesEncrypter.getPassword())
                    .isEqualTo(new byte[]{73, -106, -125, 31, 77, -66, 83, -33, -8, -96, 19, -68, 80, 109, -11, -47});
            assertThat(aesEncrypter.getIV())
                    .isEqualTo(new byte[]{-108, -106, 25, 95, -44, -21, 17, -7, -116, -79, 104, -3, -45, 64, 24, -82});
        }

        @Test
        public void testConstructorWithCredentialProvider() {
            AESCredentialProvider aesCredentialProvider = mock(AESCredentialProvider.class);
            byte[] password = new byte[]{73, -106, -125, 31, 77, -66, 83, -33, -8, -96, 19, -68, 80, 109, -11, -47};
            byte[] iv = new byte[]{-108, -106, 25, 95, -44, -21, 17, -7, -116, -79, 104, -3, -45, 64, 24, -82};
            when(aesCredentialProvider.getPassword()).thenReturn(password);
            when(aesCredentialProvider.getIV()).thenReturn(iv);
            mAESEventEncrypter = new AESEventEncrypter(aesCredentialProvider);
            AESEncrypter aesEncrypter = mAESEventEncrypter.getAESEncrypter();
            assertThat(aesEncrypter.getAlgorithm()).isEqualTo(AESEncrypter.DEFAULT_ALGORITHM);
            assertThat(aesEncrypter.getPassword()).isEqualTo(password);
            assertThat(aesEncrypter.getIV()).isEqualTo(iv);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class AESEventEncrypterEncryptionParametrizedTests extends BaseAESEventEncrypterTests {

        private String mInput;
        private String mExpectedEncryptedData;
        private byte[] mExpectedDecryptedData;

        protected AESEncrypter mAESEncrypter;

        protected CounterReport mCounterReport;
        protected EncryptedCounterReport mEncryptedCounterReport;
        protected String mEncryptedValue;

        protected static final String SHORT_STRING = "short test string";
        protected static final String LONG_STRING = new RandomStringGenerator(256 * 1024).nextString();

        @ParameterizedRobolectricTestRunner.Parameters(name = "For input data = {3}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {null, null, new byte[0], "[0] null"},
                    {"", null, new byte[0], "[1] empty array"},

                    {SHORT_STRING, SHORT_STRING,
                            SHORT_STRING.getBytes(), "[2] short string"},

                    {LONG_STRING, LONG_STRING,
                            LONG_STRING.getBytes(), "[3] long string"}
            });
        }

        public AESEventEncrypterEncryptionParametrizedTests(final String input,
                                                            final String expectedEncryptedData,
                                                            final byte[] expectedDecryptedData,
                                                            String inputCaption) {
            mInput = input;
            mExpectedEncryptedData = expectedEncryptedData;
            mExpectedDecryptedData = expectedDecryptedData;
        }

        @Override
        @Before
        public void setUp() throws Throwable {
            super.setUp();
            mAESEncrypter = createAESEncrypterMock();
            mAESEventEncrypter = new AESEventEncrypter(mAESEncrypter);

            mCounterReport = new CounterReport();
            mCounterReport.setValue(mInput);
            mEncryptedCounterReport = mAESEventEncrypter.encrypt(mCounterReport);
            mEncryptedValue = mEncryptedCounterReport.mCounterReport.getValue();
        }

        @Test
        public void testEncryptedValue() {
            String expectedEncryptedValue = TextUtils.isEmpty(mExpectedEncryptedData) ? mExpectedEncryptedData :
                    Base64.encodeToString(mExpectedEncryptedData.getBytes(), Base64.DEFAULT);
            assertThat(mEncryptedValue).isEqualTo(expectedEncryptedValue);
        }

        @Test
        public void testDecryptedValue() {
            byte[] decryptedValue =
                    mAESEventEncrypter.decrypt(mEncryptedValue == null ? null : mEncryptedValue.getBytes());
            assertThat(decryptedValue).isEqualTo(mExpectedDecryptedData);
        }

        @Test
        public void testEncryptionMode() {
            assertThat(mEncryptedCounterReport.mEventEncryptionMode)
                    .isEqualTo(EventEncryptionMode.AES_VALUE_ENCRYPTION);
        }

        private AESEncrypter createAESEncrypterMock() throws Throwable {
            AESEncrypter aesEncrypter = mock(AESEncrypter.class);

            doAnswer(new Answer() {
                public Object answer(final InvocationOnMock invocation) throws Throwable {
                    return invocation.getArguments()[0];
                }
            }).when(aesEncrypter).encrypt(any(byte[].class));
            doAnswer(new Answer() {
                public Object answer(final InvocationOnMock invocation) throws Throwable {
                    return invocation.getArguments()[0];
                }
            }).when(aesEncrypter).decrypt(any(byte[].class));

            return aesEncrypter;
        }
    }
}
