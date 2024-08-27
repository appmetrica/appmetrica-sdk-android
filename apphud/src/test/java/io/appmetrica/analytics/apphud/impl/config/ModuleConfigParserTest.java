package io.appmetrica.analytics.apphud.impl.config;

import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.protobuf.client.ModuleConfigProtobuf;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ModuleConfigParserTest extends CommonTest {

    private final String apiKey = "some_api_key";

    @Mock
    private JSONObject inputJson;
    @Mock
    private ModuleConfig config;
    @Mock
    private ModuleConfigToProtoConverter converter;

    @Captor
    private ArgumentCaptor<ModuleConfigProtobuf.ModuleConfig> configCaptor;

    private ModuleConfigParser parser;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new ModuleConfigParser(converter);
        when(converter.toModel(ArgumentMatchers.any())).thenReturn(config);
        when(inputJson.optString(Constants.Startup.API_KEY_KEY)).thenReturn(apiKey);
    }

    @Test
    public void parse() {
        assertThat(parser.parse(inputJson)).isEqualTo(config);
        verify(converter).toModel(configCaptor.capture());
        assertThat(configCaptor.getAllValues()).hasSize(1);

        new ProtoObjectPropertyAssertions<>(configCaptor.getValue())
            .checkField("apiKey", apiKey)
            .checkAll();
    }
}
