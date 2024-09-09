package io.appmetrica.analytics.apphud.impl.config.remote;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteApphudConfigConverterTest extends CommonTest {

    @NonNull
    private final RemoteApphudConfigConverter remoteApphudConfigConverter = new RemoteApphudConfigConverter();

    @Test
    public void fromModelAndToModel() {
        String apiKey = "some_api_key";
        RemoteApphudConfig model = new RemoteApphudConfig(
            true,
            apiKey
        );
        byte[] bytes = remoteApphudConfigConverter.fromModel(model);
        assertThat(remoteApphudConfigConverter.toModel(bytes)).usingRecursiveComparison().isEqualTo(model);
    }

    @Test
    public void toModelWithBrokenBytes() {
        RemoteApphudConfig model = remoteApphudConfigConverter.toModel("some_bytes".getBytes());
        assertThat(model).usingRecursiveComparison().isEqualTo(new RemoteApphudConfig(false, ""));
    }
}
