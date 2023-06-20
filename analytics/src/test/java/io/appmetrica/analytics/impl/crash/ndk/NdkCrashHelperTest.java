package io.appmetrica.analytics.impl.crash.ndk;

import android.content.Context;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class NdkCrashHelperTest extends CommonTest {

    private Context mContext;
    private NdkCrashHelper mNativeCrashesHelper;

    private final String someEnv = "someEnv";

    @Mock
    private FileProvider fileProvider;
    @Mock
    private NdkCrashInitializer initializer;
    @Rule
    public MockedStaticRule<FileUtils> sFileUtilsMockedRule = new MockedStaticRule<>(FileUtils.class);

    private final String crashSubdir = "subdircrash";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(FileUtils.getAppStorageDirectory(mContext)).thenReturn(new File("test"));

        mNativeCrashesHelper = Mockito.spy(new NdkCrashHelper(mContext, mock(List.class), fileProvider, mock(LibraryLoader.class)));
        doReturn(initializer).when(mNativeCrashesHelper).loadNativeCrashesLibrary();
        doReturn(crashSubdir).when(initializer).getFolderName();
    }

    @Test
    public void testLibraryNotLoadedByDefault() {
        verify(mContext, never()).getFilesDir();
    }

    @Test
    public void testLibraryNotLoadedAfterDisabled() {
        mNativeCrashesHelper.setReportsEnabled(false, TestData.TEST_UUID, someEnv);
        verify(mContext, never()).getFilesDir();
    }

    @Test
    public void testLibraryLoadedIfEnabled() {
        mNativeCrashesHelper.setReportsEnabled(true, TestData.TEST_UUID, someEnv);
        verify(fileProvider, times(1)).getStorageSubDirectory(mContext, crashSubdir);
    }

    @Test
    public void testLibraryNotLoadedIfNullDirectory() {
        when(fileProvider.getStorageSubDirectory(same(mContext), anyString())).thenReturn(null);
        mNativeCrashesHelper.setReportsEnabled(true, TestData.TEST_UUID, someEnv);
        verify(initializer, never()).setUpHandler(anyString(), anyString(), anyString());
    }

    @Test
    public void testLibraryLoadedOnlyOnce() {
        String subdir = "subdir";
        doReturn(subdir).when(fileProvider).getStorageSubDirectory(mContext, crashSubdir);
        mNativeCrashesHelper.setReportsEnabled(true, TestData.TEST_UUID, someEnv);
        verify(initializer, times(1)).setUpHandler(TestData.TEST_UUID, subdir, someEnv);
        reset(initializer);
        mNativeCrashesHelper.setReportsEnabled(true, TestData.TEST_UUID, someEnv);
        mNativeCrashesHelper.setReportsEnabled(true, TestData.TEST_UUID, someEnv);

        verifyNoMoreInteractions(initializer);
        verify(fileProvider, times(1)).getStorageSubDirectory(mContext, crashSubdir);
    }
}
