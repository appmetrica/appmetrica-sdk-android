package io.appmetrica.analytics.impl.crash.ndk;

import android.content.Context;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(RobolectricTestRunner.class)
public class NdkCrashHelperChooseProviderTest extends CommonTest {

    @Mock
    private Context context;
    @Mock
    private NdkCrashInitializer initializer1;
    @Mock
    private NdkCrashInitializer initializer2;
    @Mock
    private FileProvider fileProvider;
    @Mock
    private LibraryLoader libraryLoader;

    private NdkCrashHelper helper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        helper = new NdkCrashHelper(context, Arrays.asList(initializer1, initializer2), fileProvider, libraryLoader);
    }

    @Test
    public void firstLibLoaded() {
        assertThat(helper.loadNativeCrashesLibrary()).isSameAs(initializer1);
    }

    @Test
    public void secondLibLoaded() {
        String firstLib = "firstLib";

        doReturn(firstLib).when(initializer1).getLibraryName();
        doThrow(new UnsatisfiedLinkError()).when(libraryLoader).loadLibrary(firstLib);

        assertThat(helper.loadNativeCrashesLibrary()).isSameAs(initializer2);
    }

    @Test
    public void noLibsLoaded() {
        String firstLib = "firstLib";
        String secondLib = "secondLib";

        doReturn(firstLib).when(initializer1).getLibraryName();
        doReturn(secondLib).when(initializer2).getLibraryName();
        doThrow(new UnsatisfiedLinkError()).when(libraryLoader).loadLibrary(firstLib);
        doThrow(new UnsatisfiedLinkError()).when(libraryLoader).loadLibrary(secondLib);

        assertThat(helper.loadNativeCrashesLibrary()).isNull();
    }

}
