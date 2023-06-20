package io.appmetrica.analytics.impl.crash.client.converter;

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class ModelToByteArraySerializerTest extends CommonTest {

    @Mock
    private ProtobufConverter<Object, MessageNano> converter;
    private ModelToByteArraySerializer byteArrayConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        byteArrayConverter = new ModelToByteArraySerializer(converter);
    }

    @Test
    public void toProtoReturnsCorrectByteArray() {
        Object value = new Object();
        CrashAndroid.Crash toBeReturned = new CrashAndroid.Crash();
        toBeReturned.buildId = "someText";
        doReturn(toBeReturned).when(converter).fromModel(value);
        assertThat(byteArrayConverter.toProto(value)).isEqualTo(MessageNano.toByteArray(toBeReturned));
    }

}
