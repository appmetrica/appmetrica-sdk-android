package io.appmetrica.analytics.impl.crash;

import android.os.FileObserver;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CrashFileObserverTest extends CommonTest {

    @Mock
    private Consumer<File> listener;
    @Mock
    private FileProvider fileProvider;
    @Mock
    private File file;
    private final File directory = new File("/");

    private CrashFileObserver observer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        observer = new CrashFileObserver(directory, listener, fileProvider);
    }

    @Test
    public void testCallListener() {
        String somePath = "somePath";
        when(fileProvider.getFileByNonNullPath(directory, somePath)).thenReturn(file);
        observer.onEvent(FileObserver.CLOSE_WRITE, somePath);
        verify(listener).consume(file);
    }

    @Test
    public void testNotCallListenerForEmptyPath() {
        String somePath = "";
        observer.onEvent(FileObserver.CLOSE_WRITE, somePath);
        verify(listener, never()).consume(any(File.class));
    }

}
