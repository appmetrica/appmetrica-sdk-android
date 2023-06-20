package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.shadows.localsocket.LocalServerSocketShadow;
import io.appmetrica.analytics.testutils.shadows.localsocket.LocalSocketShadow;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(shadows={LocalServerSocketShadow.class, LocalSocketShadow.class})
public class CrashpadCrashWatcherTest extends CommonTest {

    private static final String TEST_CRASHPAD_SOCKET = "testCrashpadSocket";
    private static final String TEST_CRASHPAD_DB_DIR = "native_crashes";

    @Mock
    private CrashpadLoader loader;
    @Mock
    private Consumer<String> setuper;

    private CrashpadCrashWatcher watcher;

    private LocalSocket socket;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        watcher = new CrashpadCrashWatcher(TEST_CRASHPAD_SOCKET, TEST_CRASHPAD_DB_DIR, loader, setuper);
        socket = new LocalSocket();
        doReturn(true).when(loader).loadIfNeeded();
    }

    @After
    public void tearDown() throws IOException {
        socket.close();
    }

    @Test
    public void directoryIsNull() {
        watcher = new CrashpadCrashWatcher(TEST_CRASHPAD_SOCKET, null, loader, setuper);
        Consumer<String> consumer = mock(Consumer.class);
        watcher.subscribe(consumer);
        verify(loader, never()).loadIfNeeded();
    }

    @Test
    public void simpleConsumerTest() throws IOException {
        Consumer<String> consumer = mock(Consumer.class);
        watcher.subscribe(consumer);

        String uuid = "test-uuid";
        sendMessage(TEST_CRASHPAD_SOCKET, uuid);

        verify(loader).loadIfNeeded();
        verify(setuper).consume(TEST_CRASHPAD_DB_DIR);

        verify(consumer, timeout(1000).times(1)).consume(uuid);
    }

    @Test
    public void consumerNotCalledAfterUnsubscribe() throws IOException {
        Consumer<String> consumer = mock(Consumer.class);
        watcher.subscribe(consumer);

        String uuid = "test-uuid";
        watcher.unsubscribe(consumer);

        sendMessage(TEST_CRASHPAD_SOCKET, uuid);

        verify(consumer, timeout(1000).times(0)).consume(anyString());
    }

    @Test
    public void consumerNotCalledIfLibraryNotLoaded() throws IOException {
        Consumer<String> consumer = mock(Consumer.class);
        doReturn(false).when(loader).loadIfNeeded();
        watcher.subscribe(consumer);

        String uuid = "test-uuid";

        sendMessage(TEST_CRASHPAD_SOCKET, uuid);

        verify(consumer, timeout(1000).times(0)).consume(anyString());
    }

    @Test
    public void consumerNotCalledAfterErrorDuringSetup() throws IOException {
        Consumer<String> consumer = mock(Consumer.class);
        doThrow(new UnsatisfiedLinkError()).when(consumer).consume(anyString());
        watcher.subscribe(consumer);

        String uuid = "test-uuid";

        sendMessage(TEST_CRASHPAD_SOCKET, uuid);

        verify(consumer, timeout(1000).times(0)).consume(anyString());
    }

    private void sendMessage(String to, String message) throws IOException {
        socket.connect(new LocalSocketAddress(to));
        socket.getOutputStream().write(message.getBytes());
    }

}
