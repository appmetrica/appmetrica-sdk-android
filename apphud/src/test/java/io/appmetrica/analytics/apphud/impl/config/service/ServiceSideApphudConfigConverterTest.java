package io.appmetrica.analytics.apphud.impl.config.service;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceSideApphudConfigConverterTest extends CommonTest {

    @NonNull
    private final ServiceSideApphudConfigConverter converter = new ServiceSideApphudConfigConverter();

    @Test
    public void fromModelAndToModel() {
        String apiKey = "some_api_key";
        ServiceSideApphudConfig model = new ServiceSideApphudConfig(
            true,
            apiKey
        );
        byte[] bytes = converter.fromModel(model);
        assertThat(converter.toModel(bytes)).usingRecursiveComparison().isEqualTo(model);
    }

    @Test
    public void toModelWithBrokenBytes() {
        ServiceSideApphudConfig model = converter.toModel("some_bytes".getBytes());
        assertThat(model).usingRecursiveComparison().isEqualTo(new ServiceSideApphudConfig(false, ""));
    }
}
