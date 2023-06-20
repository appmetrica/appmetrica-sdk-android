package io.appmetrica.analytics.impl.component.session;

import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionIDProviderTest extends CommonTest {

    private static final long INITIAL_SESSION_ID = 10000000000L;

    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    private SessionIDProvider sessionIDProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionIDProvider = new SessionIDProvider(vitalComponentDataProvider);
    }

    @Test
    public void testCreateNewSessionCreateFirstSessionWithExpectedSessionId() {
        assertThat(sessionIDProvider.getNextSessionId()).isEqualTo(INITIAL_SESSION_ID);
        verify(vitalComponentDataProvider, times(1)).setSessionId(INITIAL_SESSION_ID);
    }

    @Test
    public void testCreateNewSessionCreateSessionWithExpectedSessionId() {
        long sessionId = 10000003400L;
        when(vitalComponentDataProvider.getSessionId()).thenReturn(sessionId);
        assertThat(sessionIDProvider.getNextSessionId()).isEqualTo(sessionId + 1);
        verify(vitalComponentDataProvider, times(1)).setSessionId(sessionId + 1);
    }

}
