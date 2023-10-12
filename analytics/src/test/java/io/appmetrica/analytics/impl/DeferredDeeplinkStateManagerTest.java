package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class DeferredDeeplinkStateManagerTest extends CommonTest {

    @Mock
    private DeferredDeeplinkListener mDeeplinkListener;
    @Mock
    private DeferredDeeplinkParametersListener mDeeplinkParametersListener;
    private final String mValidDeeplink = "valid_deeplink";
    private final String mUnparsedReferrer = "unparsed_referrer";
    private final Map<String, String> mValidParameters = new HashMap<String, String>();
    private DeferredDeeplinkState mValidState;
    private DeferredDeeplinkState mStateWithoutParameters;
    private DeferredDeeplinkState mStateWithoutDeeplink;
    private DeferredDeeplinkState mStateWithoutAnything;
    private DeferredDeeplinkStateManager mDeferredDeeplinkStateManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mValidParameters.put("key0", "value0");
        mValidParameters.put("key1", "value1");
        mValidState = new DeferredDeeplinkState(mValidDeeplink, mValidParameters, mUnparsedReferrer);
        mStateWithoutParameters = new DeferredDeeplinkState(mValidDeeplink, null, mUnparsedReferrer);
        mStateWithoutDeeplink = new DeferredDeeplinkState(null, null, mUnparsedReferrer);
        mStateWithoutAnything = new DeferredDeeplinkState(null, null, null);
        mDeferredDeeplinkStateManager = new DeferredDeeplinkStateManager(false);
    }

    @Test
    public void requestDeferredDeeplinkWasCheckedNullReferrer() {
        mDeferredDeeplinkStateManager = new DeferredDeeplinkStateManager(true);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.NOT_A_FIRST_LAUNCH, "");
    }

    @Test
    public void requestDeferredDeeplinkParametersWasCheckedNullReferrer() {
        mDeferredDeeplinkStateManager = new DeferredDeeplinkStateManager(true);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.NOT_A_FIRST_LAUNCH, "");
    }

    @Test
    public void requestDeferredDeeplinkWasCheckedHasReferrer() {
        mDeferredDeeplinkStateManager = new DeferredDeeplinkStateManager(true);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.NOT_A_FIRST_LAUNCH, mUnparsedReferrer);
    }

    @Test
    public void requestDeferredDeeplinkParametersWasCheckedHasReferrer() {
        mDeferredDeeplinkStateManager = new DeferredDeeplinkStateManager(true);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.NOT_A_FIRST_LAUNCH, mUnparsedReferrer);
    }

    @Test
    public void requestDeferredDeeplinkNoSavedState() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void requestDeferredDeeplinkParametersNoSavedState() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void requestDeferredDeeplinkHasEverything() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onDeeplinkLoaded(mValidDeeplink);
    }

    @Test
    public void requestDeferredDeeplinkParametersHasEverything() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onParametersLoaded(mValidParameters);
    }

    @Test
    public void requestDeferredDeeplinkNoParameters() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutParameters);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onDeeplinkLoaded(mValidDeeplink);
    }

    @Test
    public void requestDeferredDeeplinkParametersNoParameters() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutParameters);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.PARSE_ERROR, mUnparsedReferrer);
    }

    @Test
    public void requestDeferredDeeplinkNoDeeplink() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.PARSE_ERROR, mUnparsedReferrer);
    }

    @Test
    public void requestDeferredDeeplinkParametersNoDeeplink() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.PARSE_ERROR, mUnparsedReferrer);
    }

    @Test
    public void requestDeferredDeeplinkNoReferrer() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutAnything);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.NO_REFERRER, "");
    }

    @Test
    public void requestDeferredDeeplinkParametersNoReferrer() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutAnything);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.NO_REFERRER, "");
    }

    @Test
    public void onValidDeeplinkLoadedBothListeners() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verify(mDeeplinkListener).onDeeplinkLoaded(mValidDeeplink);
        verify(mDeeplinkParametersListener).onParametersLoaded(mValidParameters);
    }

    @Test
    public void onValidDeeplinkLoadedOnlyDeeplinkListener() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verify(mDeeplinkListener).onDeeplinkLoaded(mValidDeeplink);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void onValidDeeplinkLoadedOnlyParametersListener() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verify(mDeeplinkParametersListener).onParametersLoaded(mValidParameters);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void onValidDeeplinkLoadedNoListeners() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verifyNoMoreInteractions(mDeeplinkListener);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void onErrorBothListeners() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.PARSE_ERROR, mUnparsedReferrer);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.PARSE_ERROR, mUnparsedReferrer);
    }

    @Test
    public void onErrorOnlyDeeplinkListener() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.PARSE_ERROR, mUnparsedReferrer);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void onErrordOnlyParametersListener() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.PARSE_ERROR, mUnparsedReferrer);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void onErrorNoListeners() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void onErrorBothListenersNullReferrer() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutAnything);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.NO_REFERRER, "");
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.NO_REFERRER, "");
    }

    @Test
    public void firstOnErrorThenRequestDeeplink() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onError(DeferredDeeplinkListener.Error.PARSE_ERROR, mUnparsedReferrer);
    }

    @Test
    public void firstOnErrorThenRequestParameters() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onError(DeferredDeeplinkParametersListener.Error.PARSE_ERROR, mUnparsedReferrer);
    }

    @Test
    public void requestDeeplinkHasStateThenOnLoaded() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onDeeplinkLoaded(any(String.class));
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void requestDeeplinkHasStateThenOnError() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        verify(mDeeplinkListener).onDeeplinkLoaded(any(String.class));
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void requestParametersHasStateThenOnLoaded() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onParametersLoaded(any(Map.class));
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void requestParametersHasStateThenOnError() {
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        verify(mDeeplinkParametersListener).onParametersLoaded(any(Map.class));
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void requestDeeplinkOnLoadedTwice() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verify(mDeeplinkListener).onDeeplinkLoaded(any(String.class));
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void requestDeeplinkOnErrorTwice() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplink(mDeeplinkListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verify(mDeeplinkListener).onError(any(DeferredDeeplinkListener.Error.class), anyString());
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verifyNoMoreInteractions(mDeeplinkListener);
    }

    @Test
    public void requestParametersOnLoadedTwice() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verify(mDeeplinkParametersListener).onParametersLoaded(any(Map.class));
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mValidState);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }

    @Test
    public void requestParametersOnErrorTwice() {
        mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(mDeeplinkParametersListener);
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verify(mDeeplinkParametersListener).onError(any(DeferredDeeplinkParametersListener.Error.class), anyString());
        mDeferredDeeplinkStateManager.onDeeplinkLoaded(mStateWithoutDeeplink);
        verifyNoMoreInteractions(mDeeplinkParametersListener);
    }
}
