package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import io.appmetrica.analytics.impl.crash.ndk.LibraryLoader;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class CrashpadLoaderTest extends CommonTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private LibraryLoader loader;
    @InjectMocks
    private CrashpadLoader crashpadLoader;

    @Test
    public void fineCase() {
        assertThat(crashpadLoader.loadIfNeeded()).isTrue();
        verify(loader).loadLibrary("appmetrica-service-native");
    }

    @Test
    public void loadAlreadyLoadedLibrary() {
        crashpadLoader.loadIfNeeded();
        reset(loader);
        assertThat(crashpadLoader.loadIfNeeded()).isTrue();
        verifyZeroInteractions(loader);
    }

    @Test
    public void loadingError() {
        doThrow(new IllegalStateException()).when(loader).loadLibrary("appmetrica-service-native");
        assertThat(crashpadLoader.loadIfNeeded()).isFalse();
    }

    @Test
    public void loadAfterLoadingError() {
        doThrow(new IllegalStateException()).when(loader).loadLibrary("appmetrica-service-native");
        crashpadLoader.loadIfNeeded();
        reset(loader);
        assertThat(crashpadLoader.loadIfNeeded()).isFalse();
    }

}
