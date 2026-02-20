package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class StringAttributeTest extends CommonTest {

    private static final String KEY = "stringKey";
    private static final String VALUE = "value";

    @Test
    public void testStringValue() {
        StringUpdatePatcher patcher = (StringUpdatePatcher) Attribute
                .customString(KEY).withValue(VALUE).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isEqualTo(VALUE);
        softAssertions.assertThat(patcher.getAttributeSavingStrategy()).as("set if undefined").isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

    @Test
    public void testStringPermanentValue() {
        StringUpdatePatcher patcher = (StringUpdatePatcher) Attribute
                .customString(KEY).withValueIfUndefined(VALUE).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isEqualTo(VALUE);
        softAssertions.assertThat(patcher.getAttributeSavingStrategy()).as("set if undefined").isInstanceOf(SetIfUndefinedSavingStrategy.class);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

    @Test
    public void testResetStringAttribute() {
        ResetUpdatePatcher patcher = (ResetUpdatePatcher) Attribute
                .customString(KEY).withValueReset().getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getType()).as("type").isEqualTo(Userprofile.Profile.Attribute.STRING);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

}
