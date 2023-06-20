package io.appmetrica.analytics.impl.utils;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class JsonUtilsMapToJsonTest extends CommonTest {

    private Object testObject = new Object();
    private Map<String, Object> map;

    @Before
    public void setUp() {
        map = new HashMap<String, Object>();
    }

    @Test
    public void testMapToJsonStringForSimpleMap() {
        String validJson = "{\"string\":\"value\",\"int\":111,\"bool\":false,\"double\":22.444,\"object\":\"" + testObject + "\"}";
        Map<String, Object> eventMap = new LinkedHashMap<String, Object>();
        eventMap.put("string", "value");
        eventMap.put("int", 111);
        eventMap.put("bool", false);
        eventMap.put("double", 22.444);
        eventMap.put("object", testObject);

        assertThat(validJson).isEqualTo(JsonHelper.prepareForJson(eventMap).toString());
        assertThat(validJson).isEqualTo(new JSONObject(eventMap).toString());
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithNull() {
        map.put(randomString(), null);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithString() {
        map.put(randomString(), randomString());
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithInt() {
        map.put(randomString(), new Random().nextInt());
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithLong() {
        map.put(randomString(), new Random().nextLong());
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithBool() {
        map.put(randomString(), new Random().nextBoolean());
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithFloat() {
        map.put(randomString(), new Random().nextFloat());
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithDouble() {
        map.put(randomString(), new Random().nextDouble());
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithBytes() {
        byte[] bytes = new byte[new Random().nextInt(10000)];
        new Random().nextBytes(bytes);
        map.put(randomString(), bytes);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithArrayWithNull() {
        Object[] input = new Object[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            input[i] = null;
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithStringArray() {
        String[] strings = new String[new Random().nextInt(20)];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = randomString();
        }
        map.put(randomString(), strings);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithIntArray() {
        int[] input = new int[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            input[i] = new Random().nextInt();
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithLongArray() {
        long[] input = new long[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            input[i] = new Random().nextLong();
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithBoolArray() {
        boolean[] input = new boolean[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            input[i] = new Random().nextBoolean();
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithFloatArray() {
        float[] input = new float[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            input[i] = new Random().nextFloat();
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithDoubleArray() {
        double[] input = new double[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            input[i] = new Random().nextDouble();
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithArrayOfBytesArray() {
        Object[] input = new Object[new Random().nextInt(20)];
        for (int i = 0; i < input.length; i++) {
            byte[] item = new byte[new Random().nextInt(20)];
            new Random().nextBytes(item);
            input[i] = item;
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithListOfStrings() {
        List<String> input = new ArrayList<String>();
        for (int i = 0; i < new Random().nextInt(20); i++) {
            input.add(randomString());
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithListWithStringsAndNull() {
        List<String> input = new ArrayList<String>();
        input.add(null);
        for (int i = 0; i < new Random().nextInt(20); i++) {
            input.add(randomString());
        }
        input.add(null);
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithSetOfStrings() {
        Set<String> input = new HashSet<String>();
        for (int i = 0; i < new Random().nextInt(20); i++) {
            input.add(randomString());
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithListOfSetsOfStrings() {
        List<Set<String>> input = new ArrayList<Set<String>>();
        for (int i = 0; i < new Random().nextInt(20); i++) {
            Set<String> set = new HashSet<String>();
            for (int j = 0; j < new Random().nextInt(20); j++) {
                set.add(randomString());
            }
            input.add(set);
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    @Test
    public void testPrepareJsonReturnValidJsonForMapWithListOfMapOfString() {
        List<Map<String, String>> input = new ArrayList<Map<String, String>>();
        for (int i = 0; i < new Random().nextInt(20); i++) {
            Map<String, String> map = new HashMap<String, String>();
            for (int j = 0; j < new Random().nextInt(20); j++) {
                map.put(randomString(), randomString());
            }
            input.add(map);
        }
        map.put(randomString(), input);
        testPrepareJsonReturnValidJson(map);
    }

    private String randomString() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(new Random().nextInt(30) + 1);
        return randomStringGenerator.nextString();
    }

    private void testPrepareJsonReturnValidJson(Map<String, Object> map) {
        String expectedValue = new JSONObject(new HashMap<String, Object>(map)).toString();
        String inputValue = JsonHelper.prepareForJson(new HashMap<String, Object>(map)).toString();
        assertThat(inputValue).isEqualTo(expectedValue);
    }

    @Test
    public void testMapToJsonStringWithNestedMap() {
        String validJson = "{\"string\":\"value\",\"object\":\""
                + testObject + "\",\"additional\":{\"inner\":\"value\",\"inner2\":22.5}}";
        Map<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("inner", "value");
        innerMap.put("inner2", 22.5);

        Map<String, Object> eventMap = new LinkedHashMap<String, Object>();
        eventMap.put("string", "value");
        eventMap.put("object", testObject);
        eventMap.put("additional", innerMap);

        assertThat(new JSONObject(eventMap).toString()).isEqualTo(validJson);
        assertThat(JsonHelper.prepareForJson(eventMap).toString()).isEqualTo(validJson);
    }

    @Test
    public void testMapToJsonStringWithNestedArray() {
        String validJson = "{\"string\":\"value\",\"map\":{\"inner\":\"value\",\"inner2\":22.5},\"bool_array\":[false,true,null],\"int_array\":[1,11,111,1111]}";
        Map<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("inner", "value");
        innerMap.put("inner2", 22.5);

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

        assertThat(validJson).isEqualTo(JsonHelper.prepareForJson(eventMap).toString());
        assertThat(validJson).isEqualTo(new JSONObject(eventMap).toString());
    }

    @Test
    public void testMapToJsonStringWithDoubleNestedMap() {
        String validJson = "{\"string\":\"value\",\"map\":{\"inner\":\"value\",\"inner2\":{\"inner-inner\":\""
                + testObject + "\",\"inner-inner2\":22.5}},\"bool_array\":[false,true,null],\"int_array\":[1,11,111,1111]}";
        Map<String, Object> doubleInnerMap = new LinkedHashMap<String, Object>();
        doubleInnerMap.put("inner-inner", testObject);
        doubleInnerMap.put("inner-inner2", 22.5);

        Map<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("inner", "value");
        innerMap.put("inner2", doubleInnerMap);

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

        assertThat(validJson).isEqualTo(JsonHelper.prepareForJson(eventMap).toString());
        assertThat(validJson).isEqualTo(new JSONObject(eventMap).toString());
    }

    @Test
    public void testNullMapToJsonString() {
        assertThat(JsonHelper.mapToJsonString(null)).isNull();
    }

    @Test
    public void testNullMapToJson() {
        assertThat(JsonHelper.mapToJson(null)).isNull();
    }

    @Test
    public void testNullMapToJsonNullEmptyWise() {
        assertThat(JsonHelper.mapToJsonNullEmptyWise(null)).isNull();
    }

    @Test
    public void emptyMapToJsonString() {
        assertThat(JsonHelper.mapToJsonString(new HashMap<String, Object>())).isNull();
    }

    @Test
    public void emptyMapToJson() {
        assertThat(JsonHelper.mapToJson(new HashMap<String, Object>())).isNull();
    }

    @Test
    public void emptyMapToJsonNullEmptyWise() throws JSONException {
        JSONAssert.assertEquals(
                new JSONObject(),
                JsonHelper.mapToJsonNullEmptyWise(new HashMap<String, Object>()),
                true
        );
    }

    @Test
    public void mapToJsonFilled() throws JSONException {
        Map<String, Object> filled = new HashMap<String, Object>();
        filled.put("key1", "value1");
        filled.put("key2", 2);
        JSONObject expected = new JSONObject().put("key1", "value1").put("key2", 2);
        JSONAssert.assertEquals(expected, JsonHelper.mapToJson(filled), true);
        JSONAssert.assertEquals(expected, JsonHelper.mapToJsonNullEmptyWise(filled), true);
        JSONAssert.assertEquals(expected.toString(), JsonHelper.mapToJsonString(filled), true);
    }
}
