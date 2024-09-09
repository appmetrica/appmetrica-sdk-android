package io.appmetrica.analytics.apphud.impl.config.client;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ClientApphudConfigCheckerTest extends CommonTest {

    @NonNull
    private final ClientApphudConfigChecker checker = new ClientApphudConfigChecker();

    @Test
    public void doesNeedUpdateIfFull() {
        ClientApphudConfig config = new ClientApphudConfig(
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
        ClientApphudConfig config = new ClientApphudConfig(
            "",
            "some_device_id",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfNullApiKey() {
        ClientApphudConfig config = new ClientApphudConfig(
            null,
            "some_device_id",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfEmptyDeviceId() {
        ClientApphudConfig config = new ClientApphudConfig(
            "some_api_key",
            "",
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfNullDeviceId() {
        ClientApphudConfig config = new ClientApphudConfig(
            "some_api_key",
            null,
            "some_uuid"
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfEmptyUuid() {
        ClientApphudConfig config = new ClientApphudConfig(
            "some_api_key",
            "some_device_id",
            ""
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }

    @Test
    public void doesNeedUpdateIfNullUuid() {
        ClientApphudConfig config = new ClientApphudConfig(
            "some_api_key",
            "some_device_id",
            null
        );
        assertThat(checker.doesNeedUpdate(config)).isTrue();
    }
}
