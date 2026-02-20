package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class ReferrerRetrieverWrapperTest extends CommonTest {

    @Mock
    private IReferrerRetriever mReferrerRetriever;
    @Mock
    private ReferrerReceivedListener mListener;
    private ReferrerRetrieverWrapper mReferrerRetrieverWrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mReferrerRetrieverWrapper = new ReferrerRetrieverWrapper(mReferrerRetriever);
    }

    @Test
    public void testDelegated() throws Throwable {
        mReferrerRetrieverWrapper.retrieveReferrer(mListener);
        verify(mReferrerRetriever).retrieveReferrer(mListener);
    }

    @Test
    public void testThrows() throws Throwable {
        RuntimeException ex = new RuntimeException();
        doThrow(ex).when(mReferrerRetriever).retrieveReferrer(mListener);
        mReferrerRetrieverWrapper.retrieveReferrer(mListener);
        verify(mListener).onReferrerRetrieveError(ex);
    }
}
