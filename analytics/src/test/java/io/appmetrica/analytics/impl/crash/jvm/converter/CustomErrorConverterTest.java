package io.appmetrica.analytics.impl.crash.jvm.converter;

import io.appmetrica.analytics.impl.crash.jvm.client.CustomError;
import io.appmetrica.analytics.impl.crash.jvm.client.RegularError;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CustomErrorConverterTest extends CommonTest {

    @Mock
    private RegularErrorConverter regularErrorConverter;
    @InjectMocks
    private CustomErrorConverter customErrorConverter;
    private final CrashAndroid.Error error = new CrashAndroid.Error();
    private final RegularError regularError = mock(RegularError.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(error).when(regularErrorConverter).fromModel(regularError);
    }

    @Test
    public void regularConverterCalled() {
        assertThat(
            customErrorConverter.fromModel(new CustomError(regularError, "id"))
        ).isSameAs(error);

        verify(regularErrorConverter).fromModel(regularError);
    }

    @Test
    public void allFieldsNotEmpty() throws IllegalAccessException {
        String id = "id";
        CustomError customError = new CustomError(regularError, id);

        CrashAndroid.Error customErrorProto = customErrorConverter.fromModel(customError);

        verify(regularErrorConverter).fromModel(regularError);

        ObjectPropertyAssertions(customErrorProto)
            .withFinalFieldOnly(false)
            .withIgnoredFields("throwable", "threads", "methodCallStacktrace", "buildId", "isOffline", "message", "custom",
                "pluginEnvironment", "virtualMachine", "virtualMachineVersion")
            .checkField("type", CrashAndroid.Error.CUSTOM)
            .checkAll();

        ObjectPropertyAssertions(customErrorProto.custom)
            .withFinalFieldOnly(false)
            .checkField("identifier", id)
            .checkAll();
    }

    @Test
    public void identifierWithInvalidEncoding() throws Exception {
        String prefix = "identifier";
        String invalidFormattedStringWithUnpairedSurrogate = "\uD83D";
        CustomError customError = new CustomError(
            regularError,
            prefix + invalidFormattedStringWithUnpairedSurrogate // Invalid formatted string with unpaired surrogate
        );
        CrashAndroid.Error errorProto = customErrorConverter.fromModel(customError);
        byte[] protoBytes = MessageNano.toByteArray(errorProto);
        assertThat(CrashAndroid.Error.parseFrom(protoBytes).custom.identifier).contains(prefix);
    }
}
