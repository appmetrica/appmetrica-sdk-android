package io.appmetrica.analytics.impl;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DataResultReceiverTest extends CommonTest {

    @Mock
    private DataResultReceiver.Receiver mReceiver;
    @Mock
    private Handler mHandler;
    @Mock
    private ClientIdentifiersHolder mClientIdentifiersHolder;

    private DataResultReceiver mDataResultReceiver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mDataResultReceiver = new DataResultReceiver(mHandler, mReceiver);
    }

    @Test
    public void testOnReceiveResult() {
        final int code = 200;
        Bundle bundle = mock(Bundle.class);
        mDataResultReceiver.onReceiveResult(code, bundle);
    }

    @Test
    public void testNotifyOnStartupUpdatedNullReceiver() {
        DataResultReceiver.notifyOnStartupUpdated(null, mClientIdentifiersHolder);
    }

    @Test
    public void testNotifyOnStartupUpdated() {
        ResultReceiver resultReceiver = mock(ResultReceiver.class);
        DataResultReceiver.notifyOnStartupUpdated(resultReceiver, mClientIdentifiersHolder);
        ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(mClientIdentifiersHolder).toBundle(captor.capture());
        verify(resultReceiver).send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, captor.getValue());
    }

    @Test
    public void testNotifyOnStartupError() {
        ResultReceiver resultReceiver = mock(ResultReceiver.class);
        final StartupError error = StartupError.NETWORK;
        DataResultReceiver.notifyOnStartupError(resultReceiver, error, mClientIdentifiersHolder);
        ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(mClientIdentifiersHolder).toBundle(captor.capture());
        verify(resultReceiver).send(DataResultReceiver.RESULT_CODE_STARTUP_ERROR, captor.getValue());
    }

    @Test
    public void testNotifyOnStartupErrorNullClientIdentifiersHolder() {
        ResultReceiver resultReceiver = mock(ResultReceiver.class);
        final StartupError error = StartupError.NETWORK;
        DataResultReceiver.notifyOnStartupError(resultReceiver, error, null);
        ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(resultReceiver).send(eq(DataResultReceiver.RESULT_CODE_STARTUP_ERROR), captor.capture());
        assertThat(StartupError.fromBundle(captor.getValue())).isEqualTo(error);
    }

    @Test
    public void testNotifyOnStartupErrorNullReceiver() {
        DataResultReceiver.notifyOnStartupError(null, StartupError.NETWORK, mClientIdentifiersHolder);
    }
}
