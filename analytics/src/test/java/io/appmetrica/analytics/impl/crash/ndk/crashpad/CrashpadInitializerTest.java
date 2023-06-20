package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.content.Context;
import android.os.Bundle;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class CrashpadInitializerTest extends CommonTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private PermanentConfigSerializer serializer;
    @Mock
    private Context context;
    @Mock
    private FileProvider fileProvider;
    @Mock
    private ProcessConfiguration processConfiguration;
    @Mock
    private ProcessDetector processDetector;
    @Mock
    private CrashpadExtractorWrapper crashpadExtractorWrapper;
    @Mock
    private Consumer<Bundle> setUpNativeWrapper;
    @Mock
    private RuntimeConfigStorage storage;
    @Mock
    private Function<Void, String> crashpadVersionRetriever;
    @InjectMocks
    private CrashpadInitializer crashpadInitializer;

    private final String version = "someVestion";

    @Before
    public void setUp() {
        doReturn(version).when(crashpadVersionRetriever).apply(null);
    }

    @Test
    public void correctLibName() {
        assertThat(crashpadInitializer.getLibraryName()).isEqualTo("appmetrica-native");
    }

    @Test
    public void correctFolder() {
        assertThat(crashpadInitializer.getFolderName()).isEqualTo("appmetrica_native_crashes");
    }

    @Test
    public void fillParametersWithoutAppProcess() {
        File folder = mock(File.class);
        String handlerPath = "folderforlibs/some.so";
        boolean useLinker = false;
        boolean is64Bit = true;
        ExtractorResult extractorResult = new ExtractorResult(handlerPath, useLinker, null, is64Bit);

        String packageName = "somepackagename";
        String configEncoded = "configEncoded";
        String apikey = "apikey";
        String runtimeConfig = "runtimeConfig";
        doReturn(folder).when(fileProvider).getLibFolder(context);
        doReturn("someprocessname").when(processDetector).getProcessName();
        doReturn(packageName).when(processConfiguration).getPackageName();
        doReturn(200).when(processConfiguration).getProcessID();
        doReturn("somepsid").when(processConfiguration).getProcessSessionID();
        doReturn(configEncoded).when(serializer).serialize(apikey, processConfiguration);

        Bundle bundle = crashpadInitializer.fillParameters(apikey, "folder", extractorResult, runtimeConfig);

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(bundle.getString("arg_dd")).as("dump dir").isEqualTo("folder");
        assertions.assertThat(bundle.getString("arg_hp")).as("handler path").isEqualTo(handlerPath);
        assertions.assertThat(bundle.getBoolean("arg_ul")).as("use linker").isEqualTo(useLinker);
        assertions.assertThat(bundle.getBoolean("arg_ap")).as("use appProcess").isFalse();
        assertions.assertThat(bundle.getBoolean("arg_i64")).as("is 64 bit").isEqualTo(is64Bit);
        assertions.assertThat(bundle.getString("arg_rc")).as("runtime config").isEqualTo(runtimeConfig);
        assertions.assertThat(bundle.getString("arg_sn")).as("socket name")
                .isEqualTo(context.getPackageName() + "-crashpad_new_crash_socket");
        assertions.assertThat(bundle.getString("arg_cd")).as("client description").isEqualTo(configEncoded);

        assertions.assertThat(bundle.size()).as("size").isEqualTo(8);

        assertions.assertAll();
    }

    @Test
    public void fillParametersWithAppProcess() {
        File folder = mock(File.class);
        String handlerPath = "folderforlibs/some.so";
        boolean useLinker = false;
        boolean is64Bit = true;

        String apkPath = "some;apkPath";
        String libPath = "some;libPaht";
        String dataDir = "some/data/dir";

        AppProcessConfig appProcessConfig = new AppProcessConfig(apkPath, libPath, dataDir);

        ExtractorResult extractorResult = new ExtractorResult(handlerPath, useLinker, appProcessConfig, is64Bit);

        String packageName = "somepackagename";
        String configEncoded = "configEncoded";
        String apikey = "apikey";
        String runtimeConfig = "runtimeConfig";
        doReturn(folder).when(fileProvider).getLibFolder(context);
        doReturn("someprocessname").when(processDetector).getProcessName();
        doReturn(packageName).when(processConfiguration).getPackageName();
        doReturn(200).when(processConfiguration).getProcessID();
        doReturn("somepsid").when(processConfiguration).getProcessSessionID();
        doReturn(configEncoded).when(serializer).serialize(apikey, processConfiguration);

        Bundle bundle = crashpadInitializer.fillParameters(apikey, "folder", extractorResult, runtimeConfig);

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(bundle.getString("arg_dd")).as("dump dir").isEqualTo("folder");
        assertions.assertThat(bundle.getString("arg_hp")).as("handler path").isEqualTo(handlerPath);
        assertions.assertThat(bundle.getBoolean("arg_ul")).as("use linker").isEqualTo(useLinker);
        assertions.assertThat(bundle.getBoolean("arg_ap")).as("use appProcess").isTrue();
        assertions.assertThat(bundle.getString("arg_mc")).as("main class").isEqualTo("io.appmetrica.analytics.impl.ac.HandlerRunner");
        assertions.assertThat(bundle.getString("arg_akp")).as("apk path").isEqualTo(apkPath);
        assertions.assertThat(bundle.getString("arg_lp")).as("lib path").isEqualTo(libPath);
        assertions.assertThat(bundle.getString("arg_dp")).as("data dir").isEqualTo(dataDir);
        assertions.assertThat(bundle.getBoolean("arg_i64")).as("is 64 bit").isEqualTo(is64Bit);
        assertions.assertThat(bundle.getString("arg_rc")).as("runtime config").isEqualTo(runtimeConfig);
        assertions.assertThat(bundle.getString("arg_sn")).as("socket name")
                .isEqualTo(context.getPackageName() + "-crashpad_new_crash_socket");
        assertions.assertThat(bundle.getString("arg_cd")).as("client description").isEqualTo(configEncoded);

        assertions.assertThat(bundle.size()).as("size").isEqualTo(12);

        assertions.assertAll();
    }

    @Test
    public void useHandler() {
        final String handler = "defaultHandler.so";
        ExtractorResult result = new ExtractorResult(handler, false, null);
        doReturn(result).when(crashpadExtractorWrapper).findOrExtractHandler();
        String env = "env";
        crashpadInitializer.setUpHandler("apiKey", "folder", env);
        verify(storage).setErrorEnvironment(env);
        verify(storage).setHandlerVersion(version);
        verify(crashpadExtractorWrapper).findOrExtractHandler();
        verifyNoMoreInteractions(crashpadExtractorWrapper);
        verify(setUpNativeWrapper).consume(argThat(new ArgumentMatcher<Bundle>() {
            @Override
            public boolean matches(Bundle argument) {
                return handler.equals(argument.getString("arg_hp")) && !argument.getBoolean("arg_ul") && argument.getBoolean("arg_i64");
            }
        }));
    }

    @Test
    public void useAppProcess() {
        AppProcessConfig appProcessConfig = new AppProcessConfig("apk", "lib", "data");
        ExtractorResult result = new ExtractorResult("", false, appProcessConfig);
        doReturn(result).when(crashpadExtractorWrapper).findOrExtractHandler();
        String env = "env";
        crashpadInitializer.setUpHandler("apiKey", "folder", env);
        verify(storage).setErrorEnvironment(env);
        verify(storage).setHandlerVersion(version);
        verify(crashpadExtractorWrapper).findOrExtractHandler();
        verifyNoMoreInteractions(crashpadExtractorWrapper);
        verify(setUpNativeWrapper).consume(argThat(new ArgumentMatcher<Bundle>() {
            @Override
            public boolean matches(Bundle argument) {
                return argument.getBoolean("arg_ap") && "apk".equals(argument.getString("arg_akp"));
            }
        }));
    }

    @Test
    public void noHandler() {
        doReturn(true).when(crashpadExtractorWrapper).shouldExtract();
        doReturn(null).when(crashpadExtractorWrapper).findOrExtractHandler();
        crashpadInitializer.setUpHandler("apiKey", "folder", "env");
        verify(crashpadExtractorWrapper).findOrExtractHandler();
        verifyNoMoreInteractions(crashpadExtractorWrapper);
        verifyZeroInteractions(storage);
        verifyZeroInteractions(setUpNativeWrapper);
    }

}
