package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.ReporterEnvironment;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CrashToFileWriterTest extends CommonTest {

    private Context mContext;
    @Mock
    private FileProvider mFileProvider;
    @Mock
    private CrashFolderPreparer mCrashFolderPreparer;
    @Mock
    private File mCrashFolder;
    @Mock
    private ReportToSend mReport;
    @Mock
    private ReporterEnvironment mReporterEnvironment;
    @Mock
    private ProcessConfiguration mProcessConfiguration;
    @Mock
    private FileLocksHolder fileLocksHolder;
    private CrashToFileWriter mCrashToFileWriter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mFileProvider.getCrashesDirectory(mContext)).thenReturn(mCrashFolder);
        when(mReport.getEnvironment()).thenReturn(mReporterEnvironment);
        when(mReporterEnvironment.getProcessConfiguration()).thenReturn(mProcessConfiguration);
        when(mProcessConfiguration.getProcessID()).thenReturn(7766);
        when(mProcessConfiguration.getProcessSessionID()).thenReturn("112233");
        mCrashToFileWriter = new CrashToFileWriter(mContext, mFileProvider, mCrashFolderPreparer, fileLocksHolder);
    }

    @Test
    public void writeToFileCouldNotPrepareCrashFolder() {
        when(mCrashFolderPreparer.prepareCrashFolder(mCrashFolder)).thenReturn(false);
        mCrashToFileWriter.writeToFile(mReport);
        verifyZeroInteractions(mReport);
    }

    @Test
    public void writeToFile() throws Throwable {
        ExclusiveMultiProcessFileLock fileLock = mock(ExclusiveMultiProcessFileLock.class);
        String fileName = "7766-112233";
        when(fileLocksHolder.getOrCreate(fileName)).thenReturn(fileLock);
        when(mCrashFolderPreparer.prepareCrashFolder(mCrashFolder)).thenReturn(true);
        mCrashToFileWriter.writeToFile(mReport);
        InOrder inOrder = Mockito.inOrder(fileLock, mFileProvider);
        inOrder.verify(fileLock).lock();
        inOrder.verify(mFileProvider).getFileInNonNullDirectory(mCrashFolder, "7766-112233");
        inOrder.verify(fileLock).unlockAndClear();
    }
}
