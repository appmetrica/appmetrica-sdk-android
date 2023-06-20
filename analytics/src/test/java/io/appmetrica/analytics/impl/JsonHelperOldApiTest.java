package io.appmetrica.analytics.impl;

import android.os.Build;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
public class JsonHelperOldApiTest extends CommonTest {

    private Object mTestObject = new Object();
    private Map<String, Object> mMap;

    @Before
    public void setUp() {
        mMap = new HashMap<String, Object>();
    }

    @Test
    public void testMapToJsonStringForSimpleMap() throws Exception {
        Map<String, Object> eventMap = new HashMap<String, Object>();
        eventMap.put("string", "value");
        eventMap.put("int", 111);
        eventMap.put("bool", false);
        eventMap.put("double", 22.444);
        eventMap.put("object", mTestObject);

        String json = JsonHelper.mapToJsonString(eventMap);
        assertThat(json.toString()).isEqualTo(new JSONObject(eventMap).toString());
    }

    @Test
    public void testMapToJsonStringWithNestedMap() throws Exception {
        Map<String, Object> innerMap = new HashMap<String, Object>();
        innerMap.put("inner", "value");
        innerMap.put("inner2", 22.5);

        Map<String, Object> eventMap = new HashMap<String, Object>();
        eventMap.put("string", "value");
        eventMap.put("object", mTestObject);
        eventMap.put("additional", innerMap);

        assertThat(new JSONObject(eventMap).get("additional")).isNotInstanceOf(JSONObject.class);
        JSONObject jsonObject = (JSONObject) JsonHelper.prepareForJson(eventMap);
        assertThat(jsonObject.get("additional")).isExactlyInstanceOf(JSONObject.class);

        assertThat(jsonObject.getString("object")).isEqualTo(mTestObject.toString());
        assertThat(jsonObject.get("string")).isEqualTo("value");

        JSONObject innerJson = jsonObject.getJSONObject("additional");
        assertThat(innerJson.getString("inner")).isEqualTo("value");
        assertThat(innerJson.getDouble("inner2")).isEqualTo(22.5);
    }

    @Test
    public void testMapToJsonStringWithNestedArray() throws Exception {
        Map<String, Object> eventMap = new HashMap<String, Object>();

        ArrayList<Boolean> innerArray1 = new ArrayList<Boolean>();
        innerArray1.add(false);
        innerArray1.add(true);
        innerArray1.add(null);
        eventMap.put("bool_array", innerArray1);

        int[] innerArray2 = {1, 11, 111, 1111};
        eventMap.put("int_array", innerArray2);

        assertThat(new JSONObject(eventMap).get("int_array")).isNotInstanceOf(JSONArray.class);
        JSONObject jsonObject = (JSONObject) JsonHelper.prepareForJson(eventMap);

        JSONArray boolArray = (JSONArray) jsonObject.get("bool_array");
        assertThat(boolArray.get(0)).isEqualTo(false);
        assertThat(boolArray.get(1)).isEqualTo(true);
        assertThat(boolArray.opt(2)).isNull();
        assertThat(boolArray.length()).isEqualTo(3);

        JSONArray intArray = jsonObject.getJSONArray("int_array");
        for (int i = 0; i < intArray.length(); i++) {
            assertThat(intArray.getInt(i)).isEqualTo(innerArray2[i]);
        }
        assertThat(intArray.length()).isEqualTo(4);
    }

    @Test
    public void testMapToJsonStringWithNestedArrayAndMap() throws Exception {
        Map<String, Object> innerMap = new HashMap<String, Object>();
        innerMap.put("inner", "value");
        innerMap.put("inner2", 22.5);

        int[] innerArray2 = {1, 11, 111, 1111};

        Map<String, Object> eventMap = new HashMap<String, Object>();
        eventMap.put("string", "value");
        eventMap.put("map", innerMap);
        eventMap.put("int_array", innerArray2);

        assertThat(new JSONObject(eventMap).get("map")).isNotInstanceOf(JSONObject.class);
        JSONObject jsonObject = (JSONObject) JsonHelper.prepareForJson(eventMap);
        assertThat(jsonObject.get("map")).isExactlyInstanceOf(JSONObject.class);

        JSONArray intArray = jsonObject.getJSONArray("int_array");
        for (int i = 0; i < intArray.length(); i++) {
            assertThat(intArray.getInt(i)).isEqualTo(innerArray2[i]);
        }
        assertThat(intArray.length()).isEqualTo(4);

        JSONObject jsonMap = jsonObject.getJSONObject("map");
        assertThat(jsonMap.length()).isEqualTo(2);
        assertThat(jsonMap.getString("inner")).isEqualTo("value");
        assertThat(jsonMap.getDouble("inner2")).isEqualTo(22.5);
    }

    @Test
    public void testMapToJsonStringWithDoubleNestedMap() throws Exception {
        Map<String, Object> doubleInnerMap = new LinkedHashMap<String, Object>();
        doubleInnerMap.put("inner-inner", mTestObject);
        doubleInnerMap.put("inner-inner2", 22.5);

        Map<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("inner", "value");
        innerMap.put("innerMap", doubleInnerMap);

        ArrayList<Boolean> innerArray1 = new ArrayList<Boolean>();
        innerArray1.add(false);
        innerArray1.add(true);
        innerArray1.add(null);

        int[] innerArray2 = {1, 11, 111, 1111};

        Map<String, Object> eventMap = new LinkedHashMap<String, Object>();
        eventMap.put("string", "value");
        eventMap.put("map", innerMap);
        eventMap.put("bool_array", innerArray1);
        eventMap.put("int_array", innerArray2);

        assertThat(new JSONObject(eventMap).get("map")).isNotInstanceOf(JSONObject.class);

        JSONObject jsonObject = (JSONObject) JsonHelper.prepareForJson(eventMap);
        JSONObject innerJsonMap = jsonObject.getJSONObject("map");

        assertThat(innerJsonMap.getString("inner")).isEqualTo("value");

        JSONObject doubleInnerJsonMap = innerJsonMap.getJSONObject("innerMap");

        assertThat(doubleInnerJsonMap.getString("inner-inner")).isEqualTo(mTestObject.toString());
        assertThat(doubleInnerJsonMap.getDouble("inner-inner2")).isEqualTo(22.5);
    }

    @Test
    public void testPrepareJsonForMapWithStringShouldReturnTheSameValueAsJsonObject() {
        mMap.put(randomString(), randomString());
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonForMapWithNullShouldReturnTheSameValueAsJsonObject() {
        mMap.put(randomString(), null);
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonForMapWithIntShouldReturnTheSameValueAsJsonObject() {
        mMap.put(randomString(), new Random().nextInt());
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonForMapWithLongShouldReturnTheSameValueAsJsonObject() {
        mMap.put(randomString(), new Random().nextLong());
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonForMapWithFloatShouldReturnTheSameValuesAsJsonObject() {
        mMap.put(randomString(), new Random().nextFloat());
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonWithDoubleShouldReturnTheSameValueAsJsonObject() {
        mMap.put(randomString(), new Random().nextDouble());
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonWithBooleanShouldReturnTheSameValueAsJsonObject() {
        mMap.put(randomString(), new Random().nextBoolean());
        assertPrepareJsonIsEqualsJsonObject(mMap);
    }

    @Test
    public void testPrepareJsonForMapWithStringArrayShouldReturnCorrectJson() {
        String[] array = new String[1];
        for (int i = 0; i < array.length; i++) {
            array[i] = "string";
        }
        mMap.put("array", array);
        assertThat(JsonHelper.prepareForJson(mMap).toString()).isEqualTo("{\"array\":[\"string\"]}");
    }

    @Test
    public void testJsonObjectForMapWithStringArrayShouldReturnIncorrectJson() {
        String[] array = new String[1];
        array[0] = "string";
        mMap.put("array", array);
        assertThat(new JSONObject(mMap).toString()).isNotEqualTo("{\"array\":[\"string\"]}");
    }

    @Test
    public void testPrepareJsonForMapWithArrayListWithStringShouldReturnCorrectJson() {
        List list = new ArrayList();
        list.add("string");
        mMap.put("array", list);
        assertThat(JsonHelper.prepareForJson(mMap).toString()).isEqualTo("{\"array\":[\"string\"]}");
    }

    @Test
    public void testJsonObjectForMapWithArrayListWithStringShouldReturnIncorrectJson() {
        List list = new ArrayList();
        list.add("string");
        mMap.put("array", list);
        assertThat(new JSONObject(mMap).toString()).isNotEqualTo("{\"array\":[\"string\"]}");
    }

    @Test
    public void testPrepareJsonForMapWithHashSetWithStringShouldReturnCorrectJson() {
        Set set = new HashSet();
        set.add("string");
        mMap.put("array", set);
        assertThat(JsonHelper.prepareForJson(mMap).toString()).isEqualTo("{\"array\":[\"string\"]}");
    }

    @Test
    public void testJsonObjectForMapWithHashSetWithStringShouldReturnIncorrectJson() {
        Set set = new HashSet();
        set.add("string");
        mMap.put("array", set);
        assertThat(new JSONObject(mMap).toString()).isNotEqualTo("{\"array\":[\"string\"]}");
    }

    @Test
    public void testPrepareJsonForMapWithMapWithStringShouldReturnCorrectJson() {
        Map innerMap = new HashMap();
        innerMap.put("key", "value");
        mMap.put("array", innerMap);
        assertThat(JsonHelper.prepareForJson(mMap).toString()).isEqualTo("{\"array\":{\"key\":\"value\"}}");
    }

    @Test
    public void testPrepareJsonForMapWithMapWithStringShouldReturnIncorrectJson() {
        Map innerMap = new HashMap();
        innerMap.put("key", "value");
        mMap.put("array", innerMap);
        assertThat(new JSONObject(mMap).toString()).isNotEqualTo("{\"array\":{\"key\":\"value\"}}");
    }

    private void assertPrepareJsonIsEqualsJsonObject(Map map) {
        String actualValue = JsonHelper.prepareForJson(new HashMap(map)).toString();
        String expectedValue = new JSONObject(new HashMap(map)).toString();
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    private String randomString() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(new Random().nextInt(30) + 1);
        return randomStringGenerator.nextString();
    }
}
