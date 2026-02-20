package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasuredJsonMapTest extends CommonTest {

    private MeasuredJsonMap mMeasuredJsonMap;
    private int initialTotalSize;

    @Before
    public void setUp() throws JSONException {
        final JSONObject json = new JSONObject()
            .put("a", "bb")
            .put("cc", "dddd");
        initialTotalSize = 9;
        mMeasuredJsonMap = new MeasuredJsonMap(json.toString());
    }

    @Test
    public void testDefaultConstructor() {
        mMeasuredJsonMap = new MeasuredJsonMap();
        assertThat(mMeasuredJsonMap.size()).isEqualTo(0);
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(0);
        assertThat(mMeasuredJsonMap.toString()).isEqualTo("{}");
    }

    @Test
    public void testInitialLength() {
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize);
    }

    @Test
    public void testPutNonNull() {
        mMeasuredJsonMap.put("123", "456");
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize + 6);
    }

    @Test
    public void testPutNullValue() {
        mMeasuredJsonMap.put("123", null);
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize);
    }

    @Test
    public void testPutNullValueExistingKey() {
        mMeasuredJsonMap.put("a", null);
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize - 3);
        assertThat(mMeasuredJsonMap.containsKey("a")).isFalse();
    }

    @Test
    public void testPutValueExistingKey() {
        mMeasuredJsonMap.put("a", "bbbb");
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize + 2);
        assertThat(mMeasuredJsonMap.get("a")).isEqualTo("bbbb");
    }

    @Test
    public void testRemove() {
        mMeasuredJsonMap.put("key", "value");
        mMeasuredJsonMap.remove("key");
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize);
        assertThat(mMeasuredJsonMap.containsKey("key")).isFalse();
    }

    @Test
    public void testRemoveNonExistingItem() {
        mMeasuredJsonMap.remove("b");
        assertThat(mMeasuredJsonMap.getKeysAndValuesSymbolsCount()).isEqualTo(initialTotalSize);
    }
}
