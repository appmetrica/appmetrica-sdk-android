package io.appmetrica.analytics.impl;

import android.content.Intent;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DeeplinkConsumerTest extends CommonTest {

    @Mock
    private IMainReporter mainReporter;
    private DeeplinkConsumer deeplinkConsumer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        deeplinkConsumer = new DeeplinkConsumer(mainReporter);
    }

    @Test
    public void nullAndEmptyDeeplinks() {
        deeplinkConsumer.reportAppOpen((Intent) null);
        deeplinkConsumer.reportAppOpen((String) null);
        deeplinkConsumer.reportAutoAppOpen(null);
        deeplinkConsumer.reportAppOpen((Intent) null);
        deeplinkConsumer.reportAppOpen((String) null);
        deeplinkConsumer.reportAutoAppOpen(null);
        deeplinkConsumer.reportAppOpen("");
        deeplinkConsumer.reportAutoAppOpen("");
        deeplinkConsumer.reportAppOpen("");
        deeplinkConsumer.reportAutoAppOpen("");

        deeplinkConsumer.reportAutoAppOpen("link1");
        deeplinkConsumer.reportAppOpen("link2");
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAppOpen((Intent) null);
        deeplinkConsumer.reportAppOpen((String) null);
        deeplinkConsumer.reportAutoAppOpen(null);
        deeplinkConsumer.reportAppOpen((Intent) null);
        deeplinkConsumer.reportAppOpen((String) null);
        deeplinkConsumer.reportAutoAppOpen(null);
        deeplinkConsumer.reportAppOpen("");
        deeplinkConsumer.reportAutoAppOpen("");
        deeplinkConsumer.reportAppOpen("");
        deeplinkConsumer.reportAutoAppOpen("");
        verifyZeroInteractions(mainReporter);
    }

    @Test
    public void filledLinks() {
        deeplinkConsumer.reportAppOpen("link1");
        verify(mainReporter).reportAppOpen("link1", false);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAppOpen("link1");
        verify(mainReporter).reportAppOpen("link1", false);
        clearInvocations(mainReporter);

        Intent intent = mock(Intent.class);
        when(intent.getDataString()).thenReturn("link1");
        deeplinkConsumer.reportAppOpen(intent);
        verify(mainReporter).reportAppOpen("link1", false);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAutoAppOpen("link1");
        verifyZeroInteractions(mainReporter);

        deeplinkConsumer.reportAutoAppOpen("link2");
        verify(mainReporter).reportAppOpen("link2", true);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAppOpen("link2");
        verifyZeroInteractions(mainReporter);

        deeplinkConsumer.reportAppOpen("link2");
        verifyZeroInteractions(mainReporter);

        when(intent.getDataString()).thenReturn("link2");
        deeplinkConsumer.reportAppOpen(intent);
        verifyZeroInteractions(mainReporter);

        deeplinkConsumer.reportAutoAppOpen("link1");
        verify(mainReporter).reportAppOpen("link1", true);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAppOpen("link3");
        verify(mainReporter).reportAppOpen("link3", false);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAppOpen("link3");
        verify(mainReporter).reportAppOpen("link3", false);
        clearInvocations(mainReporter);

        when(intent.getDataString()).thenReturn("link3");
        deeplinkConsumer.reportAppOpen(intent);
        verify(mainReporter).reportAppOpen("link3", false);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAutoAppOpen("link3");
        verifyZeroInteractions(mainReporter);

        deeplinkConsumer.reportAppOpen("link2");
        verify(mainReporter).reportAppOpen("link2", false);
        clearInvocations(mainReporter);

        deeplinkConsumer.reportAppOpen("link1");
        verify(mainReporter).reportAppOpen("link1", false);
    }
}
