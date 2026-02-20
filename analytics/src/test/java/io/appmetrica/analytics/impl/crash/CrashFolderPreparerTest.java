package io.appmetrica.analytics.impl.crash;

import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CrashFolderPreparerTest extends CommonTest {

    @Mock
    private File crashesFolder;
    private final CrashFolderPreparer crashFolderPreparer = new CrashFolderPreparer();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void directoryIsNull() {
        assertThat(crashFolderPreparer.prepareCrashFolder(null)).isFalse();
    }

    @Test
    public void testDirectoryCreated() {
        doReturn(false).when(crashesFolder).exists();
        doReturn(true).when(crashesFolder).mkdir();

        assertThat(crashFolderPreparer.prepareCrashFolder(crashesFolder)).isTrue();

        verify(crashesFolder).mkdir();
    }

    @Test
    public void testDirectoryNotCreated() {
        doReturn(false).when(crashesFolder).exists();
        doReturn(false).when(crashesFolder).mkdir();

        assertThat(crashFolderPreparer.prepareCrashFolder(crashesFolder)).isFalse();

        verify(crashesFolder).mkdir();
    }

    @Test
    public void testDirectoryExists() {
        doReturn(true).when(crashesFolder).exists();
        doReturn(true).when(crashesFolder).isDirectory();

        assertThat(crashFolderPreparer.prepareCrashFolder(crashesFolder)).isTrue();

        verify(crashesFolder, never()).mkdir();
        verify(crashesFolder).isDirectory();
    }

    @Test
    public void testFileExists() {
        doReturn(true).when(crashesFolder).exists();
        doReturn(false).when(crashesFolder).isDirectory();
        doReturn(true).when(crashesFolder).delete();
        doReturn(true).when(crashesFolder).mkdir();

        assertThat(crashFolderPreparer.prepareCrashFolder(crashesFolder)).isTrue();

        verify(crashesFolder).delete();
        verify(crashesFolder).mkdir();
    }

    @Test
    public void testFailedToReplaceFileAndMakeDir() {
        doReturn(true).when(crashesFolder).exists();
        doReturn(false).when(crashesFolder).isDirectory();
        doReturn(true).when(crashesFolder).delete();
        doReturn(false).when(crashesFolder).mkdir();

        assertThat(crashFolderPreparer.prepareCrashFolder(crashesFolder)).isFalse();

        verify(crashesFolder).delete();
        verify(crashesFolder).mkdir();
    }

    @Test
    public void testFailedToReplaceFile() {
        doReturn(true).when(crashesFolder).exists();
        doReturn(false).when(crashesFolder).isDirectory();
        doReturn(false).when(crashesFolder).delete();

        assertThat(crashFolderPreparer.prepareCrashFolder(crashesFolder)).isFalse();

        verify(crashesFolder).delete();
    }

    @Test
    public void testMakeDirectory() {
        File file = mock(File.class);
        doReturn(true).when(file).mkdir();
        assertThat(crashFolderPreparer.makeCrashesFolder(file)).isTrue();
    }

    @Test
    public void testFailedToMakeDirectory() {
        File file = mock(File.class);
        doReturn(false).when(file).mkdir();
        assertThat(crashFolderPreparer.makeCrashesFolder(file)).isFalse();
    }

}
