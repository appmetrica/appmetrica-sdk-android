package io.appmetrica.analytics.impl.referrer.service;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReferrerFromLibraryRetrieverTest extends CommonTest {

    @Mock
    private InstallReferrerClient mClient;
    @Mock
    private ReferrerReceivedListener mListener;
    @Mock
    private ICommonExecutor executor;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    private ReferrerFromLibraryRetriever mReferrerFromLibraryRetriever;

    @Before
    public void setUp() throws Throwable {
        MockitoAnnotations.openMocks(this);
        mReferrerFromLibraryRetriever = new ReferrerFromLibraryRetriever(mClient, executor);
    }

    @Test
    public void testRetrieveReferrerOK() throws Throwable {
        final String installReferrer = "some test referrer";
        final long clickTimestamp = 438573450;
        final long installTimestamp = 3288994;
        ReferrerDetails details = mock(ReferrerDetails.class);
        when(details.getInstallReferrer()).thenReturn(installReferrer);
        when(details.getReferrerClickTimestampSeconds()).thenReturn(clickTimestamp);
        when(details.getInstallBeginTimestampSeconds()).thenReturn(installTimestamp);
        when(mClient.getInstallReferrer()).thenReturn(details);
        mReferrerFromLibraryRetriever.retrieveReferrer(mListener);
        ArgumentCaptor<InstallReferrerStateListener> referrerListenerCaptor = ArgumentCaptor.forClass(InstallReferrerStateListener.class);
        verify(mClient).startConnection(referrerListenerCaptor.capture());
        referrerListenerCaptor.getValue().onInstallReferrerSetupFinished(0);
        verify(mClient).endConnection();
        verifyNoInteractions(mListener);
        clearInvocations(mClient);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(mClient, never()).getInstallReferrer();
        verify(mListener).onReferrerReceived(argThat(new ArgumentMatcher<ReferrerInfo>() {
            @Override
            public boolean matches(ReferrerInfo argument) {
                return argument.installReferrer.equals(installReferrer) &&
                        argument.installBeginTimestampSeconds == installTimestamp &&
                        argument.referrerClickTimestampSeconds == clickTimestamp &&
                        argument.source == ReferrerInfo.Source.GP;
            }
        }));
        verify(mListener, never()).onReferrerRetrieveError(nullable(Throwable.class));
    }

    @Test
    public void testRetrieveReferrerThrows() throws Throwable {
        RuntimeException exception = new RuntimeException();
        when(mClient.getInstallReferrer()).thenThrow(exception);
        mReferrerFromLibraryRetriever.retrieveReferrer(mListener);
        ArgumentCaptor<InstallReferrerStateListener> referrerListenerCaptor = ArgumentCaptor.forClass(InstallReferrerStateListener.class);
        verify(mClient).startConnection(referrerListenerCaptor.capture());
        referrerListenerCaptor.getValue().onInstallReferrerSetupFinished(0);
        verify(mClient).endConnection();
        verifyNoInteractions(mListener);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(mListener, never()).onReferrerReceived(nullable(ReferrerInfo.class));
        verify(mListener).onReferrerRetrieveError(exception);
    }

    @Test
    public void testRetrieverReferrerBadCode() throws Throwable {
        when(mClient.getInstallReferrer()).thenReturn(mock(ReferrerDetails.class));
        mReferrerFromLibraryRetriever.retrieveReferrer(mListener);
        ArgumentCaptor<InstallReferrerStateListener> referrerListenerCaptor = ArgumentCaptor.forClass(InstallReferrerStateListener.class);
        verify(mClient).startConnection(referrerListenerCaptor.capture());
        referrerListenerCaptor.getValue().onInstallReferrerSetupFinished(1);
        verify(mClient).endConnection();
        verifyNoInteractions(mListener);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(mListener, never()).onReferrerReceived(nullable(ReferrerInfo.class));
        verify(mListener).onReferrerRetrieveError(argThat(new ArgumentMatcher<Throwable>() {
            @Override
            public boolean matches(Throwable argument) {
                return argument.getMessage().equals("Referrer check failed with error 1");
            }
        }));
    }
}
