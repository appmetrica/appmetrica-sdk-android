package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.content.Context;
import android.os.Build;
import android.os.Process;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.utils.AbiResolver;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CrashpadExtractorWrapperTest extends CommonTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private Context context;
    @Mock
    private ICommonExecutor executor;
    @Mock
    private File defaultHandler;
    @Mock
    private File cacheDir;
    @Mock
    private File crashpadDir;
    @Mock
    private Function<Void, String> versionExtractor;
    @Mock
    private Callable<String> libDirInsideApk;
    @Mock
    private AbiResolver abiResolver;
    @Mock
    private CrashpadExtractor extractor;
    @Mock
    private AppProcessConfigProvider appProcessConfigProvider;
    @Mock
    private FileProvider fileProvider;

    private final List<String> defaultLibNames = Arrays.asList("libsomelib.so", "libsomelibold.so");
    private final String version = "1.4.5";
    private CrashpadExtractorWrapper crashpadExtractorWrapper;

    @Before
    public void setUp() {
        doReturn(cacheDir).when(context).getCacheDir();
        doReturn(version).when(versionExtractor).apply(nullable(Void.class));
        crashpadExtractorWrapper = new CrashpadExtractorWrapper(
                context, executor, defaultLibNames, defaultHandler, crashpadDir, versionExtractor, libDirInsideApk,
                abiResolver, extractor, appProcessConfigProvider, fileProvider
        );
    }

    @Test
    public void shouldExtract() {
        doReturn(false).when(defaultHandler).exists();
        assertThat(crashpadExtractorWrapper.shouldExtract()).isTrue();
    }

    @Test
    public void shouldNotExtract() {
        doReturn(true).when(defaultHandler).exists();
        assertThat(crashpadExtractorWrapper.shouldExtract()).isFalse();
    }

    @Test
    public void shouldExtractBecauseHandlerIsNull() {
        crashpadExtractorWrapper = new CrashpadExtractorWrapper(
                context, executor, defaultLibNames, null, crashpadDir, versionExtractor, libDirInsideApk,
                abiResolver, extractor, appProcessConfigProvider, fileProvider
        );
        assertThat(crashpadExtractorWrapper.shouldExtract()).isTrue();
    }

    @Test
    public void directoryCreated() {
        doReturn(false).when(crashpadDir).exists();
        doReturn(true).when(crashpadDir).mkdirs();
        assertThat(crashpadExtractorWrapper.makeCrashpadDirAndSetPermission()).isFalse();
        verify(crashpadDir).mkdirs();
    }

    @Test
    public void cacheDirExecutable() {
        doReturn(false).when(crashpadDir).exists();
        doReturn(true).when(crashpadDir).mkdirs();
        doReturn(true).when(cacheDir).setExecutable(true, false);
        assertThat(crashpadExtractorWrapper.makeCrashpadDirAndSetPermission()).isFalse();
        verify(cacheDir).setExecutable(true, false);
    }

    @Test
    public void noCrashpadDir() {
        crashpadExtractorWrapper = new CrashpadExtractorWrapper(
                context, executor, defaultLibNames, defaultHandler, null, versionExtractor, libDirInsideApk,
                abiResolver, extractor, appProcessConfigProvider, fileProvider
        );
        assertThat(crashpadExtractorWrapper.makeCrashpadDirAndSetPermission()).isFalse();
    }

    @Test
    public void crashpadDirExecutable() {
        doReturn(false).when(crashpadDir).exists();
        doReturn(true).when(crashpadDir).mkdirs();
        doReturn(true).when(cacheDir).setExecutable(true, false);
        doReturn(true).when(crashpadDir).setExecutable(true, false);
        assertThat(crashpadExtractorWrapper.makeCrashpadDirAndSetPermission()).isTrue();
        verify(crashpadDir).setExecutable(true, false);
    }

    @Test
    public void crashpadDirExist() {
        doReturn(true).when(crashpadDir).exists();
        assertThat(crashpadExtractorWrapper.makeCrashpadDirAndSetPermission()).isTrue();
        verify(crashpadDir).exists();
        verifyNoMoreInteractions(crashpadDir);
        verifyZeroInteractions(cacheDir);
    }

    @Test
    public void cacheDirIsNull() {
        doReturn(false).when(crashpadDir).exists();
        doReturn(true).when(crashpadDir).mkdirs();
        doReturn(null).when(context).getCacheDir();
        crashpadExtractorWrapper = new CrashpadExtractorWrapper(
                context, executor, defaultLibNames, defaultHandler, crashpadDir, versionExtractor, libDirInsideApk,
                abiResolver, extractor, appProcessConfigProvider, fileProvider
        );
        assertThat(crashpadExtractorWrapper.makeCrashpadDirAndSetPermission()).isFalse();
        verify(crashpadDir).exists();
        verify(crashpadDir).mkdirs();
    }

    @Test
    public void cleanupNoCrashpadDir() {
        crashpadExtractorWrapper = new CrashpadExtractorWrapper(
                context, executor, defaultLibNames, defaultHandler, null, versionExtractor, libDirInsideApk,
                abiResolver, extractor, appProcessConfigProvider, fileProvider
        );
        crashpadExtractorWrapper.deleteFiles(new CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher("suffix"));
    }

    @Test
    public void cleanupNoFiles() {
        crashpadExtractorWrapper.deleteFiles(new CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher("suffix"));
        verify(crashpadDir).listFiles();
    }

    @Test
    public void deleteFilesWithFilter() {
        File[] files = new File[5];
        for (int i = 1; i <= 5; i++) {
            files[i-1] = mock(File.class);
            doReturn("file-" + i).when(files[i-1]).getName();
        }
        doReturn(files).when(crashpadDir).listFiles();
        crashpadExtractorWrapper.deleteFiles(new CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher("-5"));
        verify(files[0]).delete();
        verify(files[1]).delete();
        verify(files[2]).delete();
        verify(files[3]).delete();
        verify(files[4], times(0)).delete();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void abiResolving() {
        doReturn(true).when(crashpadDir).exists();
        doReturn(false).when(defaultHandler).exists();
        crashpadExtractorWrapper.findOrExtractHandler();
        verify(abiResolver).getAbi();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void noAppProcessConfig() {
        String abi = "x86";

        doReturn(true).when(crashpadDir).exists();
        doReturn(false).when(defaultHandler).exists();
        doReturn(version).when(versionExtractor).apply(nullable(Void.class));
        doReturn(abi).when(abiResolver).getAbi();
        doReturn(null).when(appProcessConfigProvider).provideAppConfig(any(Context.class), anyString());

        crashpadExtractorWrapper.findOrExtractHandler();

        verify(appProcessConfigProvider).provideAppConfig(any(Context.class), anyString());
        verify(extractor).extractFileIfStale(
                String.format("lib/%s/%s", abi, defaultLibNames.get(0)),
                defaultLibNames.get(0) + "-" + version
        );
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.P)
    public void hasAppProcessConfig() {
        AppProcessConfig config = new AppProcessConfig("", "", "");
        doReturn("x86").when(abiResolver).getAbi();
        doReturn(config).when(appProcessConfigProvider).provideAppConfig(any(Context.class), anyString());

        ExtractorResult result = crashpadExtractorWrapper
            .findOrExtractHandler();
        assertThat(result).isNotNull();
        assertThat(result.appProcessConfig).isSameAs(config);

        verifyZeroInteractions(defaultHandler);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.O_MR1)
    public void staledFilesDeleted() {
        doReturn(true).when(crashpadDir).exists();
        doReturn(false).when(defaultHandler).exists();
        doReturn("x86").when(abiResolver).getAbi();
        crashpadExtractorWrapper.findOrExtractHandler();
        verify(executor).execute(any(Runnable.class));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.O_MR1)
    public void fineCase() {
        String abi = "x86";

        doReturn(true).when(crashpadDir).exists();
        doReturn(false).when(defaultHandler).exists();
        doReturn(version).when(versionExtractor).apply(nullable(Void.class));
        doReturn(abi).when(abiResolver).getAbi();

        crashpadExtractorWrapper.findOrExtractHandler();

        verify(extractor).extractFileIfStale(
                String.format("lib/%s/%s", abi, defaultLibNames.get(0)),
                defaultLibNames.get(0) + "-" + version
        );
    }

    @Test
    public void noAbi() {
        doReturn(true).when(crashpadDir).exists();
        doReturn(false).when(defaultHandler).exists();
        crashpadExtractorWrapper.findOrExtractHandler();
        verifyZeroInteractions(executor);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.O_MR1)
    public void extractedByOS() {
        String path = "somePath";
        doReturn(path).when(defaultHandler).getAbsolutePath();
        doReturn(true).when(defaultHandler).exists();
        ExtractorResult extractorResult = crashpadExtractorWrapper.findOrExtractHandler();

        assertThat(extractorResult).isNotNull();

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(extractorResult.pathToHandler).as("path").isEqualTo(path);
        assertions.assertThat(extractorResult.useLinker).as("use linker").isFalse();
        assertions.assertThat(extractorResult.is64bit).as("is 64 bit").isEqualTo(Process.is64Bit());
        assertions.assertAll();

        verify(defaultHandler).getAbsolutePath();
        verifyZeroInteractions(executor);
    }

    @Test
    public void runWithLinker() throws Exception {
        File libDir = mock(File.class);
        File pathToHandlerFile = mock(File.class);
        File pathToOldHandlerFile = mock(File.class);
        String pathToHandler = "some path to handler";
        when(pathToHandlerFile.getAbsolutePath()).thenReturn(pathToHandler);
        doReturn(true).when(pathToHandlerFile).exists();
        String pathToOldHandler = "some path to old handler";
        when(pathToOldHandlerFile.getAbsolutePath()).thenReturn(pathToOldHandler);
        String path = "somePath/";
        doReturn(false).when(defaultHandler).exists();
        doReturn(path).when(libDirInsideApk).call();
        when(fileProvider.getFileByNonNullPath(path)).thenReturn(libDir);
        when(fileProvider.getFileByNonNullPath(libDir, defaultLibNames.get(0))).thenReturn(pathToHandlerFile);
        when(fileProvider.getFileByNonNullPath(libDir, defaultLibNames.get(1))).thenReturn(pathToOldHandlerFile);
        ExtractorResult extractorResult = crashpadExtractorWrapper.findOrExtractHandler();

        assertThat(extractorResult).isNotNull();

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(extractorResult.pathToHandler).as("path").isEqualTo(pathToHandler);
        assertions.assertThat(extractorResult.useLinker).as("use linker").isTrue();
        assertions.assertThat(extractorResult.is64bit).as("is 64 bit").isEqualTo(Process.is64Bit());
        assertions.assertAll();

        verify(defaultHandler, never()).getAbsolutePath();
        verifyNoInteractions(executor);
    }

    @Test
    public void runWithLinkerOldFile() throws Exception {
        File libDir = mock(File.class);
        File pathToHandlerFile = mock(File.class);
        File pathToOldHandlerFile = mock(File.class);
        String pathToHandler = "some path to handler";
        when(pathToHandlerFile.getAbsolutePath()).thenReturn(pathToHandler);
        doReturn(true).when(pathToOldHandlerFile).exists();
        String pathToOldHandler = "some path to old handler";
        when(pathToOldHandlerFile.getAbsolutePath()).thenReturn(pathToOldHandler);
        String path = "somePath/";
        doReturn(false).when(defaultHandler).exists();
        doReturn(path).when(libDirInsideApk).call();
        when(fileProvider.getFileByNonNullPath(path)).thenReturn(libDir);
        when(fileProvider.getFileByNonNullPath(libDir, defaultLibNames.get(0))).thenReturn(pathToHandlerFile);
        when(fileProvider.getFileByNonNullPath(libDir, defaultLibNames.get(1))).thenReturn(pathToOldHandlerFile);
        ExtractorResult extractorResult = crashpadExtractorWrapper.findOrExtractHandler();

        assertThat(extractorResult).isNotNull();

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(extractorResult.pathToHandler).as("path").isEqualTo(pathToOldHandler);
        assertions.assertThat(extractorResult.useLinker).as("use linker").isTrue();
        assertions.assertThat(extractorResult.is64bit).as("is 64 bit").isEqualTo(Process.is64Bit());
        assertions.assertAll();

        verify(defaultHandler, never()).getAbsolutePath();
        verifyNoInteractions(executor);
    }

    @Test
    public void suffixMatcherMatched() {
        File file = mock(File.class);
        String suffix = "suffix";
        doReturn("somename" + suffix).when(file).getName();

        CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher matcher = new CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher(suffix);

        assertThat(matcher.apply(file)).isFalse();
    }

    @Test
    public void mpBestCase() throws Exception {
        String abi = "x86";
        String libDir = "/some/lib/dir";
        File libDirFile = mock(File.class);
        File handler = mock(File.class);
        String pathToHandler = "/some/lib/dir/" + defaultLibNames.get(0);
        AppProcessConfig config = new AppProcessConfig("a", "l", "d");

        doReturn(abi).when(abiResolver).getAbi();
        doReturn(config).when(appProcessConfigProvider).provideAppConfig(context, abi);
        doReturn(libDir).when(libDirInsideApk).call();
        when(fileProvider.getFileByNonNullPath(libDir)).thenReturn(libDirFile);
        when(fileProvider.getFileByNonNullPath(libDirFile, defaultLibNames.get(0))).thenReturn(handler);
        doReturn(true).when(handler).exists();
        when(handler.getAbsolutePath()).thenReturn(pathToHandler);

        ExtractorResult result = crashpadExtractorWrapper.extractForMP();

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(result.pathToHandler).as("pathToHandler").isEqualTo(pathToHandler);
        assertions.assertThat(result.useLinker).as("useLinker").isFalse();
        assertions.assertThat(result.is64bit).as("is64bit").isEqualTo(Process.is64Bit());
        assertions.assertThat(result.appProcessConfig).as("appProcessConfig").isSameAs(config);

        assertions.assertAll();
    }

    @Test
    public void mpBestCaseOldFile() throws Exception {
        String abi = "x86";
        String libDir = "/some/lib/dir";
        File libDirFile = mock(File.class);
        File handler = mock(File.class);
        String pathToHandler = "/some/lib/dir/" + defaultLibNames.get(1);
        AppProcessConfig config = new AppProcessConfig("a", "l", "d");

        doReturn(abi).when(abiResolver).getAbi();
        doReturn(config).when(appProcessConfigProvider).provideAppConfig(context, abi);
        doReturn(libDir).when(libDirInsideApk).call();
        when(fileProvider.getFileByNonNullPath(libDir)).thenReturn(libDirFile);
        when(fileProvider.getFileByNonNullPath(libDirFile, defaultLibNames.get(0))).thenReturn(mock(File.class));
        when(fileProvider.getFileByNonNullPath(libDirFile, defaultLibNames.get(1))).thenReturn(handler);
        doReturn(true).when(handler).exists();
        when(handler.getAbsolutePath()).thenReturn(pathToHandler);

        ExtractorResult result = crashpadExtractorWrapper.extractForMP();

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(result.pathToHandler).as("pathToHandler").isEqualTo(pathToHandler);
        assertions.assertThat(result.useLinker).as("useLinker").isFalse();
        assertions.assertThat(result.is64bit).as("is64bit").isEqualTo(Process.is64Bit());
        assertions.assertThat(result.appProcessConfig).as("appProcessConfig").isSameAs(config);

        assertions.assertAll();
    }

    @Test
    public void mpWithoutLibDir() throws Exception {
        String abi = "x86";
        AppProcessConfig config = new AppProcessConfig("a", "l", "d");

        doReturn(abi).when(abiResolver).getAbi();
        doReturn(config).when(appProcessConfigProvider).provideAppConfig(context, abi);
        doReturn(null).when(libDirInsideApk).call();

        ExtractorResult result = crashpadExtractorWrapper.extractForMP();

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(result.pathToHandler).as("pathToHandler").isEqualTo("stub");
        assertions.assertThat(result.useLinker).as("useLinker").isFalse();
        assertions.assertThat(result.is64bit).as("is64bit").isEqualTo(Process.is64Bit());
        assertions.assertThat(result.appProcessConfig).as("appProcessConfig").isSameAs(config);

        assertions.assertAll();
    }

    @Test
    public void mpWithoutConfig() throws Exception {
        String abi = "x86";
        String libDir = "/some/lib/dir";

        doReturn(abi).when(abiResolver).getAbi();
        doReturn(null).when(appProcessConfigProvider).provideAppConfig(context, abi);
        doReturn(libDir).when(libDirInsideApk).call();

        assertThat(crashpadExtractorWrapper.extractForMP()).isNull();

        verifyZeroInteractions(libDirInsideApk);
    }

    @Test
    public void suffixMatcherNotMatched() {
        File file = mock(File.class);
        String suffix = "suffix";
        doReturn("somename" + suffix + "1").when(file).getName();

        CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher matcher = new CrashpadExtractorWrapper.FileNameWithoutSuffixMatcher(suffix);

        assertThat(matcher.apply(file)).isTrue();
    }

    @Test
    public void allFilesMatcher() {
        File file = mock(File.class);
        assertThat(new CrashpadExtractorWrapper.AllFilesMatcher().apply(file)).isTrue();
        verifyZeroInteractions(file);
    }

}
