package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import android.os.Build;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.db.FileConstants;
import io.appmetrica.analytics.impl.utils.UuidGenerator;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.LogRule;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.KITKAT, Build.VERSION_CODES.LOLLIPOP})
public class PersistentUuidHolderTest extends CommonTest {

    private Context context;
    @Mock
    private FileProvider fileProvider;
    @Mock
    private UuidGenerator uuidGenerator;

    private PersistentUuidHolder persistentUuidHolder;
    private File uuidFile;
    private String fileName = FileConstants.UUID_FILE_NAME;
    private final String knownUuid = UUID.randomUUID().toString();
    private final String generatedUuid = UUID.randomUUID().toString();
    private File storageDir =  null;

    @Rule
    public final LogRule logRule = new LogRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        FileUtils.resetSdkStorage();
        context = RuntimeEnvironment.getApplication();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            storageDir = RuntimeEnvironment.getApplication().getNoBackupFilesDir();
        } else {
            storageDir = RuntimeEnvironment.getApplication().getFilesDir();
        }
        File sdkDir = new File(storageDir, "/appmetrica/analytics");
        sdkDir.mkdirs();
        uuidFile = new File(sdkDir, FileConstants.UUID_FILE_NAME);
        when(fileProvider.getFileFromStorage(context, fileName)).thenReturn(uuidFile);
        when(uuidGenerator.generateUuid()).thenReturn(generatedUuid);

        persistentUuidHolder = new PersistentUuidHolder(context, fileProvider, uuidGenerator);
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.resetSdkStorage();
        uuidFile.delete();
        storageDir.delete();
    }

    @Test
    public void readUuid() throws Exception {
        String uuid = UUID.randomUUID().toString();
        IOUtils.writeStringFileLocked(uuid, fileName, new FileOutputStream(uuidFile));
        assertThat(persistentUuidHolder.readUuid()).isEqualTo(uuid);
    }

    @Test
    public void readUuidIfFileIsNull() {
        when(fileProvider.getFileFromStorage(context, fileName)).thenReturn(null);
        assertThat(persistentUuidHolder.readUuid()).isNull();
    }

    @Test
    public void handleUuid() {
        assertThat(persistentUuidHolder.handleUuid(null)).isEqualTo(generatedUuid);
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isEqualTo(generatedUuid);
    }

    @Test
    public void handleUuidNullFile() {
        when(fileProvider.getFileFromStorage(context, fileName)).thenReturn(null);
        assertThat(persistentUuidHolder.handleUuid(null)).isEqualTo(generatedUuid);
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isNull();
    }

    @Test
    public void handleUuidWithKnownUuid() {
        assertThat(persistentUuidHolder.handleUuid(knownUuid)).isEqualTo(knownUuid);
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isEqualTo(knownUuid);
    }

    @Test
    public void handleUuidWithKnownUuidNullFile() {
        when(fileProvider.getFileFromStorage(context, fileName)).thenReturn(null);
        assertThat(persistentUuidHolder.handleUuid(knownUuid)).isEqualTo(knownUuid);
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isNull();
    }
}
