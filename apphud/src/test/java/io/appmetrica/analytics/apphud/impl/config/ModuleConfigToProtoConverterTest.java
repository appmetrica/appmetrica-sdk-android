package io.appmetrica.analytics.apphud.impl.config;

import io.appmetrica.analytics.apphud.impl.protobuf.client.ModuleConfigProtobuf;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

public class ModuleConfigToProtoConverterTest extends CommonTest {

    private final ModuleConfigToProtoConverter converter = new ModuleConfigToProtoConverter();

    @Test
    public void fromModel() {
        ModuleConfig inputConfig = new ModuleConfig("some_api_key");
        ModuleConfigProtobuf.ModuleConfig result = converter.fromModel(inputConfig);
        new ProtoObjectPropertyAssertions<>(result)
            .checkField("apiKey", inputConfig.getApiKey())
            .checkAll();
    }

    @Test
    public void toModel() {
        ModuleConfigProtobuf.ModuleConfig inputConfig = new ModuleConfigProtobuf.ModuleConfig();
        inputConfig.apiKey = "some_api_key";
        ModuleConfig result = converter.toModel(inputConfig);
        ObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .checkField("apiKey", "getApiKey", inputConfig.apiKey)
            .checkAll();
    }
}
