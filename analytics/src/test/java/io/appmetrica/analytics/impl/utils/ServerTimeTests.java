package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ServerTimeTests extends CommonTest {

    private PreferencesServiceDbStorage mServiceDbStorage;
    private TimeProvider mTimeProvider;
    private ServerTime mServerTime;

    private static final Long MAX_VALID_SERVER_TIME_DIFFERENCE = TimeUnit.DAYS.toSeconds(3);

    @Before
    public void setUp() {
        mServiceDbStorage = mock(PreferencesServiceDbStorage.class);
        mTimeProvider = mock(TimeProvider.class);
        mServerTime = ServerTime.getInstance();
        mServerTime.init(mServiceDbStorage, mTimeProvider);
    }

    @Test
    public void testInitShouldReadServerTimeOffsetFromPrefs() {
        long offset = 3243434L;
        when(mServiceDbStorage.getServerTimeOffset(anyInt())).thenReturn(offset);
        mServerTime.init(mServiceDbStorage, mTimeProvider);
        assertThat(mServerTime.getServerTimeOffsetSeconds()).isEqualTo(offset);
    }

    @Test
    public void testUpdateServerTimeShouldSaveOffset() {
        long currentTime = 232343000L;
        long timeFromStartup = 34343423000L;
        when(mTimeProvider.currentTimeMillis()).thenReturn(currentTime);
        long offset = (timeFromStartup - currentTime) / 1000;
        mServerTime.updateServerTime(timeFromStartup, MAX_VALID_SERVER_TIME_DIFFERENCE);
        verify(mServiceDbStorage, times(1)).putServerTimeOffset(offset);
        verify(mServiceDbStorage, times(1)).commit();
    }

    @Test
    public void testUpdateServerTimeShouldSaveFalseToUncheckedTimeDifferenceIfPositiveTimeDifferentIsLow() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(true);
        testUpdateServerTime(TimeUnit.HOURS.toMillis(12));
        verifyShouldSaveFalseToUncheckedTimeDifference();
    }

    @Test
    public void testUpdateServerTimeShouldSaveFalseToUncheckedTimeDifferenceIfNegativeTimeDifferenceIsLow() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(true);
        testUpdateServerTime(-TimeUnit.HOURS.toMillis(12));
        verifyShouldSaveFalseToUncheckedTimeDifference();
    }

    private void testUpdateServerTime(long timeDifference) {
        long currentTime = 323453454L;
        long timeFromStartup = currentTime + timeDifference;
        when(mTimeProvider.currentTimeMillis()).thenReturn(currentTime);
        mServerTime.updateServerTime(timeFromStartup, MAX_VALID_SERVER_TIME_DIFFERENCE);
    }

    private void verifyShouldSaveFalseToUncheckedTimeDifference() {
        verifyShouldSaveExpectedValueToUncheckedTimeDifference(false);
    }

    private void verifyShouldSaveTrueToUncheckedTimeDifference() {
        verifyShouldSaveExpectedValueToUncheckedTimeDifference(true);
    }

    private void verifyShouldSaveExpectedValueToUncheckedTimeDifference(boolean value) {
        verify(mServiceDbStorage, times(1)).putUncheckedTime(value);
        verify(mServiceDbStorage, times(1)).commit();
    }

    @Test
    public void testUpdateServerTimeShouldSaveTrueToUncheckedTimeDifferenceIfPositiveTimeDifferentIsGreat() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(true);
        testUpdateServerTime(TimeUnit.DAYS.toMillis(7));
        verifyShouldSaveTrueToUncheckedTimeDifference();
    }

    @Test
    public void testUpdateServerTimeShouldSaveTrueToUncheckedTimeDifferenceIfNegativeTimeDifferenceIsGreat() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(true);
        testUpdateServerTime(-TimeUnit.DAYS.toMillis(7));
        verifyShouldSaveTrueToUncheckedTimeDifference();
    }

    @Test
    public void testUpdateServerTimeShouldSaveFalseToUncheckedTimeDifferenceIfDeltaIsNull() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(true);
        long currentTime = 4345345435L;
        long timeFromStartup = currentTime + TimeUnit.DAYS.toMillis(10);
        when(mTimeProvider.currentTimeMillis()).thenReturn(currentTime);
        mServerTime.updateServerTime(timeFromStartup, null);
        verifyShouldSaveFalseToUncheckedTimeDifference();
    }

    @Test
    public void testUpdateServerTimeShouldNotUncheckedTimeDifferenceIfAlreadyIsFalse() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(false);
        long currentTime = 454565465L;
        long timeFromStartup = currentTime + TimeUnit.DAYS.toMillis(7);
        when(mTimeProvider.currentTimeMillis()).thenReturn(currentTime);
        mServerTime.updateServerTime(timeFromStartup, MAX_VALID_SERVER_TIME_DIFFERENCE);
        verify(mServiceDbStorage, never()).putUncheckedTime(anyBoolean());
    }

    @Test
    public void testDisableTimeDifferenceCheckingShouldSaveFalseToUncheckedTimeDifferencePast() {
        mServerTime.disableTimeDifferenceChecking();
        verifyShouldSaveFalseToUncheckedTimeDifference();
    }

    @Test
    public void testIsUncheckedTimeDifferenceShouldReturnFalseIfFalseInPreferences() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(false);
        assertThat(mServerTime.isUncheckedTime()).isFalse();
    }

    @Test
    public void testIsUncheckedTimeDifferenceShouldReturnTrueIfTrueInPreferences() {
        when(mServiceDbStorage.isUncheckedTime(anyBoolean())).thenReturn(true);
        assertThat(mServerTime.isUncheckedTime()).isTrue();
    }

    @Test
    public void testIsUncheckedTimeDifferenceShouldReturnTrueByDefault() {
        when(mServiceDbStorage.isUncheckedTime(true)).thenReturn(true);
        when(mServiceDbStorage.isUncheckedTime(false)).thenReturn(false);
        assertThat(mServerTime.isUncheckedTime()).isTrue();
    }
}
