package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReferrerListenerNotifierTest extends CommonTest {

    @Mock
    private IReferrerNotificationFilter mFilter;
    @Mock
    private ReferrerHolder.Listener mListener;
    @Mock
    private IReferrerHandledNotifier mReferrerHandledNotifier;
    @Mock
    private ReferrerInfo mReferrerInfo;
    private ReferrerListenerNotifier mReferrerListenerNotifier;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mReferrerListenerNotifier = new ReferrerListenerNotifier(mFilter, mListener, mReferrerHandledNotifier);
    }

    @Test
    public void shouldNotNotify() {
        when(mFilter.shouldNotify(mReferrerInfo)).thenReturn(false);
        mReferrerListenerNotifier.notifyIfNeeded(mReferrerInfo);
        verifyNoMoreInteractions(mListener);
    }

    @Test
    public void shouldNotify() {
        when(mFilter.shouldNotify(mReferrerInfo)).thenReturn(true);
        mReferrerListenerNotifier.notifyIfNeeded(mReferrerInfo);
        verify(mListener).handleReferrer(mReferrerInfo);
        verify(mReferrerHandledNotifier).onReferrerHandled();
    }
}
