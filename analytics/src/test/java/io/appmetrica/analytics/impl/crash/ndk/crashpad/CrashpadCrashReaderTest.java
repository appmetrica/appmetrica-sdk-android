package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class CrashpadCrashReaderTest extends CommonTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    @NonNull
    private CrashpadCrashReporter reporter;
    @Mock
    @NonNull
    private CrashpadCrashParser parser;
    @Mock
    @NonNull
    private Function<String, Bundle> crashReader;
    @Mock
    @NonNull
    private Callable<List<Bundle>> oldCrashesReader;
    @Mock
    @NonNull
    private Consumer<String> completedConsumer;
    @InjectMocks
    private CrashpadCrashReader reader;
    @Mock
    @NonNull
    private Bundle data;
    @InjectMocks
    private CrashpadCrash crash;

    @Test
    public void currentSessionCrash() {
        String crashUuid = "crashUuid";
        doReturn(data).when(crashReader).apply(crashUuid);
        doReturn(crash).when(parser).apply(crashUuid, data);

        reader.handleRealtimeCrash(crashUuid);

        verifyZeroInteractions(completedConsumer);
        verify(reporter).reportCurrentSessionNativeCrash(crash);
    }

    @Test
    public void emptyCurrentSessionCrash() {
        String crashUuid = "crashUuid";

        doReturn(null).when(crashReader).apply(crashUuid);

        reader.handleRealtimeCrash(crashUuid);

        verify(completedConsumer).consume(crashUuid);

        verifyZeroInteractions(parser);
        verifyZeroInteractions(reporter);
    }

    @Test
    public void prevSessionCrashes() throws Exception {
        String crashUuid1 = "crashUuid1";
        String crashUuid2 = "crashUuid2";
        Bundle data2 = mock(Bundle.class);
        CrashpadCrash pair2 = mock(CrashpadCrash.class);

        doReturn(Arrays.asList(data, data2)).when(oldCrashesReader).call();
        doReturn(data).when(crashReader).apply(crashUuid1);
        doReturn(crashUuid1).when(data).getString(CrashpadCrashReport.ARGUMENT_UUID);
        doReturn(crashUuid2).when(data2).getString(CrashpadCrashReport.ARGUMENT_UUID);
        doReturn(data2).when(crashReader).apply(crashUuid2);
        doReturn(crash).when(parser).apply(crashUuid1, data);
        doReturn(pair2).when(parser).apply(crashUuid2, data2);

        reader.checkForPreviousSessionCrashes();

        verify(reporter).reportPrevSessionNativeCrash(crash);
        verify(reporter).reportPrevSessionNativeCrash(pair2);

        verifyZeroInteractions(completedConsumer);
        verifyNoMoreInteractions(reporter);
    }

    @Test
    public void emptyPrevSessionCrash() throws Exception {
        String crashUuid = "crashUuid";

        doReturn(null).when(crashReader).apply(crashUuid);
        doReturn(crashUuid).when(data).getString(CrashpadCrashReport.ARGUMENT_UUID);
        doReturn(Collections.singletonList(data)).when(oldCrashesReader).call();

        reader.checkForPreviousSessionCrashes();

        verify(completedConsumer).consume(crashUuid);
        verifyZeroInteractions(reporter);
    }

    @Test
    public void noPrevCrashes() throws Exception {
        String crashUuid = "crashUuid";

        doReturn(null).when(crashReader).apply(crashUuid);
        doReturn(Collections.emptyList()).when(oldCrashesReader).call();

        reader.checkForPreviousSessionCrashes();

        verifyZeroInteractions(completedConsumer);
        verifyZeroInteractions(reporter);
    }

    @Test
    public void errorReadingPrevCrashes() throws Exception {
        String crashUuid = "crashUuid";

        doReturn(null).when(crashReader).apply(crashUuid);
        doThrow(new IllegalStateException()).when(oldCrashesReader).call();

        reader.checkForPreviousSessionCrashes();

        verifyZeroInteractions(completedConsumer);
        verifyZeroInteractions(parser);
        verifyZeroInteractions(reporter);
    }

}
