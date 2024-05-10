package io.appmetrica.analytics.impl.referrer.client;

import android.os.Handler;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.impl.DeferredDeeplinkState;
import io.appmetrica.analytics.impl.DeferredDeeplinkStateManager;
import io.appmetrica.analytics.impl.ReferrerParser;
import io.appmetrica.analytics.impl.ReportsHandler;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReferrerHelperTest extends CommonTest {

    @Mock
    private ReportsHandler mReportsHandler;
    @Mock
    private Handler handler;
    @Mock
    private DeferredDeeplinkStateManager mDeferredDeeplinkStateManager;
    @Mock
    private ReferrerParser mReferrerParser;
    @Mock
    private PreferencesClientDbStorage mPreferencesClientDbStorage;
    @Mock
    private DeferredDeeplinkListener mDeeplinkListener;
    @Mock
    private DeferredDeeplinkParametersListener mDeeplinkParametersListener;
    private ReferrerHelper mReferrerHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void maybeRequestReferrerDeeplinkWasNotChecked() {
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        verify(mReportsHandler).reportRequestReferrerEvent(any(ReferrerResultReceiver.class));
    }

    @Test
    public void maybeRequestReferrerDeeplinkWasChecked() {
        initReferrerHelper(true);
        mReferrerHelper.maybeRequestReferrer();
        verify(mReportsHandler, never()).reportRequestReferrerEvent(any(ReferrerResultReceiver.class));
    }

    @Test
    public void retrieveReferrerOnReceive() {
        DeferredDeeplinkState state = mock(DeferredDeeplinkState.class);
        String referrer = "referrer";
        when(mReferrerParser.parseDeferredDeeplinkState(referrer)).thenReturn(state);
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.onReferrerChosen(new ReferrerInfo(referrer, 10, 20, ReferrerInfo.Source.GP));
        verify(mDeferredDeeplinkStateManager).onDeeplinkLoaded(state);
    }

    @Test
    public void retrieveReferrerOnReceiveDeeplinkAlreadyChecked() {
        DeferredDeeplinkState state = mock(DeferredDeeplinkState.class);
        String referrer = "referrer";
        when(mReferrerParser.parseDeferredDeeplinkState(referrer)).thenReturn(state);
        initReferrerHelper(true);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.onReferrerChosen(new ReferrerInfo(referrer, 10, 20, ReferrerInfo.Source.HMS));
        verify(mDeferredDeeplinkStateManager, never()).onDeeplinkLoaded(state);
    }

    @Test
    public void retrieveReferrerOnReceiveDeeplinkEmptyReferrer() {
        DeferredDeeplinkState state = mock(DeferredDeeplinkState.class);
        String referrer = "";
        when(mReferrerParser.parseDeferredDeeplinkState(referrer)).thenReturn(state);
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.onReferrerChosen(new ReferrerInfo(referrer, 10, 20, ReferrerInfo.Source.GP));
        verify(mDeferredDeeplinkStateManager).onDeeplinkLoaded(state);
    }

    @Test
    public void retrieveReferrerOnReceiveDeeplinkNullReferrer() {
        DeferredDeeplinkState state = mock(DeferredDeeplinkState.class);
        String referrer = null;
        when(mReferrerParser.parseDeferredDeeplinkState(referrer)).thenReturn(state);
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.onReferrerChosen(new ReferrerInfo(referrer, 10, 20, ReferrerInfo.Source.HMS));
        verify(mDeferredDeeplinkStateManager).onDeeplinkLoaded(state);
    }

    @Test
    public void retrieveReferrerOnReceiveDeeplinkNullReferrerInfo() {
        DeferredDeeplinkState state = mock(DeferredDeeplinkState.class);
        when(mReferrerParser.parseDeferredDeeplinkState(null)).thenReturn(state);
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.onReferrerChosen(null);
        verify(mDeferredDeeplinkStateManager).onDeeplinkLoaded(state);
    }

    @Test
    public void retrieveReferrerOnReceiveDeeplinkTwice() {
        DeferredDeeplinkState stateFromPrefs = mock(DeferredDeeplinkState.class);
        String referrerFromPrefs = "initial_referrer";
        when(mReferrerParser.parseDeferredDeeplinkState(referrerFromPrefs)).thenReturn(stateFromPrefs);
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();

        String newReferrer = "new_referrer";
        DeferredDeeplinkState newState = mock(DeferredDeeplinkState.class);
        when(mReferrerParser.parseDeferredDeeplinkState(newReferrer)).thenReturn(newState);
        mReferrerHelper.onReferrerChosen(new ReferrerInfo(newReferrer, 10, 20, ReferrerInfo.Source.GP));
        verify(mDeferredDeeplinkStateManager).onDeeplinkLoaded(newState);
        clearInvocations(mDeferredDeeplinkStateManager);
        mReferrerHelper.onReferrerChosen(new ReferrerInfo(newReferrer, 10, 20, ReferrerInfo.Source.HMS));
        verify(mDeferredDeeplinkStateManager).onDeeplinkLoaded(newState);
    }

    @Test
    public void requestDeferredDeeplink() {
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.requestDeferredDeeplink(mDeeplinkListener);
        InOrder inOrder = Mockito.inOrder(mDeferredDeeplinkStateManager, mPreferencesClientDbStorage);
        inOrder.verify(mDeferredDeeplinkStateManager).requestDeferredDeeplink(mDeeplinkListener);
        inOrder.verify(mPreferencesClientDbStorage).markDeferredDeeplinkChecked();
    }

    @Test
    public void requestDeferredDeeplinkParameters() {
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        mReferrerHelper.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        InOrder inOrder = Mockito.inOrder(mDeferredDeeplinkStateManager, mPreferencesClientDbStorage);
        inOrder.verify(mDeferredDeeplinkStateManager).requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        inOrder.verify(mPreferencesClientDbStorage).markDeferredDeeplinkChecked();
    }

    @Test(expected = Exception.class)
    public void requestDeferredDeeplinkThrowsException() {
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        doThrow(new RuntimeException()).when(mDeferredDeeplinkStateManager).requestDeferredDeeplink(mDeeplinkListener);
        mReferrerHelper.requestDeferredDeeplink(mDeeplinkListener);
        verify(mPreferencesClientDbStorage).markDeferredDeeplinkChecked();
    }

    @Test(expected = Exception.class)
    public void requestDeferredDeeplinkParametersThrowsException() {
        initReferrerHelper(false);
        mReferrerHelper.maybeRequestReferrer();
        doThrow(new RuntimeException()).when(mDeferredDeeplinkStateManager).requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mReferrerHelper.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mPreferencesClientDbStorage).markDeferredDeeplinkChecked();
    }

    private void initReferrerHelper(boolean wasDeeplinkChecked) {
        mReferrerHelper = new ReferrerHelper(
                mReportsHandler,
                mPreferencesClientDbStorage,
                handler,
                wasDeeplinkChecked,
                mDeferredDeeplinkStateManager,
                mReferrerParser
        );
    }
}
