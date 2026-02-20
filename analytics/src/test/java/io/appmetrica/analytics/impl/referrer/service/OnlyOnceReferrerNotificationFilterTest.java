package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class OnlyOnceReferrerNotificationFilterTest extends CommonTest {

    @Mock
    private IReferrerHandledProvider mReferrerHandledProvider;
    @Mock
    private ReferrerInfo mReferrerInfo;
    private OnlyOnceReferrerNotificationFilter mFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mFilter = new OnlyOnceReferrerNotificationFilter(mReferrerHandledProvider);
    }

    @Test
    public void wasHandledNonNull() {
        when(mReferrerHandledProvider.wasReferrerHandled()).thenReturn(true);
        assertThat(mFilter.shouldNotify(mReferrerInfo)).isFalse();
    }

    @Test
    public void wasNotHandledNonNull() {
        when(mReferrerHandledProvider.wasReferrerHandled()).thenReturn(false);
        assertThat(mFilter.shouldNotify(mReferrerInfo)).isTrue();
    }

    @Test
    public void wasHandledNull() {
        when(mReferrerHandledProvider.wasReferrerHandled()).thenReturn(true);
        assertThat(mFilter.shouldNotify(null)).isFalse();
    }

    @Test
    public void wasNotHandledNull() {
        when(mReferrerHandledProvider.wasReferrerHandled()).thenReturn(false);
        assertThat(mFilter.shouldNotify(null)).isFalse();
    }
}
