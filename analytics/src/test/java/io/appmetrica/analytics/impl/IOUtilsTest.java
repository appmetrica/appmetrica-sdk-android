package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class IOUtilsTest extends CommonTest {

    private Context context;

    private static final String FILE_NAME = "test_file.dat";
    private static final String FILE_VALUE = "test file value";

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.getApplication();
    }

    @Test
    public void fileStreamToString() throws IOException {
        writeFile(FILE_NAME, FILE_VALUE);
        assertThat(IOUtils.toString(new FileInputStream(getFilePath(FILE_NAME)))).isEqualTo(FILE_VALUE);
    }

    @Test
    public void getStringFileLocked() throws IOException {
        writeFile(FILE_NAME, FILE_VALUE);
        assertThat(IOUtils.getStringFileLocked(new File(getFilePath(FILE_NAME)))).isEqualTo(FILE_VALUE);
    }

    @Test
    public void getStringFileLockedIfFileNotExists() throws IOException {
        assertThat(IOUtils.getStringFileLocked(new File(getFilePath(FILE_NAME)))).isNull();
    }

    @Test
    public void readFileLockedIfFileIsNull() throws IOException {
        assertThat(IOUtils.getStringFileLocked(null)).isNull();
    }

    @Test
    public void readFileLockedIfFileDoesNotExist() throws IOException {
        assertThat(IOUtils.getStringFileLocked(new File(getFilePath(FILE_NAME)))).isNull();
    }

    @Test
    public void readFileLockedIfFileExists() throws IOException {
        writeFile(FILE_NAME, FILE_VALUE);
        assertThat(IOUtils.readFileLocked(new File(getFilePath(FILE_NAME)))).isEqualTo(FILE_VALUE.getBytes());
    }

    private void writeFile(String fileName, String value) throws IOException {
        IOUtils.writeStringFileLocked(value, fileName, new FileOutputStream(new File(getFilePath(fileName))));
    }

    private String getFilePath(String fileName) {
        return new File(context.getFilesDir(), fileName).getAbsolutePath();
    }
}
