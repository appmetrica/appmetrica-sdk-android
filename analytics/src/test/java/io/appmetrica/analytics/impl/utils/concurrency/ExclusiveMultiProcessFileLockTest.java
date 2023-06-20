package io.appmetrica.analytics.impl.utils.concurrency;

import android.content.Context;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ExclusiveMultiProcessFileLockTest extends CommonTest {

    @Rule
    public final MockedStaticRule<FileUtils> fileUtilsMockedStaticRule = new MockedStaticRule<>(FileUtils.class);

    private Context context;
    @Mock
    private ReentrantLock reentrantLock;
    @Mock
    private FileLocker fileLocker;
    @Mock
    private File file;

    private ExclusiveMultiProcessFileLock exclusiveMultiProcessFileLock;

    private final String simpleFileName = "fileName";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(FileUtils.getFileFromSdkStorage(context, simpleFileName + ".lock")).thenReturn(file);

        exclusiveMultiProcessFileLock = new ExclusiveMultiProcessFileLock(reentrantLock, fileLocker);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions(
                new ExclusiveMultiProcessFileLock(context, simpleFileName)
        )
                .withPrivateFields(true)
                .checkFieldNonNull("reentrantLock")
                .checkFieldRecursively("fileLocker", new Consumer<ObjectPropertyAssertions<FileLocker>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<FileLocker> assertions) {
                        try {
                            assertions.withPrivateFields(true)
                                    .checkField("lockFile", file)
                                    .checkAll();
                        } catch (Exception e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                })
                .checkAll();
    }

    @Test
    public void lock() throws Throwable {
        exclusiveMultiProcessFileLock.lock();
        InOrder inOrder = inOrder(reentrantLock, fileLocker);
        inOrder.verify(reentrantLock).lock();
        inOrder.verify(fileLocker).lock();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void unlock() throws Exception {
        exclusiveMultiProcessFileLock.unlock();
        InOrder inOrder = inOrder(reentrantLock, fileLocker);
        inOrder.verify(fileLocker).unlock();
        inOrder.verify(reentrantLock).unlock();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void unlockAndClear() throws Exception {
        exclusiveMultiProcessFileLock.unlockAndClear();
        InOrder inOrder = inOrder(reentrantLock, fileLocker);
        inOrder.verify(fileLocker).unlockAndClear();
        inOrder.verify(reentrantLock).unlock();
        inOrder.verifyNoMoreInteractions();
    }
}
