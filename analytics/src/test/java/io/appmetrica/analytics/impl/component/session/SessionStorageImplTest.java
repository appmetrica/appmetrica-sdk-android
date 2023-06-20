package io.appmetrica.analytics.impl.component.session;

import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class SessionStorageImplTest extends CommonTest {

    private static final String TAG = "thissessiontag";

    private PreferencesComponentDbStorage mStorage;

    @Before
    public void setUp() {
        mStorage = mock(PreferencesComponentDbStorage.class);
    }

    @Test
    public void testWithNullSession() {
        doReturn(null).when(mStorage).getSessionParameters(TAG);
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        assertThat(sessionStorage.hasValues()).isFalse();
    }

    @Test
    public void testWithBlankLineSession() {
        doReturn("").when(mStorage).getSessionParameters(TAG);
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        assertThat(sessionStorage.hasValues()).isFalse();
    }

    @Test
    public void testWithEmptyJson() {
        doReturn("{}").when(mStorage).getSessionParameters(TAG);
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        assertThat(sessionStorage.hasValues()).isFalse();
        assertThat(sessionStorage.getLastEventOffset()).isNull();
        assertThat(sessionStorage.getCreationTime()).isNull();
        assertThat(sessionStorage.getReportId()).isNull();
        assertThat(sessionStorage.getSessionId()).isNull();
        assertThat(sessionStorage.getSleepStart()).isNull();
        assertThat(sessionStorage.isAliveReportNeeded()).isNull();
    }

    @Test
    public void testWithStrangeJson() {
        doReturn("!2Q;L43Q;L 4K23P").when(mStorage).getSessionParameters(TAG);
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        assertThat(sessionStorage.hasValues()).isFalse();
    }

    @Test
    public void testWithSomeValues() {
        final int sleepStart = new Random().nextInt(100000);
        doReturn("{" +
                SessionStorageImpl.SLEEP_START + ":" + String.valueOf(sleepStart) +
                "}").when(mStorage).getSessionParameters(TAG);
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        assertThat(sessionStorage.hasValues()).isTrue();
        assertThat(sessionStorage.getSleepStart()).isEqualTo(sleepStart);
    }

    @Test
    public void testClear() {
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        sessionStorage.putAliveReportNeeded(true)
                .putSleepStart(1)
                .putCreationTime(1)
                .putReportId(1)
                .putSessionId(1)
                .putLastEventOffset(1)
                .commit();
        assertThat(sessionStorage.hasValues()).isTrue();
        sessionStorage.clear();
        assertThat(sessionStorage.hasValues()).isFalse();
    }

    @Test
    public void testProperties() {
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mStorage, TAG);
        Random random = new Random();
        boolean aliveNeeded = true;
        int sleepStart = random.nextInt(1000);
        int creationTime = random.nextInt(1000) + 1000;
        int reportId = random.nextInt(1000) + 2000;
        int sessionId = random.nextInt(1000) + 3000;
        int lastEventOffset = random.nextInt(1000) + 4000;
        sessionStorage.putAliveReportNeeded(aliveNeeded)
                .putSleepStart(sleepStart)
                .putCreationTime(creationTime)
                .putReportId(reportId)
                .putSessionId(sessionId)
                .putLastEventOffset(lastEventOffset)
                .commit();
        assertThat(sessionStorage.isAliveReportNeeded()).isTrue();
        assertThat(sessionStorage.getSleepStart()).isEqualTo(sleepStart);
        assertThat(sessionStorage.getCreationTime()).isEqualTo(creationTime);
        assertThat(sessionStorage.getReportId()).isEqualTo(reportId);
        assertThat(sessionStorage.getSessionId()).isEqualTo(sessionId);
        assertThat(sessionStorage.getLastEventOffset()).isEqualTo(lastEventOffset);
    }

}
