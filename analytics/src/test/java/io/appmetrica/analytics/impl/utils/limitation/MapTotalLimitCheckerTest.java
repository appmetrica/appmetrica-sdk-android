package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MapTotalLimitCheckerTest extends CommonTest {

    @Mock
    private MeasuredJsonMap mJson;
    @Mock
    private PublicLogger mPublicLogger;
    private final String mKey = "key";
    private final String mValue = "value";
    private final int mTotalMaxSize = 15;
    private final String mTag = "TAG";
    private MapTotalLimitChecker mChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mChecker = new MapTotalLimitChecker(mTotalMaxSize, mTag, mPublicLogger);
    }

    @Test
    public void testLimitNotReached() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(1);
        when(mJson.containsKey(mKey)).thenReturn(false);
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isFalse();
    }

    @Test
    public void testLimitReached() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(10);
        when(mJson.containsKey(mKey)).thenReturn(false);
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isTrue();
    }

    @Test
    public void testLimitReachedExactly() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(7);
        when(mJson.containsKey(mKey)).thenReturn(false);
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isFalse();
    }

    @Test
    public void testHadKeyLimitNotReached() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(13);
        when(mJson.containsKey(mKey)).thenReturn(true);
        when(mJson.get(mKey)).thenReturn("abcd");
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isFalse();
    }

    @Test
    public void testHadKeyLimitReached() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(13);
        when(mJson.containsKey(mKey)).thenReturn(true);
        when(mJson.get(mKey)).thenReturn("a");
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isTrue();
    }

    @Test
    public void testHadKeyNullValueLimitNotReached() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(9);
        when(mJson.containsKey(mKey)).thenReturn(true);
        when(mJson.get(mKey)).thenReturn(null);
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isFalse();
    }

    @Test
    public void testHadKeyNullValueLimitReached() {
        when(mJson.getKeysAndValuesSymbolsCount()).thenReturn(11);
        when(mJson.containsKey(mKey)).thenReturn(true);
        when(mJson.get(mKey)).thenReturn(null);
        assertThat(mChecker.willLimitBeReached(mJson, mKey, mValue)).isTrue();
    }

    @Test
    public void testLogTotalLimitReachedLoggerEnabled() {
        mChecker.logTotalLimitReached(mKey);
        verify(mPublicLogger).warning(anyString(), eq(mTag), eq(mTotalMaxSize), eq(mKey));
    }
}
