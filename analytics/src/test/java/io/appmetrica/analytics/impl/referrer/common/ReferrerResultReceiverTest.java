package io.appmetrica.analytics.impl.referrer.common;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class ReferrerResultReceiverTest extends CommonTest {

    private static final int CODE_OK = 1;

    @Mock
    private Handler handler;
    @Mock
    private ReferrerChosenListener listener;
    private ReferrerResultReceiver referrerResultReceiver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        referrerResultReceiver = new ReferrerResultReceiver(handler, listener);
    }

    @Test
    public void onReceiveResultBadCode() {
        referrerResultReceiver.onReceiveResult(99, new Bundle());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void onReceiveResultNullReferrer() {
        referrerResultReceiver.onReceiveResult(CODE_OK, new Bundle());
        verify(listener).onReferrerChosen(null);
    }

    @Test
    public void onReceiveResultHasReferrer() {
        ReferrerInfo referrerInfo = new ReferrerInfo("referrer", 10, 20, ReferrerInfo.Source.GP);
        Bundle bundle = new Bundle();
        bundle.putByteArray("referrer", referrerInfo.toProto());
        referrerResultReceiver.onReceiveResult(CODE_OK, bundle);
        verify(listener).onReferrerChosen(referrerInfo);
    }

    @Test
    public void onReceiveResultBadBytes() {
        Bundle bundle = new Bundle();
        bundle.putByteArray("referrer", "bytes".getBytes());
        referrerResultReceiver.onReceiveResult(CODE_OK, bundle);
        verify(listener).onReferrerChosen(null);
    }

    @Test
    public void sendReferrerNullReceiver() {
        ReferrerResultReceiver.sendReferrer(null, null);
    }

    @Test
    public void sendReferrerNullReferrer() {
        ReferrerResultReceiver mockedReceiver = mock(ReferrerResultReceiver.class);
        ReferrerResultReceiver.sendReferrer(mockedReceiver, null);
        verify(mockedReceiver).send(eq(CODE_OK), argThat(bundleWithReferrer(null)));
    }

    @Test
    public void sendReferrerNotNullReferrer() {
        ReferrerResultReceiver mockedReceiver = mock(ReferrerResultReceiver.class);
        final ReferrerInfo referrerInfo = new ReferrerInfo("test referrer", 10, 20, ReferrerInfo.Source.HMS);
        ReferrerResultReceiver.sendReferrer(mockedReceiver, referrerInfo);
        verify(mockedReceiver).send(eq(CODE_OK), argThat(bundleWithReferrer(referrerInfo)));
    }

    private ArgumentMatcher<Bundle> bundleWithReferrer(@Nullable final ReferrerInfo expected) {
        return new ArgumentMatcher<Bundle>() {
            @Override
            public boolean matches(Bundle argument) {
                byte[] bytes = argument.getByteArray("referrer");
                try {
                    return argument.keySet().size() == 1 &&
                            (expected == null && bytes == null || expected.equals(ReferrerInfo.parseFrom(bytes)));
                } catch (InvalidProtocolBufferNanoException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
