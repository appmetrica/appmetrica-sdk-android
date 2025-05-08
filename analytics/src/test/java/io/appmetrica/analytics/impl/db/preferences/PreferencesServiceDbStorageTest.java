package io.appmetrica.analytics.impl.db.preferences;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.network.NetworkHost;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PreferencesServiceDbStorageTest extends CommonTest {

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class RetryPolicyBadHostsTest extends CommonTest {

        @Mock
        private IKeyValueTableDbHelper mDbStorage;
        @NonNull
        private PreferencesServiceDbStorage mServicePreferences;
        @NonNull
        private final NetworkHost networkHost;

        @ParameterizedRobolectricTestRunner.Parameters(name = "host {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {NetworkHost.DIAGNOSTIC}
            });
        }

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.getArgument(1);
                }
            }).when(mDbStorage).getInt(anyString(), anyInt());
            mServicePreferences = new PreferencesServiceDbStorage(mDbStorage);
        }

        public RetryPolicyBadHostsTest(@NonNull NetworkHost host) {
            networkHost = host;
        }

        @Test
        public void testGetNextSendAttemptNumberDefault() {
            assertThat(mServicePreferences.getNextSendAttemptNumber(networkHost, -1)).isEqualTo(-1);
        }

        @Test
        public void testGetNextSendAttemptNumber() {
            when(mDbStorage.getInt(nullable(String.class), anyInt())).thenReturn(2);
            assertThat(mServicePreferences.getNextSendAttemptNumber(networkHost, -1)).isEqualTo(-1);
        }

        @Test
        public void testPutNextSendAttemptNumber() {
            mServicePreferences.putNextSendAttemptNumber(networkHost, 5);
            verifyNoMoreInteractions(mDbStorage);
        }

        @Test
        public void testGetLastSendAttemptTimeDefault() {
            assertThat(mServicePreferences.getLastSendAttemptTimeSeconds(networkHost, -1)).isEqualTo(-1);
        }

        @Test
        public void testGetLastSendAttemptTime() {
            when(mDbStorage.getInt(nullable(String.class), anyInt())).thenReturn(2);
            assertThat(mServicePreferences.getLastSendAttemptTimeSeconds(networkHost, -1)).isEqualTo(-1);
        }

        @Test
        public void testPutLastSendAttemptTime() {
            mServicePreferences.putLastSendAttemptTimeSeconds(networkHost, 5);
            verifyNoMoreInteractions(mDbStorage);
        }

    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class RetryPolicyTest extends CommonTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "host {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {NetworkHost.DIAGNOSTIC, null, null},
                {NetworkHost.REPORT, PreferencesServiceDbStorage.NEXT_REPORT_SEND_ATTEMPT_NUMBER, PreferencesServiceDbStorage.LAST_REPORT_SEND_ATTEMPT_TIME},
                {NetworkHost.STARTUP, PreferencesServiceDbStorage.NEXT_STARTUP_SEND_ATTEMPT_NUMBER, PreferencesServiceDbStorage.LAST_STARTUP_SEND_ATTEMPT_TIME},
                {NetworkHost.LOCATION, PreferencesServiceDbStorage.NEXT_LOCATION_SEND_ATTEMPT_NUMBER, PreferencesServiceDbStorage.LAST_LOCATION_SEND_ATTEMPT_TIME}
            });
        }

        @NonNull
        private final NetworkHost networkHost;
        @NonNull
        private final PreferencesItem mNextSendAttemptNumber;
        @NonNull
        private final PreferencesItem mLastSendAttemptTime;

        @Mock
        private IKeyValueTableDbHelper mDbStorage;
        @NonNull
        private PreferencesServiceDbStorage mServicePreferences;

        public RetryPolicyTest(@NonNull NetworkHost networkHost,
                               @NonNull PreferencesItem nextSendAttemptNumber,
                               @NonNull PreferencesItem lastSendAttemptTime) {
            this.networkHost = networkHost;
            mNextSendAttemptNumber = nextSendAttemptNumber;
            mLastSendAttemptTime = lastSendAttemptTime;
        }

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.getArgument(1);
                }
            }).when(mDbStorage).getInt(anyString(), anyInt());
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.getArgument(1);
                }
            }).when(mDbStorage).getLong(anyString(), anyLong());
            mServicePreferences = new PreferencesServiceDbStorage(mDbStorage);
        }

        @Test
        public void testGetNextSendAttemptNumberDefault() {
            assertThat(mServicePreferences.getNextSendAttemptNumber(networkHost, -1)).isEqualTo(-1);
        }

        @Test
        public void testGetNextSendAttemptNumber() {
            if (mNextSendAttemptNumber != null) {
                when(mDbStorage.getInt(eq(mNextSendAttemptNumber.fullKey()), anyInt())).thenReturn(2);
                assertThat(mServicePreferences.getNextSendAttemptNumber(networkHost, 0)).isEqualTo(2);
            } else {
                assertThat(mServicePreferences.getNextSendAttemptNumber(networkHost, -1)).isEqualTo(-1);
            }
        }

        @Test
        public void testPutNextSendAttemptNumber() {
            mServicePreferences.putNextSendAttemptNumber(networkHost, 5);
            if (mNextSendAttemptNumber != null) {
                verify(mDbStorage).put(mNextSendAttemptNumber.fullKey(), 5);
            } else {
                verifyNoMoreInteractions(mDbStorage);
            }
        }

        @Test
        public void testGetLastSendAttemptTimeDefault() {
            assertThat(mServicePreferences.getLastSendAttemptTimeSeconds(networkHost, -1)).isEqualTo(-1);
        }

        @Test
        public void testGetLastSendAttemptTime() {
            if (mLastSendAttemptTime != null) {
                when(mDbStorage.getLong(eq(mLastSendAttemptTime.fullKey()), anyLong())).thenReturn(2L);
                assertThat(mServicePreferences.getLastSendAttemptTimeSeconds(networkHost, 0)).isEqualTo(2);
            } else {
                assertThat(mServicePreferences.getLastSendAttemptTimeSeconds(networkHost, -1)).isEqualTo(-1);
            }
        }

        @Test
        public void testPutLastSendAttemptTime() {
            mServicePreferences.putLastSendAttemptTimeSeconds(networkHost, 5);
            if (mLastSendAttemptTime != null) {
                verify(mDbStorage).put(mLastSendAttemptTime.fullKey(), 5L);
            } else {
                verifyNoMoreInteractions(mDbStorage);
            }
        }
    }

    private PreferencesServiceDbStorage mServiceDbStorage;
    @Mock
    private IKeyValueTableDbHelper mDbStorage;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        }).when(mDbStorage).getLong(anyString(), anyLong());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        }).when(mDbStorage).getBoolean(anyString(), anyBoolean());
        mServiceDbStorage = new PreferencesServiceDbStorage(mDbStorage);
    }

    @Test
    public void testIsProbablyTimeFromPastShouldReturnDefValueIfUndefinedAndDefValueIsTrue() {
        assertThat(mServiceDbStorage.isUncheckedTime(true)).isTrue();
    }

    @Test
    public void testIsProbablyTimeFromPastShouldReturnDefValueIfUndefinedAndDefValueIsFalse() {
        assertThat(mServiceDbStorage.isUncheckedTime(false)).isFalse();
    }

    @Test
    public void testIsProbablyTimeFromPastShouldReturnSavedValueIfTrue() {
        when(mDbStorage.getBoolean(eq(PreferencesServiceDbStorage.UNCHECKED_TIME.fullKey()), anyBoolean())).thenReturn(true);
        assertThat(mServiceDbStorage.isUncheckedTime(false)).isTrue();
    }

    @Test
    public void testIsProbablyTimeFromPastShouldReturnSavedValueIfFalse() {
        when(mDbStorage.getBoolean(eq(PreferencesServiceDbStorage.UNCHECKED_TIME.fullKey()), anyBoolean())).thenReturn(false);
        assertThat(mServiceDbStorage.isUncheckedTime(true)).isFalse();
    }

    @Test
    public void testPutTimeFromPastTrue() {
        mServiceDbStorage.putUncheckedTime(true);
        verify(mDbStorage).put(PreferencesServiceDbStorage.UNCHECKED_TIME.fullKey(), true);
    }

    @Test
    public void testPutTimeFromPastFalse() {
        mServiceDbStorage.putUncheckedTime(false);
        verify(mDbStorage).put(PreferencesServiceDbStorage.UNCHECKED_TIME.fullKey(), false);
    }

    @Test
    public void getDataSendingRestricted() {
        assertThat(mServiceDbStorage.getDataSendingRestrictedFromMainReporter()).isNull();

        when(mDbStorage.containsKey(PreferencesServiceDbStorage.DATA_SENDING_RESTRICTED_IN_MAIN.fullKey())).thenReturn(true);
        when(mDbStorage.getBoolean(eq(PreferencesServiceDbStorage.DATA_SENDING_RESTRICTED_IN_MAIN.fullKey()), anyBoolean()))
            .thenReturn(false);
        assertThat(mServiceDbStorage.getDataSendingRestrictedFromMainReporter()).isFalse();
    }

    @Test
    public void saveDataSendingRestricted() {
        mServiceDbStorage.putDataSendingRestrictedFromMainReporter(false);
        verify(mDbStorage).put(PreferencesServiceDbStorage.DATA_SENDING_RESTRICTED_IN_MAIN.fullKey(), false);
    }

    @Test
    public void testLastIdentityLightSendTimeSecondsMissing() {
        final long defaultValue = 10;
        assertThat(mServiceDbStorage.getLastIdentityLightSendTimeSeconds(defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testGetIdentityLightSendTimeSeconds() {
        final long lastSendTime = 12345678;
        when(mDbStorage.getLong(eq(PreferencesServiceDbStorage.LAST_IDENTITY_LIGHT_SEND_TIME.fullKey()), anyLong()))
            .thenReturn(lastSendTime);
        assertThat(mServiceDbStorage.getLastIdentityLightSendTimeSeconds(0L)).isEqualTo(lastSendTime);
    }

    @Test
    public void testPutIdentityLightSendTimeSeconds() {
        final long lastSendTime = 12345678;
        mServiceDbStorage.putLastIdentityLightSendTimeSeconds(lastSendTime);
        verify(mDbStorage).put(PreferencesServiceDbStorage.LAST_IDENTITY_LIGHT_SEND_TIME.fullKey(), lastSendTime);
    }

    @Test
    public void wasSatellitePreloadInfoCheckedMissing() {
        assertThat(mServiceDbStorage.wasSatellitePreloadInfoChecked()).isFalse();
    }

    @Test
    public void wasSatellitePreloadInfoChecked() {
        when(mDbStorage.getBoolean(eq(PreferencesServiceDbStorage.SATELLITE_PRELOAD_INFO_CHECKED.fullKey()), anyBoolean()))
            .thenReturn(true);
        assertThat(mServiceDbStorage.wasSatellitePreloadInfoChecked()).isTrue();
    }

    @Test
    public void markSatellitePreloadInfoChecked() {
        mServiceDbStorage.markSatellitePreloadInfoChecked();
        verify(mDbStorage).put(PreferencesServiceDbStorage.SATELLITE_PRELOAD_INFO_CHECKED.fullKey(), true);
    }

    @Test
    public void wereSatelliteClidsChecked() {
        assertThat(mServiceDbStorage.wereSatelliteClidsChecked()).isFalse();

        when(mDbStorage.containsKey(PreferencesServiceDbStorage.SATELLITE_CLIDS_CHECKED.fullKey())).thenReturn(true);
        when(mDbStorage.getBoolean(eq(PreferencesServiceDbStorage.SATELLITE_CLIDS_CHECKED.fullKey()), anyBoolean()))
            .thenReturn(true);
        assertThat(mServiceDbStorage.wereSatelliteClidsChecked()).isTrue();
    }

    @Test
    public void markSatelliteClidsChecked() {
        mServiceDbStorage.markSatelliteClidsChecked();
        verify(mDbStorage).put(PreferencesServiceDbStorage.SATELLITE_CLIDS_CHECKED.fullKey(), true);
    }

    @Test
    public void getVitalData() {
        String data = "afdasfadfasdas";
        when(mDbStorage.getString(PreferencesServiceDbStorage.VITAL_DATA.fullKey(), null)).thenReturn(data);
        assertThat(mServiceDbStorage.getVitalData()).isEqualTo(data);
    }

    @Test
    public void getVitalNullData() {
        when(mDbStorage.getString(PreferencesServiceDbStorage.VITAL_DATA.fullKey(), null)).thenReturn(null);
        assertThat(mServiceDbStorage.getVitalData()).isNull();
    }

    @Test
    public void putVitalData() {
        String data = "sfdasfasdasdas";
        mServiceDbStorage.putVitalData(data);
        InOrder inOrder = inOrder(mDbStorage);
        inOrder.verify(mDbStorage).put(PreferencesServiceDbStorage.VITAL_DATA.fullKey(), data);
        inOrder.verify(mDbStorage).commit();
    }

    @Test
    public void putLastVersionKotlinSendTime() {
        long value = 24323L;
        mServiceDbStorage.putLastKotlinVersionSendTime(value);
        verify(mDbStorage).put(PreferencesServiceDbStorage.LAST_KOTLIN_VERSION_SEND_TIME.fullKey(), value);
    }

    @Test
    public void getLastVersionKotlinSendTime() {
        long value = 2312321L;
        when(mDbStorage.getLong(PreferencesServiceDbStorage.LAST_KOTLIN_VERSION_SEND_TIME.fullKey(), 0))
            .thenReturn(value);
        assertThat(mServiceDbStorage.lastKotlinVersionSendTime()).isEqualTo(value);
    }

    @Test
    public void getLastVersionKotlinSendTimeDefaultValue() {
        when(mDbStorage.getLong(PreferencesServiceDbStorage.LAST_KOTLIN_VERSION_SEND_TIME.fullKey(), 0))
            .thenReturn(0L);
        assertThat(mServiceDbStorage.lastKotlinVersionSendTime()).isZero();
    }

    @Test
    public void saveAdvIdentifiersTrackingEnabledForTrue() {
        mServiceDbStorage.saveAdvIdentifiersTrackingEnabled(true);
        verify(mDbStorage).put(PreferencesServiceDbStorage.ADV_IDENTIFIERS_TRACKING_ENABLED.fullKey(), true);
    }

    @Test
    public void saveAdvIdentifiersTrackingEnabledForFalse() {
        mServiceDbStorage.saveAdvIdentifiersTrackingEnabled(false);
        verify(mDbStorage).put(PreferencesServiceDbStorage.ADV_IDENTIFIERS_TRACKING_ENABLED.fullKey(), false);
    }

    @Test
    public void isAdvIdentifiersTrackingStatusEnabled() {
        when(mDbStorage.getBoolean(PreferencesServiceDbStorage.ADV_IDENTIFIERS_TRACKING_ENABLED.fullKey(), false))
            .thenReturn(true);
        assertThat(mServiceDbStorage.isAdvIdentifiersTrackingStatusEnabled(false)).isTrue();
    }

    //just add new put methods when needed
    public static PreferencesServiceDbStorage createMock() {
        PreferencesServiceDbStorage mock = mock(PreferencesServiceDbStorage.class);
        doReturn(mock).when(mock).putDataSendingRestrictedFromMainReporter(anyBoolean());
        doReturn(null).when(mock).getDataSendingRestrictedFromMainReporter();
        return mock;
    }
}
