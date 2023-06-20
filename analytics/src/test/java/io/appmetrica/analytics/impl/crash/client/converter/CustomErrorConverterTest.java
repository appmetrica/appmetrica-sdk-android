package io.appmetrica.analytics.impl.crash.client.converter;

import io.appmetrica.analytics.impl.crash.client.CustomError;
import io.appmetrica.analytics.impl.crash.client.RegularError;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
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
    private CrashAndroid.Error error = new CrashAndroid.Error();
    private RegularError regularError = mock(RegularError.class);

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

}
