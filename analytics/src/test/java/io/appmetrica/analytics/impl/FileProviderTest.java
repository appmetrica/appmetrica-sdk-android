package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class FileProviderTest extends CommonTest {

    private FileProvider mFileProvider;
    private final String mCurrentDir = new File("").getAbsolutePath();
    private final File mCurrentDirFile = new File("").getAbsoluteFile();

    @Before
    public void setUp() {
        mFileProvider = new FileProvider();
    }

    @Test
    public void testGetFileByNonNullPath() {
        assertThat(mFileProvider.getFileByNonNullPath(mCurrentDir).getAbsolutePath()).isEqualTo(mCurrentDir);
    }

    @Test
    public void testGetFileByNonNullPathWithName() {
        assertThat(mFileProvider.getFileByNonNullPath(mCurrentDirFile, "name").getAbsolutePath())
            .isEqualTo(mCurrentDir + "/name");
    }

    @Test
    public void testGetFileInNonNullDirectory() {
        final String fileName = "filename";
        assertThat(mFileProvider.getFileInNonNullDirectory(new File(mCurrentDir), fileName).getAbsolutePath()).isEqualTo(mCurrentDir + "/" + fileName);
    }
}
