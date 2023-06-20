package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Build;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FileProviderTest extends CommonTest {

    private Context mContext;
    private FileProvider mFileProvider;
    private final String mCurrentDir = new File("").getAbsolutePath();
    private final File mCurrentDirFile = new File("").getAbsoluteFile();
    @Mock
    private File file;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        File mFile = new File(mCurrentDir, "appmetrica path");
        when(mContext.getFilesDir()).thenReturn(mFile);
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            when(mContext.getNoBackupFilesDir()).thenReturn(mFile);
        }
        mFileProvider = new FileProvider();
    }

    @Test
    public void testGetFileByPath() {
        assertThat(mFileProvider.getFileByPath(mCurrentDir).getAbsolutePath()).isEqualTo(mCurrentDir);
    }

    @Test
    public void testGetFileByPathNull() {
        assertThat(mFileProvider.getFileByPath(null)).isNull();
    }

    @Test
    public void testGetFileByNonNullPath() {
        assertThat(mFileProvider.getFileByNonNullPath(mCurrentDir).getAbsolutePath()).isEqualTo(mCurrentDir);
    }

    @Test
    public void testGetFileByPathWithName() {
        assertThat(mFileProvider.getFileByPath(mCurrentDirFile, "name").getAbsolutePath())
                .isEqualTo(mCurrentDir + "/name");
    }

    @Test
    public void testGetFileByPathWithNameNull() {
        assertThat(mFileProvider.getFileByPath(null, "name")).isNull();
    }

    @Test
    public void testGetFileByNonNullPathWithName() {
        assertThat(mFileProvider.getFileByNonNullPath(mCurrentDirFile, "name").getAbsolutePath())
                .isEqualTo(mCurrentDir + "/name");
    }

    @Test
    public void testGetAbsoluteFileByPath() {
        assertThat(mFileProvider.getAbsoluteFileByPath(mCurrentDirFile, "name"))
                .isEqualTo(new File(mCurrentDir + "/name"));
    }

    @Test
    public void testGetAbsoluteFileByPathNull() {
        assertThat(mFileProvider.getAbsoluteFileByPath(null, "name")).isNull();
    }

    @Test
    public void testGetFileInNonNullDirectory() {
        final String fileName = "filename";
        assertThat(mFileProvider.getFileInNonNullDirectory(new File(mCurrentDir), fileName).getAbsolutePath()).isEqualTo(mCurrentDir + "/" + fileName);
    }

    @Test
    public void getDbFileFromFileStorage() {
        String dbName = "test.db";
        when(mContext.getDatabasePath(dbName)).thenReturn(file);
        assertThat(mFileProvider.getDbFileFromFilesStorage(mContext, dbName)).isEqualTo(file);
    }

    @Test
    public void getDbFileFromFileStorageNull() {
        String dbName = "test.db";
        when(mContext.getDatabasePath(dbName)).thenReturn(null);
        assertThat(mFileProvider.getDbFileFromFilesStorage(mContext, dbName)).isNull();
    }

    @Test
    public void getDbFileFromNoBackupStorage() {
        String dbName = "test.db";
        when(mContext.getNoBackupFilesDir()).thenReturn(RuntimeEnvironment.getApplication().getNoBackupFilesDir());
        assertThat(mFileProvider.getDbFileFromNoBackupStorage(mContext, dbName))
                .isEqualTo(new File(RuntimeEnvironment.getApplication().getNoBackupFilesDir(), dbName));
    }

    @Test
    public void getDbFileFromNoBackupStorageNull() {
        String dbName = "test.db";
        when(mContext.getNoBackupFilesDir()).thenReturn(null);
        assertThat(mFileProvider.getDbFileFromNoBackupStorage(mContext, dbName)).isNull();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void getLibFolderDataDir() {
        when(mContext.getDataDir()).thenReturn(RuntimeEnvironment.getApplication().getDataDir());
        assertThat(mFileProvider.getLibFolder(mContext))
                .isEqualTo(new File(RuntimeEnvironment.getApplication().getDataDir(), "lib"));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void getLibFolderDataDirIsNull() {
        when(mContext.getDataDir()).thenReturn(null);
        assertThat(mFileProvider.getLibFolder(mContext)).isNull();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void getLibFolderFilesDir() {
        when(mContext.getFilesDir()).thenReturn(RuntimeEnvironment.getApplication().getFilesDir());
        assertThat(mFileProvider.getLibFolder(mContext))
                .isEqualTo(new File(RuntimeEnvironment.getApplication().getFilesDir().getParentFile(), "lib"));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void getLibFolderFilesDirIsNull() {
        when(mContext.getFilesDir()).thenReturn(null);
        assertThat(mFileProvider.getLibFolder(mContext)).isNull();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void getLibFolderFilesDirParentIsNull() {
        File filesDir = mock(File.class);
        when(filesDir.getParentFile()).thenReturn(null);
        when(mContext.getFilesDir()).thenReturn(filesDir);
        assertThat(mFileProvider.getLibFolder(mContext)).isNull();
    }
}
