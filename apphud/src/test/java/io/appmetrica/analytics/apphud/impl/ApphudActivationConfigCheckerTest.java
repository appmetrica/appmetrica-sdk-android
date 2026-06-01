package io.appmetrica.analytics.apphud.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.client.model.ApphudActivationConfig;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ApphudActivationConfigCheckerTest extends CommonTest {

    @NonNull
    private final ApphudActivationConfigChecker checker = new ApphudActivationConfigChecker();

    @Test
    public void doesNeedUpdateIfFull() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            "some_api_key",
            "some_device_id",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isFalse();
    }

    @Test
    public void doesNeedUpdateIfNull() {
        assertThat(checker.doesNeedUpdate(null)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfEmptyApiKey() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            "",
            "some_device_id",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfNullApiKey() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            null,
            "some_device_id",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfEmptyDeviceId() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            "some_api_key",
            "",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfNullDeviceId() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            "some_api_key",
            null,
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfEmptyUuid() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            "some_api_key",
            "some_device_id",
            ""
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfNullUuid() {
        ApphudActivationConfig config = new ApphudActivationConfig(
            "some_api_key",
            "some_device_id",
            null
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }
}
