package io.appmetrica.analytics.impl.crash;

import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReadOldCrashesRunnableTest extends CommonTest {

    @Mock
    private File crashDirectory;
    @Mock
    private Consumer<File> newCrashListener;
    @Mock
    private FileLocksHolder fileLocksHolder;

    private ReadOldCrashesRunnable readOldCrashesRunnable;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        readOldCrashesRunnable = new ReadOldCrashesRunnable(
                crashDirectory,
                newCrashListener,
                fileLocksHolder
        );
    }

    @Test
    public void testNoDirectory() {
        doReturn(false).when(crashDirectory).exists();
        readOldCrashesRunnable.run();
        verifyZeroInteractions(newCrashListener);
    }

    @Test
    public void testIsNotDirectory() {
        doReturn(true).when(crashDirectory).exists();
        doReturn(false).when(crashDirectory).isDirectory();
        readOldCrashesRunnable.run();
        verifyZeroInteractions(newCrashListener);
    }

    @Test
    public void testConsumeAllFiles() throws Throwable {
        ExclusiveMultiProcessFileLock file1Lock = mock(ExclusiveMultiProcessFileLock.class);
        ExclusiveMultiProcessFileLock file2Lock = mock(ExclusiveMultiProcessFileLock.class);
        ExclusiveMultiProcessFileLock file3Lock = mock(ExclusiveMultiProcessFileLock.class);
        when(fileLocksHolder.getOrCreate("file1")).thenReturn(file1Lock);
        when(fileLocksHolder.getOrCreate("file2")).thenReturn(file2Lock);
        when(fileLocksHolder.getOrCreate("file3")).thenReturn(file3Lock);
        doReturn(true).when(crashDirectory).exists();
        doReturn(true).when(crashDirectory).isDirectory();
        File file1 = mock(File.class);
        when(file1.getName()).thenReturn("file1");
        File file2 = mock(File.class);
        when(file2.getName()).thenReturn("file2");
        File file3 = mock(File.class);
        when(file3.getName()).thenReturn("file3");
        doReturn(new File[]{file1, file2, file3}).when(crashDirectory).listFiles();
        readOldCrashesRunnable.run();
        InOrder inOrder = Mockito.inOrder(file1Lock, file2Lock, file3Lock, newCrashListener);
        inOrder.verify(file1Lock).lock();
        inOrder.verify(newCrashListener).consume(file1);
        inOrder.verify(file1Lock).unlockAndClear();
        inOrder.verify(file2Lock).lock();
        inOrder.verify(newCrashListener).consume(file2);
        inOrder.verify(file2Lock).unlockAndClear();
        inOrder.verify(file3Lock).lock();
        inOrder.verify(newCrashListener).consume(file3);
        inOrder.verify(file3Lock).unlockAndClear();
    }

}
