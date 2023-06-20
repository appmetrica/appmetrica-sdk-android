package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.BooleanUpdatePatcher;
import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BooleanAttributeTest extends CommonTest {

    private static final String KEY = "booleanKey";

    @Test
    public void testBooleanValue() {
        BooleanUpdatePatcher patcher = (BooleanUpdatePatcher) Attribute
                .customBoolean(KEY).withValue(true).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isTrue();
        softAssertions.assertThat(patcher.getAttributeSavingStrategy()).as("set if undefined").isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

    @Test
    public void testBooleanPermanentValue() {
        BooleanUpdatePatcher patcher = (BooleanUpdatePatcher) Attribute
                .customBoolean(KEY).withValueIfUndefined(true).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isTrue();
        softAssertions.assertThat(patcher.getAttributeSavingStrategy()).as("set if undefined").isInstanceOf(SetIfUndefinedSavingStrategy.class);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

    @Test
    public void testResetBooleanAttribute() {
        ResetUpdatePatcher patcher = (ResetUpdatePatcher) Attribute
                .customBoolean(KEY).withValueReset().getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getType()).as("type").isEqualTo(Userprofile.Profile.Attribute.BOOL);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

}
