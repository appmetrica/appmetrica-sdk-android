package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReadOldCrashesRunnableTest extends CommonTest {

    @Mock
    private Context context;
    @Mock
    private File crashDirectory;
    @Mock
    private Consumer<File> newCrashListener;

    private ReadOldCrashesRunnable readOldCrashesRunnable;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        readOldCrashesRunnable = new ReadOldCrashesRunnable(
            context,
            crashDirectory,
            newCrashListener
        );
    }

    @Test
    public void testNoDirectory() {
        doReturn(false).when(crashDirectory).exists();
        readOldCrashesRunnable.run();
        verifyNoMoreInteractions(newCrashListener);
    }

    @Test
    public void testIsNotDirectory() {
        doReturn(true).when(crashDirectory).exists();
        doReturn(false).when(crashDirectory).isDirectory();
        readOldCrashesRunnable.run();
        verifyNoMoreInteractions(newCrashListener);
    }

    @Test
    public void testConsumeAllFiles() throws Throwable {
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
        InOrder inOrder = Mockito.inOrder(newCrashListener);
        inOrder.verify(newCrashListener).consume(file1);
        inOrder.verify(newCrashListener).consume(file2);
        inOrder.verify(newCrashListener).consume(file3);
    }

}
