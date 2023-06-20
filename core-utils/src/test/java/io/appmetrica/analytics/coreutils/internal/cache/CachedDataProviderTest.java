package io.appmetrica.analytics.coreutils.internal.cache;

import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CachedDataProviderTest extends CommonTest implements CachedDataProvider {

    private CachedData<Object> mCachedData;

    private final long refreshTime = 100L;
    private final long expiryTime = 200L;

    @Before
    public void setUp() throws Exception {
        mCachedData = new CachedData<Object>(refreshTime, expiryTime, "description");
    }

    @Test
    public void testConstructorWithExpireTime() {
        long refreshTime = 3243242L;
        long expiryTime = 324324244L;
        CachedData<Object> data = new CachedData<Object>(refreshTime, expiryTime, "descriptions");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(data.getRefreshTime()).isEqualTo(refreshTime);
        softly.assertThat(data.getExpiryTime()).isEqualTo(expiryTime);
        softly.assertAll();
    }

    @Test
    public void testIsEmpty() {
        mCachedData.setData(new Object());
        assertThat(mCachedData.isEmpty()).isFalse();
    }

    @Test
    public void testIsEmptyWithoutData() {
        assertThat(mCachedData.isEmpty()).isTrue();
    }

    @Test
    public void testGetData() {
        Object value = new Object();
        mCachedData.setData(value);
        assertThat(mCachedData.getData()).isEqualTo(value);
    }

    @Test
    public void testGetDataWithoutData() {
        assertThat(mCachedData.getData()).isNull();
    }

    @Test
    public void testSetExpirationPolicy() {
        long refreshTime = 542312L;
        long expiryTime = 666777;
        mCachedData.setExpirationPolicy(refreshTime, expiryTime);
        assertThat(mCachedData.getRefreshTime()).isEqualTo(refreshTime);
        assertThat(mCachedData.getExpiryTime()).isEqualTo(expiryTime);
    }

    @Test
    public void testShouldUpdateWithoutData() {
        assertThat(mCachedData.shouldUpdateData()).isTrue();
    }

    @Test
    public void testShouldClearWithoutData() {
        assertThat(mCachedData.shouldClearData()).isFalse();
    }

    @Test
    public void testShouldUpdateBeforeExpiring() {
        mCachedData.setData(new Object());
        assertThat(mCachedData.shouldUpdateData()).isFalse();
    }

    @Test
    public void testShouldClearBeforeExpiring() {
        mCachedData.setData(new Object());
        sleepForRefreshTime();
        assertThat(mCachedData.shouldClearData()).isFalse();
    }

    @Test
    public void testShouldUpdateAfterExpiring() {
        mCachedData.setData(new Object());
        sleepForRefreshTime();
        assertThat(mCachedData.shouldUpdateData()).isTrue();
    }

    @Test
    public void testShouldClearAfterExpiring() {
        mCachedData.setData(new Object());
        sleepForExpiryTime();
        assertThat(mCachedData.shouldClearData()).isTrue();
    }

    @Test
    public void testShouldUpdateAndClearAfterSetDataAfterExpiring() {
        mCachedData.setData(new Object());
        sleepForRefreshTime();
        sleepForExpiryTime();
        mCachedData.setData(new Object());
        assertThat(mCachedData.shouldUpdateData()).isFalse();
        assertThat(mCachedData.shouldClearData()).isFalse();
    }

    @Test
    public void testShouldUpdateAndClearAfterSetSameObjectAfterExpiring() {
        Object data = new Object();
        mCachedData.setData(data);
        sleepForRefreshTime();
        sleepForExpiryTime();
        mCachedData.setData(data);
        assertThat(mCachedData.shouldUpdateData()).isFalse();
        assertThat(mCachedData.shouldClearData()).isFalse();
    }

    private void sleepForRefreshTime() {
        sleep(refreshTime + 1);
    }

    private void sleepForExpiryTime() {
        sleep(expiryTime + 1);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}
