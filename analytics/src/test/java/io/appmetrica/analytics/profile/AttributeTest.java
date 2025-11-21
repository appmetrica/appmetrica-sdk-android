package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AttributeTest extends CommonTest {

    @Test
    public void testStringTrimmer() {
        StringTrimmer trimmer = (StringTrimmer) ((StringUpdatePatcher)
                Attribute.customString("key").withValue("value").getUserProfileUpdatePatcher()).getValueTrimmer();
        assertThat(trimmer.getMaxSize()).isEqualTo(200);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class NamedAttributeCreationTest {

        private final String mKey;
        private final CustomAttribute mCustomAttribute;
        private final Object mAttribute;
        private final Class<? extends CustomAttribute> mResultClass;

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"testKeyString", Attribute.customString("testKeyString"), StringAttribute.class},
                    {"testKeyNumber", Attribute.customNumber("testKeyNumber"), NumberAttribute.class},
                    {"testKeyBoolean", Attribute.customBoolean("testKeyBoolean"), BooleanAttribute.class},
                    {"testKeyCounter", Attribute.customCounter("testKeyCounter"), CounterAttribute.class},
            });
        }

        public NamedAttributeCreationTest(String key, Object attribute, Class<? extends CustomAttribute> resultClass) {
            mKey = key;
            mAttribute = attribute;
            mCustomAttribute = ReflectionHelpers.getField(attribute, "mCustomAttribute");
            mResultClass = resultClass;
        }

        @Test
        public void testAttributeCreation() {
            assertThat(mCustomAttribute.getKey()).isEqualTo(mKey);
            assertThat(mAttribute).isExactlyInstanceOf(mResultClass);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class PredefinedAttributeTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {Attribute.birthDate(), BirthDateAttribute.class},
                    {Attribute.gender(), GenderAttribute.class},
                    {Attribute.name(), NameAttribute.class},
                    {Attribute.notificationsEnabled(), NotificationsEnabledAttribute.class},
                    {Attribute.phoneHash(), FirstPartyDataPhoneSha256Attribute.class},
                    {Attribute.emailHash(), FirstPartyDataEmailSha256Attribute.class},
                    {Attribute.telegramLoginHash(), FirstPartyDataTelegramLoginSha256Attribute.class},
            });
        }

        private final Object mAttribute;
        private final Class<?> mResultClass;

        public PredefinedAttributeTest(Object attribute, Class<?> resultClass) {
            mAttribute = attribute;
            mResultClass = resultClass;
        }

        @Test
        public void testAttribute() {
            assertThat(mAttribute).isExactlyInstanceOf(mResultClass);
        }

    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class PredefinedAttributesKeyTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {Attribute.birthDate(), "appmetrica_birth_date"},
                    {Attribute.gender(), "appmetrica_gender"},
                    {Attribute.name(), "appmetrica_name"},
                    {Attribute.notificationsEnabled(), "appmetrica_notifications_enabled"},
                    {Attribute.phoneHash(), "appmetrica_1pd_phone_sha256"},
                    {Attribute.emailHash(), "appmetrica_1pd_email_sha256"},
                    {Attribute.telegramLoginHash(), "appmetrica_1pd_telegram_sha256"},
            });
        }

        private final CustomAttribute mCustomAttribute;
        private final String mKey;

        public PredefinedAttributesKeyTest(Object attribute, String key) {
            mCustomAttribute = ReflectionHelpers.getField(attribute, "mCustomAttribute");
            mKey = key;
        }

        @Test
        public void testAttribute() {
            assertThat(mCustomAttribute.getKey()).isEqualTo(mKey);
        }

    }
}
