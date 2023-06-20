package io.appmetrica.analytics.impl;

import android.os.Build;
import io.appmetrica.analytics.IdentifiersResult;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class JsonHelperTest extends CommonTest {

    @Test
    public void testOptHexByteArray() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "1116aa");
        assertThat(JsonHelper.optHexByteArray(jsonObject, "key", null)).isEqualTo(new byte[]{17, 22, -86});
    }

    @Test
    public void testOptHexByteArrayIfNotExists() throws Exception {
        JSONObject jsonObject = new JSONObject();
        assertThat(JsonHelper.optHexByteArray(jsonObject, "key", null)).isNull();
    }

    @Test
    public void testOnHexByteArrayIfContainsEmptyString() throws Exception {
        JSONObject jsonObject = new JSONObject().put("key", "");
        assertThat(JsonHelper.optHexByteArray(jsonObject, "key", null)).isEmpty();
    }

    @Test
    public void testOptHexByteArrayForInvalidHexString() throws Exception {
        JSONObject jsonObject = new JSONObject().put("key", "1a5");
        assertThat(JsonHelper.optHexByteArray(jsonObject, "key", null)).isNull();
    }

    @Test
    public void testOptHexByteArrayForNonNullFallback() throws Exception {
        assertThat(JsonHelper.optHexByteArray(new JSONObject(), "key", new byte[]{1, 2, 3}))
            .isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    public void testClidsToStringNull() {
        assertThat(JsonHelper.clidsToString(null)).isNull();
    }

    @Test
    public void testClidsFromStringNull() {
        assertThat(JsonHelper.clidsFromString(null)).isNull();
    }

    @Test
    public void testClidsFromStringEmpty() {
        assertThat(JsonHelper.clidsFromString("")).isNotNull().isEmpty();
    }

    @Test
    public void testClidsToStringNotEmpty() throws JSONException {
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        JSONObject expected = new JSONObject()
            .put("clid0", "0")
            .put("clid1", "1");
        JSONAssert.assertEquals(expected.toString(), JsonHelper.clidsToString(clids), true);
    }

    @Test
    public void testClidsFromStringNotEmpty() throws JSONException {
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        JSONObject clidsJson = new JSONObject()
            .put("clid0", "0")
            .put("clid1", "1");
        assertThat(JsonHelper.clidsFromString(clidsJson.toString())).isEqualTo(clids);
    }

    @Test
    public void testAdsIdentifiersToJson() throws Exception {
        final String id = "4365384768";
        final IdentifierStatus status = IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE;
        final String error = "error explanation";
        IdentifiersResult adsIdentifiers = new IdentifiersResult(id, status, error);
        JSONObject json = JsonHelper.adsIdentifiersResultToJson(adsIdentifiers);
        IdentifiersResult deserialized = JsonHelper.adsIdentifiersResultFromJson(json);
        assertThat(deserialized).isEqualToComparingFieldByField(adsIdentifiers);
        ObjectPropertyAssertions<IdentifiersResult> assertions = ObjectPropertyAssertions(deserialized);
        assertions.checkField("id", id);
        assertions.checkField("status", status);
        assertions.checkField("errorExplanation", error);
        assertions.checkAll();
    }

    @Test
    public void testAutoInappCollectingConfigToJson() throws Exception {
        final int sendFrequencySeconds = 232343;
        final int firstCollectingInappMaxAgeSeconds = 64643;
        BillingConfig config = new BillingConfig(sendFrequencySeconds, firstCollectingInappMaxAgeSeconds);
        JSONObject json = JsonHelper.autoInappCollectingConfigToJson(config);
        BillingConfig deserialized = JsonHelper.autoInappCollectingConfigFromJson(json);

        assertThat(deserialized).isEqualToComparingFieldByField(config);
        ObjectPropertyAssertions<BillingConfig> assertions = ObjectPropertyAssertions(deserialized);
        assertions.checkField("sendFrequencySeconds", sendFrequencySeconds);
        assertions.checkField("firstCollectingInappMaxAgeSeconds", firstCollectingInappMaxAgeSeconds);
        assertions.checkAll();
    }

    @Test
    public void testAutoInappCollectingConfigToJsonIfEmpty() throws Exception {
        JSONObject json = new JSONObject();
        BillingConfig deserialized = JsonHelper.autoInappCollectingConfigFromJson(json);

        ObjectPropertyAssertions<BillingConfig> assertions = ObjectPropertyAssertions(deserialized);
        assertions.checkField("sendFrequencySeconds", 86400);
        assertions.checkField("firstCollectingInappMaxAgeSeconds", 86400);
        assertions.checkAll();
    }

    @Test
    public void screenInfoFromEmptyJson() throws Exception {
        assertScreenInfoIsEmpty(JsonHelper.screenInfoFromJson(new JSONObject()));
    }

    @Test
    public void screenInfoFromFilledJson() throws JSONException {
        int width = 364578;
        int height = 3478;
        int dpi = 888999;
        float scaleFactor = 5.6f;
        String deviceType = DeviceTypeValues.TABLET;
        JSONObject json = new JSONObject()
            .put("width", width)
            .put("height", height)
            .put("dpi", dpi)
            .put("scaleFactor", scaleFactor)
            .put("deviceType", "tablet");
        assertThat(JsonHelper.screenInfoFromJson(json)).isEqualTo(new ScreenInfo(width, height, dpi, scaleFactor, deviceType));
    }

    @Test
    public void screenInfoFromFilledJsonString() throws Exception {
        String filledJson = "{\"width\":24,\"height\":76,\"dpi\":50,\"scaleFactor\":'0.5',\"deviceType\":tablet}";
        ObjectPropertyAssertions(JsonHelper.screenInfoFromJsonString(filledJson))
            .checkField("width", "getWidth", 24)
            .checkField("height", "getHeight", 76)
            .checkField("dpi", "getDpi", 50)
            .checkFloatField("scaleFactor", "getScaleFactor", 0.5f, 0.0000001f)
            .checkField("deviceType", "getDeviceType", DeviceTypeValues.TABLET)
            .checkAll();
    }

    @Test
    public void screenInfoFromNullString() throws Exception {
        assertThat(JsonHelper.screenInfoFromJsonString(null)).isNull();
    }

    @Test
    public void screenInfoFromEmptyString() throws Exception {
        assertThat(JsonHelper.screenInfoFromJsonString("")).isNull();
    }

    @Test
    public void screenInfoFromInvalidJsonString() throws Exception {
        assertThat(JsonHelper.screenInfoFromJsonString("asdsad")).isNull();
    }

    private void assertScreenInfoIsEmpty(ScreenInfo screenInfo)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ObjectPropertyAssertions(screenInfo)
            .checkField("width", "getWidth", 0)
            .checkField("height", "getHeight", 0)
            .checkField("dpi", "getDpi", 0)
            .checkFloatField("scaleFactor", "getScaleFactor", 0f, 0.0000001f)
            .checkField("deviceType", DeviceTypeValues.PHONE)
            .checkAll();
    }

    @Test
    public void nullScreenInfoToJson() {
        assertThat(JsonHelper.screenInfoToJson(null)).isNull();
    }

    @Test
    public void nonNullScreenInfoToJson() throws JSONException {
        int width = 364578;
        int height = 3478;
        int dpi = 888999;
        float scaleFactor = 5.6f;
        String deviceType = DeviceTypeValues.TABLET;
        ScreenInfo screenInfo = new ScreenInfo(width, height, dpi, scaleFactor, deviceType);
        JSONObject expected = new JSONObject()
            .put("width", width)
            .put("height", height)
            .put("dpi", dpi)
            .put("scaleFactor", scaleFactor)
            .put("deviceType", "tablet");
        JSONObject actual = JsonHelper.screenInfoToJson(screenInfo);
        JSONAssert.assertEquals(actual, expected, true);
        assertThat(JsonHelper.screenInfoFromJson(actual)).isEqualTo(screenInfo);
    }

    @Test
    public void nullScreenInfoToJsonString() throws Exception {
        assertThat(JsonHelper.screenInfoToJsonString(null)).isNull();
    }

    @Test
    public void nonNullScreenInfoToJsonString() throws JSONException {
        int width = 34;
        int height = 654;
        int dpi = 12312;
        float scaleFactor = 23423f;
        String deviceType = DeviceTypeValues.TV;
        String expected = new JSONObject()
            .put("width", width)
            .put("height", height)
            .put("dpi", dpi)
            .put("scaleFactor", scaleFactor)
            .put("deviceType", "tv")
            .toString();
        String actual = JsonHelper.screenInfoToJsonString(new ScreenInfo(width, height, dpi, scaleFactor, deviceType));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void toStringListNull() {
        assertThat(JsonHelper.toStringList((String) null)).isNull();
    }

    @Test
    public void toStringListEmpty() {
        assertThat(JsonHelper.toStringList("")).isNull();
    }

    @Test
    public void toStringListNotAJson() {
        assertThat(JsonHelper.toStringList("aaa")).isNull();
    }

    @Test
    public void toStringListEmptyJsonArray() {
        assertThat(JsonHelper.toStringList("[]")).isNull();
    }

    @Test
    public void toStringListValidJsonArray() {
        assertThat(JsonHelper.toStringList("[\"aaa\", \"bbb\"]")).containsExactly("aaa", "bbb");
    }

    @Test
    public void optLongOrDefaultWithBackup() throws JSONException {
        String key = "key";
        long firstValue = 100L;
        long secondValue = 200L;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(firstJson, secondJson, key, -1L))
            .isEqualTo(secondValue);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, secondJson, key, null))
            .isNull();
    }

    @Test
    public void optLongOrDefaultWithBackupIfFirstNull() throws JSONException {
        String key = "key";
        long secondValue = 200L;
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(null, secondJson, key, -1L))
            .isEqualTo(secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(null, secondJson, key, null))
            .isEqualTo(secondValue);
    }

    @Test
    public void optLongOrDefaultWithBackupIfSecondNull() throws JSONException {
        String key = "key";
        long firstValue = 100L;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(firstJson, null, key, -1L)).isEqualTo(firstValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(firstJson, null, key, null)).isEqualTo(firstValue);
    }

    @Test
    public void optLongOrDefaultWithBackupIfBothNull() throws JSONException {
        assertThat(JsonHelper.optLongOrDefaultWithBackup(null, null, "key", -1L)).isEqualTo(-1L);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(null, null, "key", null)).isNull();
    }

    @Test
    public void optLongOrDefaultWithPrimaryIfEmpty() throws JSONException {
        String key = "key";
        long secondValue = 200L;
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(new JSONObject(), secondJson, key, -1L))
            .isEqualTo(secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(new JSONObject(), secondJson, key, null))
            .isEqualTo(secondValue);
    }

    @Test
    public void optLongOrDefaultWithSecondIsEmpty() throws JSONException {
        String key = "key";
        long firstValue = 100L;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(firstJson, new JSONObject(), key, -1L))
            .isEqualTo(firstValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(firstJson, new JSONObject(), key, null))
            .isEqualTo(firstValue);
    }

    @Test
    public void optLongOrDefaultWithBothEmpty() throws JSONException {
        assertThat(JsonHelper.optLongOrDefaultWithBackup(new JSONObject(), new JSONObject(), "key", -1L))
            .isEqualTo(-1L);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(new JSONObject(), new JSONObject(), "key", null)).isNull();
    }

    @Test
    public void optLongOrDefaultWithFirstInvalidValue() throws JSONException {
        String key = "key";
        long secondValue = 200L;
        JSONObject first = new JSONObject().put(key, "invalid value");
        JSONObject second = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(first, second, key, -1L)).isEqualTo(secondValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(first, second, key, null)).isEqualTo(secondValue);
    }

    @Test
    public void optLongOrDefaultWithSecondInvalidValue() throws JSONException {
        String key = "key";
        long firstValue = 100L;
        JSONObject first = new JSONObject().put(key, firstValue);
        JSONObject second = new JSONObject().put(key, "invalid value");
        assertThat(JsonHelper.optLongOrDefaultWithBackup(first, second, key, -1L)).isEqualTo(firstValue);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(first, second, key, null)).isEqualTo(firstValue);
    }

    @Test
    public void optLongOrDefaultWithBothInvalidValues() throws JSONException {
        String key = "key";
        JSONObject first = new JSONObject().put(key, "first invalid value");
        JSONObject second = new JSONObject().put(key, "secon invalid value");
        assertThat(JsonHelper.optLongOrDefaultWithBackup(first, second, key, -1L)).isEqualTo(-1L);
        assertThat(JsonHelper.optLongOrDefaultWithBackup(first, second, key, null)).isNull();
    }

    @Test
    public void optStringOrDefaultWithBackup() throws JSONException {
        String key = "key";
        String firstValue = "first value";
        String secondValue = "second value";
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optStringOrDefaultWithBackup(firstJson, secondJson, key)).isEqualTo(secondValue);
    }

    @Test
    public void optStringOrDefaultIfFirstIsNull() throws JSONException {
        String key = "key";
        String secondValue = "second value";
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optStringOrDefaultWithBackup(null, secondJson, key)).isEqualTo(secondValue);
    }

    @Test
    public void optStringOrDefaultIfSecondIsNull() throws JSONException {
        String key = "key";
        String firstValue = "first value";
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optStringOrDefaultWithBackup(firstJson, null, key)).isEqualTo(firstValue);
    }

    @Test
    public void optStringOrDefaultIfFirstIsEmpty() throws JSONException {
        String key = "key";
        String secondValue = "second value";
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optStringOrDefaultWithBackup(new JSONObject(), secondJson, key)).isEqualTo(secondValue);
    }

    @Test
    public void optStringOrDefaultIfSecondIsEmpty() throws JSONException {
        String key = "key";
        String firstValue = "first value";
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optStringOrDefaultWithBackup(firstJson, new JSONObject(), key)).isEqualTo(firstValue);
    }

    @Test
    public void optStringOrDefaultIfBothIsEmpty() throws JSONException {
        assertThat(JsonHelper.optStringOrDefaultWithBackup(new JSONObject(), new JSONObject(), "key")).isNull();
    }

    @Test
    public void optIntegerOrNull() throws JSONException {
        String key = "key";
        int value = 1231;
        JSONObject jsonObject = new JSONObject().put(key, value);
        assertThat(JsonHelper.optIntegerOrNull(jsonObject, key)).isEqualTo(value);
    }

    @Test
    public void optIntegerOrNullIfJsonIsNull() throws JSONException {
        assertThat(JsonHelper.optIntegerOrNull(null, "key")).isNull();
    }

    @Test
    public void optIntegerOrNullIfEmptyJson() throws JSONException {
        assertThat(JsonHelper.optIntegerOrNull(new JSONObject(), "key")).isNull();
    }

    @Test
    public void optIntegerOrNullIsInvalidValue() throws JSONException {
        String key = "key";
        JSONObject jsonObject = new JSONObject().put(key, "invalid value");
        assertThat(JsonHelper.optIntegerOrNull(jsonObject, key)).isNull();
    }

    @Test
    public void optIntegerOrDefault() throws JSONException {
        String key = "key";
        int value = 1231;
        JSONObject jsonObject = new JSONObject().put(key, value);
        assertThat(JsonHelper.optIntegerOrDefault(jsonObject, key, -100500)).isEqualTo(value);
        assertThat(JsonHelper.optIntegerOrDefault(jsonObject, key, null)).isEqualTo(value);
    }

    @Test
    public void optIntegerOrDefaultForNullJson() throws JSONException {
        assertThat(JsonHelper.optIntegerOrDefault(null, "key", -100500)).isEqualTo(-100500);
        assertThat(JsonHelper.optIntegerOrDefault(null, "key", null)).isNull();
    }

    @Test
    public void optIntegerOrNullForEmptyJson() throws JSONException {
        assertThat(JsonHelper.optIntegerOrDefault(new JSONObject(), "key", -100500)).isEqualTo(-100500);
        assertThat(JsonHelper.optIntegerOrDefault(new JSONObject(), "key", null)).isNull();
    }

    @Test
    public void optIntegerOrNullForInvalidValue() throws JSONException {
        String key = "key";
        JSONObject jsonObject = new JSONObject().put(key, "invalid value");
        assertThat(JsonHelper.optIntegerOrDefault(jsonObject, key, -100500)).isEqualTo(-100500);
        assertThat(JsonHelper.optIntegerOrDefault(jsonObject, key, null)).isNull();
    }

    @Test
    public void optIntegerOrDefaultWithBackup() throws JSONException {
        String key = "key";
        int firstValue = 78;
        int secondValue = 54;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(firstJson, secondJson, key, null))
            .isEqualTo(secondValue);
    }

    @Test
    public void optIntegerOrDefaultWithBackupForFirstNullJson() throws JSONException {
        String key = "key";
        int secondValue = 1231;
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(null, secondJson, key, null))
            .isEqualTo(secondValue);
    }

    @Test
    public void optIntegerOrDefaultWithBackupForSecondNullJson() throws JSONException {
        String key = "key";
        int firstValue = 123;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(firstJson, null, key, -100500))
            .isEqualTo(firstValue);
    }

    @Test
    public void integerOrDefaultWithBackupForBothNullJson() throws JSONException {
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(null, null, "key", -100500))
            .isEqualTo(-100500);
    }

    @Test
    public void integerOrDefaultWithBackupForFirstEmpty() throws JSONException {
        String key = "key";
        int secondValue = 1231;
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(new JSONObject(), secondJson, key, -100500))
            .isEqualTo(secondValue);
    }

    @Test
    public void integerOrDefaultWithBackupForSecondEmpty() throws JSONException {
        String key = "key";
        int firstValue = 123;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(firstJson, new JSONObject(), key, -100500))
            .isEqualTo(firstValue);
    }

    @Test
    public void integerOrDefaultWithBackupForBothEmpty() throws JSONException {
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(new JSONObject(), new JSONObject(), "key", -100500))
            .isEqualTo(-100500);
    }

    @Test
    public void integerOrDefaultWithBackupForFirstInvalid() throws JSONException {
        String key = "key";
        int secondValue = 1231;
        JSONObject firstJson = new JSONObject().put(key, "invalid value");
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(firstJson, secondJson, key, -100500))
            .isEqualTo(secondValue);
    }

    @Test
    public void integerOrDefaultWithBackupForSecondInvalid() throws JSONException {
        String key = "key";
        int firstValue = 2312;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        JSONObject secondJson = new JSONObject().put(key, "invalid vaue");
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(firstJson, secondJson, key, -100500))
            .isEqualTo(firstValue);
    }

    @Test
    public void integerOrDefaultWithBackupForBothInvalid() throws JSONException {
        String key = "key";
        JSONObject firstJson = new JSONObject().put(key, "first invalid value");
        JSONObject secondJson = new JSONObject().put(key, "second value");
        assertThat(JsonHelper.optIntegerOrDefaultWithBackup(firstJson, secondJson, key, -100500)).isEqualTo(-100500);
    }

    @Test
    public void optBooleanOrDefaultWithBackup() throws JSONException {
        String key = "key";
        boolean firstValue = true;
        boolean secondValue = false;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, secondJson, key, null))
            .isEqualTo(secondValue);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfFirstIsNull() throws JSONException {
        String key = "key";
        boolean secondValue = true;
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(null, secondJson, key, null))
            .isEqualTo(secondValue);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfSecondIsNull() throws JSONException {
        String key = "key";
        boolean firstValue = true;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, null, key, false))
            .isEqualTo(firstValue);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfBothAreNull() throws JSONException {
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(null, null, "key", false)).isFalse();
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfFirstIsEmpty() throws JSONException {
        String key = "key";
        boolean secondValue = true;
        JSONObject secondJson = new JSONObject().put(key, secondValue);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(new JSONObject(), secondJson, key, false))
            .isEqualTo(secondValue);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfSecondIsEmpty() throws JSONException {
        String key = "key";
        boolean firstValue = true;
        JSONObject firstJson = new JSONObject().put(key, firstValue);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, new JSONObject(), "key", false))
            .isEqualTo(firstValue);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfBothAreEmpty() throws JSONException {
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(new JSONObject(), new JSONObject(), "key", false))
            .isFalse();
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfFirstIsWrong() throws JSONException {
        String key = "key";
        boolean second = true;
        JSONObject firstJson = new JSONObject().put(key, "wrong value");
        JSONObject secondJson = new JSONObject().put(key, second);
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, secondJson, key, false))
            .isEqualTo(second);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfSecondIsWrong() throws JSONException {
        String key = "key";
        boolean first = true;
        JSONObject firstJson = new JSONObject().put(key, first);
        JSONObject secondJson = new JSONObject().put(key, "wrong value");
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, secondJson, key, false))
            .isEqualTo(first);
    }

    @Test
    public void optBooleanOrDefaultWithBackupIfBothAreWrong() throws JSONException {
        String key = "key";
        JSONObject firstJson = new JSONObject().put(key, "first wrong value");
        JSONObject secondJson = new JSONObject().put(key, "second wrong value");
        assertThat(JsonHelper.optBooleanOrDefaultWithBackup(firstJson, secondJson, key, false)).isFalse();
    }
}
