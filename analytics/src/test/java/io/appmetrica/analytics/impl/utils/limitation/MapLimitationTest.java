package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapLimitationTest extends CommonTest {

    @Mock
    private MapTotalLimitChecker mTotalLimitChecker;
    @Mock
    private MapTrimmers mMapTrimmers;
    @Mock
    private CollectionLimitation mCollectionLimitation;
    @Mock
    private StringTrimmer mKeyTrimmer;
    @Mock
    private StringTrimmer mValueTrimmer;
    private SimpleMapLimitation mMapLimitation;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mMapTrimmers.getCollectionLimitation()).thenReturn(mCollectionLimitation);
        when(mMapTrimmers.getKeyTrimmer()).thenReturn(mKeyTrimmer);
        when(mMapTrimmers.getValueTrimmer()).thenReturn(mValueTrimmer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        }).when(mKeyTrimmer).trim(anyString());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        }).when(mValueTrimmer).trim(anyString());
        when(mCollectionLimitation.getMaxSize()).thenReturn(30);
        when(mTotalLimitChecker.willLimitBeReached(any(MeasuredJsonMap.class), anyString(), anyString())).thenReturn(false);
        mMapLimitation = new SimpleMapLimitation(mMapTrimmers, mTotalLimitChecker);
    }

    @Test
    public void testTryToAddValueShouldNotCrashIfSourceMapIsNull() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(30);
        mMapLimitation.tryToAddValue(null, randomStringGenerator.nextString(), randomStringGenerator.nextString());
    }

    @Test
    public void testTryToAddValueShouldTrimKeyAndValue() {
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        String testKey = "test_key";
        String testValue = "test_value";
        mMapLimitation.tryToAddValue(testMap, testKey, testValue);
        verify(mKeyTrimmer).trim(testKey);
        verify(mValueTrimmer).trim(testValue);
    }

    @Test
    public void testValuesNotInsertedIfTotalLimitReached() {
        when(mTotalLimitChecker.willLimitBeReached(any(MeasuredJsonMap.class), eq("c"), eq("3"))).thenReturn(true);
        assertThat(mMapLimitation.tryToAddValue(new MeasuredJsonMap(), "c", "3")).isFalse();
        verify(mTotalLimitChecker).logTotalLimitReached("c");
    }

    @Test
    public void testValuesInsertedIfTotalLimitNotReached() {
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        testMap.put("a", "1");
        when(mCollectionLimitation.getMaxSize()).thenReturn(2);
        assertThat(mMapLimitation.tryToAddValue(testMap, "b", "2")).isTrue();
        assertThat(testMap.get("b")).isEqualTo("2");
    }

    @Test
    public void testMapTrimmed() {
        when(mCollectionLimitation.getMaxSize()).thenReturn(3);
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        testMap.put("a", "1");
        testMap.put("b", "2");
        testMap.put("c", "3");
        assertThat(mMapLimitation.tryToAddValue(testMap, "d", "4")).isFalse();
        verify(mMapTrimmers).logContainerLimitReached("d");
    }

    @Test
    public void testValueReplacedAndLimitNotReached() {
        when(mCollectionLimitation.getMaxSize()).thenReturn(10);
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        for (int i = 0; i < 10; i++) {
            testMap.put(String.valueOf(i), String.valueOf(i));
        }
        assertThat(mMapLimitation.tryToAddValue(testMap, "9", "blabla")).isTrue();
        assertThat(testMap.get("9")).isEqualTo("blabla");
    }

    @Test
    public void testNullIsNotInserted() {
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        assertThat(mMapLimitation.tryToAddValue(testMap, "key", null)).isFalse();
    }

    @Test
    public void testEntryRemoved() {
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        testMap.put("key", "2");
        assertThat(mMapLimitation.tryToAddValue(testMap, "key", null)).isTrue();
        assertThat(testMap.size()).isZero();
    }

    @Test
    public void testAddSameItem() {
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        testMap.put("key", "2");
        assertThat(mMapLimitation.tryToAddValue(testMap, "key", "2")).isFalse();
    }

    @Test
    public void testAddExistingKey() {
        MeasuredJsonMap testMap = new MeasuredJsonMap();
        testMap.put("key", "2");
        assertThat(mMapLimitation.tryToAddValue(testMap, "key", "1")).isTrue();
    }
}
