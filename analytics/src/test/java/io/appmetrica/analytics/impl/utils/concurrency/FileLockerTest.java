package io.appmetrica.analytics.impl.utils.concurrency;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class FileLockerTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    private static final String sPackage = "ru.yandex.metrica";
    private static final String sDelimeter = "_";
    private static final String sApiKey = "5012c3cc-20a4-4dac-92d1-83ebc27c0fa9";

    @ParameterizedRobolectricTestRunner.Parameters(name = "For file name = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + "null"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + "null" + "-journal"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + "null" + "-shm"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + "null" + "-wal"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + "-journal"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + "-shm"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + "-wal"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + sApiKey},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + sApiKey + "-journal"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + sApiKey + "-shm"},
            new Object[]{Constants.OLD_COMPONENT_DATABASE_PREFIX + sPackage + sDelimeter + sApiKey + "-wal"},
            new Object[]{Constants.OLD_CLIENT_DATABASE},
            new Object[]{Constants.OLD_SERVICE_DATABASE},
            new Object[]{"18883-61451b00-1102-4008-9e99-8247c7d2df8"}
        );
    }

    @Rule
    public final MockedStaticRule<FileUtils> fileUtilsMockedStaticRule = new MockedStaticRule<>(FileUtils.class);

    private final File lockFile = new File("lock");
    private Context context;
    private final String name;
    private FileLocker mDbFileLock;

    public FileLockerTest(@NonNull String name) {
        this.name = name;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = contextRule.getContext();
        when(FileUtils.getFileFromSdkStorage(context, name + ".lock")).thenReturn(lockFile);
        mDbFileLock = new FileLocker(context, name);
    }

    @After
    public void tearDown() {
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

    @Test
    public void lockThenUnlock() throws Throwable {
        mDbFileLock.lock();
        mDbFileLock.unlock();
    }

    @Test
    public void lockThenUnlockAndClear() throws Throwable {
        mDbFileLock.lock();
        mDbFileLock.unlockAndClear();
    }

    @Test
    public void lockAndUnlockSeveralTimes() throws Throwable {
        mDbFileLock.lock();
        mDbFileLock.lock();
        mDbFileLock.unlock();
        mDbFileLock.unlock();
        mDbFileLock.unlock();
    }

    @Test(expected = IllegalStateException.class)
    public void lockFileIsNull() throws Throwable {
        when(FileUtils.getFileFromSdkStorage(eq(context), anyString())).thenReturn(null);
        mDbFileLock = new FileLocker(context, name);
        mDbFileLock.lock();
    }

    public void unlockFileIsNull() throws Throwable {
        when(FileUtils.getFileFromSdkStorage(eq(context), anyString())).thenReturn(null);
        try {
            mDbFileLock.lock();
        } catch (NullPointerException ignored) {
        }
        mDbFileLock.unlock();
    }

    @Test
    public void unlockWithoutLock() {
        mDbFileLock.unlock();
    }

    @Test
    public void unlockAndClearWithoutLock() {
        mDbFileLock.unlockAndClear();
    }
}
