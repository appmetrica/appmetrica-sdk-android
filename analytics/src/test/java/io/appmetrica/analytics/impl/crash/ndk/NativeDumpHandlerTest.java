package io.appmetrica.analytics.impl.crash.ndk;

import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.crash.client.converter.NativeCrashConverter;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class NativeDumpHandlerTest extends CommonTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private NativeCrashHandlerDescription description;

    @Mock
    private NativeCrashConverter converter;
    @InjectMocks
    private NativeDumpHandler nativeDumpHandler;

    @Rule
    public final MockedStaticRule<IOUtils> sIoUtils = new MockedStaticRule<>(IOUtils.class);

    @Rule
    public final MockedStaticRule<Base64Utils> sBase64Utils = new MockedStaticRule<>(Base64Utils.class);

    @Test
    public void readDump() {
        String absPath = "fileabspath";
        File file = mock(File.class);
        CrashAndroid.Crash crash = new CrashAndroid.Crash();
        crash.buildId = "buildid";
        byte[] fileData = MessageNano.toByteArray(crash);

        doReturn(absPath).when(file).getAbsolutePath();
        when(IOUtils.readAll(absPath)).thenReturn(fileData);
        doReturn(crash).when(converter).fromModel(new NativeCrashModel(fileData, description));
        String fileDataString = "filedata";
        when(Base64Utils.compressBase64(fileData)).thenReturn(fileDataString);
        assertThat(nativeDumpHandler.apply(file, description)).isEqualTo(fileDataString);
    }

    @Test
    public void emptyDump() {
        String absPath = "fileabspath";
        File file = mock(File.class);
        byte[] fileData = new byte[] {};

        doReturn(absPath).when(file).getAbsolutePath();
        when(IOUtils.readAll(absPath)).thenReturn(fileData);

        assertThat(nativeDumpHandler.apply(file, description)).isNull();
    }

    @Test
    public void nullDump() {
        String absPath = "fileabspath";
        File file = mock(File.class);

        doReturn(absPath).when(file).getAbsolutePath();
        when(IOUtils.readAll(absPath)).thenReturn(null);

        assertThat(nativeDumpHandler.apply(file, description)).isNull();
    }

}
