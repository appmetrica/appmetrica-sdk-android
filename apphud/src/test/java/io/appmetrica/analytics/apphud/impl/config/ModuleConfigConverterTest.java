package io.appmetrica.analytics.apphud.impl.config;

import io.appmetrica.analytics.apphud.impl.protobuf.client.ModuleConfigProtobuf;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ModuleConfigConverterTest extends CommonTest {

    @Mock
    private ModuleConfig model;
    @Mock
    private ModuleConfigProtobuf.ModuleConfig proto;
    @Mock
    private ModuleConfigToProtoConverter protoConverter;
    private final byte[] bytes = "some".getBytes();

    @Rule
    public MockedStaticRule<MessageNano> messageNanoRule = new MockedStaticRule<>(MessageNano.class);

    @Rule
    public MockedStaticRule<ModuleConfigProtobuf.ModuleConfig> protoRule =
        new MockedStaticRule<>(ModuleConfigProtobuf.ModuleConfig.class);

    private ModuleConfigConverter moduleConfigConverter;

    @Before
    public void setUp() throws InvalidProtocolBufferNanoException {
        MockitoAnnotations.openMocks(this);
        moduleConfigConverter = new ModuleConfigConverter(protoConverter);
        when(protoConverter.fromModel(model)).thenReturn(proto);
        when(protoConverter.toModel(proto)).thenReturn(model);
        when(MessageNano.toByteArray(proto)).thenReturn(bytes);
        when(ModuleConfigProtobuf.ModuleConfig.parseFrom(bytes)).thenReturn(proto);
    }

    @Test
    public void fromModel() {
        assertThat(moduleConfigConverter.fromModel(model)).isEqualTo(bytes);
    }

    @Test
    public void toModel() {
        assertThat(moduleConfigConverter.toModel(bytes)).isEqualTo(model);
    }
}
