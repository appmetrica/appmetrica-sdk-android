package io.appmetrica.analytics.impl.crash.jvm;

import android.os.FileObserver;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.crash.CrashFolderPreparer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CrashDirectoryWatcherTest extends CommonTest {

    @Mock
    private FileObserver observer;
    @Mock
    private File crashDirectory;
    @Mock
    private CrashFolderPreparer crashFolderPreparer;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private CrashDirectoryWatcher watcher;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        watcher = new CrashDirectoryWatcher(
            observer,
            crashDirectory,
            crashFolderPreparer
        );
    }

    @Test
    public void testProxyStart() {
        watcher.startWatching();
        verify(crashFolderPreparer).prepareCrashFolder(crashDirectory);
        verify(observer).startWatching();
        verify(GlobalServiceLocator.getInstance().getReferenceHolder()).storeReference(observer);
    }

    @Test
    public void testProxyStop() {
        watcher.stopWatching();
        verify(observer).stopWatching();
    }
}
