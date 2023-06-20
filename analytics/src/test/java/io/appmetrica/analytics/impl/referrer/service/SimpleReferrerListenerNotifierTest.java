package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleReferrerListenerNotifierTest extends CommonTest {

    @Mock
    private ReferrerHolder.Listener listener;
    private SimpleReferrerListenerNotifier listenerNotifier;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        listenerNotifier = new SimpleReferrerListenerNotifier(listener);
    }

    @Test
    public void constructor() {
        IReferrerNotificationFilter filter = listenerNotifier.getFilter();
        assertThat(filter.shouldNotify(null)).isTrue();
        assertThat(listenerNotifier.getListener()).isSameAs(listener);
    }
}
