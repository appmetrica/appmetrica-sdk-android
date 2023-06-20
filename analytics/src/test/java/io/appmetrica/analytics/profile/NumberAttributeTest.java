package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.impl.profile.NumberUpdatePatcher;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class NumberAttributeTest extends CommonTest {

    private static final String KEY = "numberKey";
    private static final int VALUE = 200;

    @Test
    public void testNumberValue() {
        NumberUpdatePatcher patcher = (NumberUpdatePatcher) Attribute
                .customNumber(KEY).withValue(VALUE).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isEqualTo(VALUE);
        softAssertions.assertThat(patcher.getAttributeSavingStrategy()).as("set if undefined").isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

    @Test
    public void testNumberPermanentValue() {
        NumberUpdatePatcher patcher = (NumberUpdatePatcher) Attribute
                .customNumber(KEY).withValueIfUndefined(VALUE).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isEqualTo(VALUE);
        softAssertions.assertThat(patcher.getAttributeSavingStrategy()).as("set if undefined").isInstanceOf(SetIfUndefinedSavingStrategy.class);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

    @Test
    public void testResetNumberAttribute() {
        ResetUpdatePatcher patcher = (ResetUpdatePatcher) Attribute
                .customNumber(KEY).withValueReset().getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getType()).as("type").isEqualTo(Userprofile.Profile.Attribute.NUMBER);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

}
