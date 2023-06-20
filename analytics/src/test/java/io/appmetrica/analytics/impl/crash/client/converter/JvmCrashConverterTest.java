package io.appmetrica.analytics.impl.crash.client.converter;

import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JvmCrashConverterTest extends CommonTest {

    @Mock
    private ModelToByteArraySerializer<UnhandledException> baseConverter;

    private JvmCrashConverter jvmCrashConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jvmCrashConverter = new JvmCrashConverter(baseConverter);
    }

    @Test
    public void testToProto() throws IllegalAccessException, InvalidProtocolBufferNanoException {
        UnhandledException exception = mock(UnhandledException.class);
        byte[] bytes = new byte[] { 1, 3, 5, 7 };
        when(baseConverter.toProto(exception)).thenReturn(bytes);
        assertThat(jvmCrashConverter.fromModel(exception)).isEqualTo(bytes);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        jvmCrashConverter.toModel(MessageNano.toByteArray(new CrashAndroid.Error()));
    }

}
