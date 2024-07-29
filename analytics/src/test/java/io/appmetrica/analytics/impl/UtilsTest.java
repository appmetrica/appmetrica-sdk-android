package io.appmetrica.analytics.impl;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.impl.utils.limitation.StringByBytesTrimmer;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.net.ssl.HttpsURLConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class UtilsTest extends CommonTest {

    @Mock
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testJsonToMap() {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            when(JsonHelper.jsonToMap(anyString())).thenReturn(new HashMap<String, String>() {
                {
                    put("key", "value");
                }
            });
            assertThat(JsonHelper.jsonToMap("bla-bla-bla").size()).isEqualTo(1);
        }
    }

    @Test
    public void testCutStringForUnicode() {
        int stringSize = 100;
        String testString = createCyrillicString(stringSize);
        int bytesCount = testString.getBytes().length;

        for (int i = 1; i < bytesCount; i++) {
            String result = new StringByBytesTrimmer(bytesCount - i, "test", mock(PublicLogger.class)).trim(testString);
            assertThat(result).isNotEmpty();
        }
    }

    private String createCyrillicString(int size) {
        StringBuilder sb = new StringBuilder(size);
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            char c = (char) (r.nextInt(30) + '\u0430'); // = cyrillic 'а'
            sb.append(c);
        }
        return sb.toString();
    }

    @Test
    public void testEncodeClids() {
        Map<String, String> map = new TreeMap<String, String>() {
            {
                put("231", "321");
                put("test1", "val1");
            }
        };
        String s = StartupUtils.encodeClids(map);
        assertThat(s).isEqualTo("231:321,test1:val1");
    }

    @Test
    public void testEncodeClidsForOnePair() {
        Map<String, String> map = new TreeMap<String, String>() {
            {
                put("test1", "val1");
            }
        };
        String s = StartupUtils.encodeClids(map);
        assertThat(s).isEqualTo("test1:val1");
    }

    @Test
    public void testEncodeClidsForEmptyValue() {
        Map<String, String> map = new TreeMap<String, String>();
        String s = StartupUtils.encodeClids(map);
        assertThat(s).isEqualTo("");
    }

    @Test
    public void testEncodeClidsForNullValue() {
        String s = StartupUtils.encodeClids(null);
        assertThat(s).isEqualTo("");
    }

    @Test
    public void testDecodeClids() {
        String encoded = "clid:val,clid11:123";
        Map<String, String> decoded = StartupUtils.decodeClids(encoded);

        assertThat(decoded).isNotNull();
        assertThat(decoded.size()).isEqualTo(2);
        assertThat(decoded.containsKey("clid"));
        assertThat(decoded.get("clid")).isEqualTo("val");
        assertThat(decoded.containsKey("clid1"));
        assertThat(decoded.get("clid11")).isEqualTo("123");
    }

    @Test
    public void testDecodeClidsOnePair() {
        String encoded = "clid:val";
        Map<String, String> decoded = StartupUtils.decodeClids(encoded);
        assertThat(decoded).isNotNull();
        assertThat(decoded.size()).isEqualTo(1);
        assertThat(decoded.containsKey("clid"));
        assertThat(decoded.get("clid")).isEqualTo("val");
    }

    @Test
    public void testDecodeClidsEndsWithColon() {
        String encoded = "clid:val,clid11:123,";
        Map<String, String> decoded = StartupUtils.decodeClids(encoded);
        assertThat(decoded.size()).isEqualTo(2);
        assertThat(decoded.get("clid")).isEqualTo("val");
        assertThat(decoded.get("clid11")).isEqualTo("123");
    }

    @Test
    public void testDecodeClidsForNull() {
        Map<String, String> map = StartupUtils.decodeClids(null);
        assertThat(map).isNotNull();
        assertThat(map.size()).isEqualTo(0);
    }

    @Test
    public void testDecodeClidsForEmpty() {
        Map<String, String> map = StartupUtils.decodeClids(StringUtils.EMPTY);
        assertThat(map).isNotNull();
        assertThat(map.size()).isEqualTo(0);
    }

    @Test
    public void testDecodeClidsForEmptyValues() {
        String encoded = "clid:,clid11:";
        Map<String, String> decoded = StartupUtils.decodeClids(encoded);
        assertThat(decoded.get("clid")).isEqualTo("");
        assertThat(decoded.get("clid11")).isEqualTo("");
    }

    @Test
    public void testValidateValidClids() {
        Map<String, String> validClids = TestData.TEST_CLIDS;
        Map<String, String> resultClids = StartupUtils.validateClids(TestData.TEST_CLIDS);

        assertThat(resultClids).isEqualTo(validClids);
    }

    @Test
    public void testValidateNonNumberValueClids() {
        Map<String, String> badClids = new HashMap<String, String>(TestData.TEST_CLIDS);
        badClids.put("clid91", "non_integer");
        badClids.put("clid92", "123;321");

        Map<String, String> resultClids = StartupUtils.validateClids(badClids);

        assertThat(resultClids).isEqualTo(TestData.TEST_CLIDS);
        assertThat(resultClids.size()).isEqualTo(TestData.TEST_CLIDS.size());
        assertThat(resultClids.containsKey("clid91")).isFalse();
        assertThat(resultClids.containsKey("clid92")).isFalse();
    }

    @Test
    public void testValidateBadKeyClids() {
        Map<String, String> badClids = new HashMap<String, String>(TestData.TEST_CLIDS);
        badClids.put("val:ue", "55555");
        badClids.put("va,l,e", "123321");
        badClids.put("val&e", "123321");

        Map<String, String> resultClids = StartupUtils.validateClids(badClids);

        assertThat(resultClids.size()).isEqualTo(TestData.TEST_CLIDS.size());
        assertThat(resultClids).isEqualTo(TestData.TEST_CLIDS);
    }

    @Test
    public void testValidateClidsForBadKeyAndValues() {
        Map<String, String> badClids = new HashMap<String, String>();
        badClids.put("bad:key", "55555");
        badClids.put("bad:keyVal", "bad55value");
        badClids.put("badVal1", "1,");
        badClids.put("badVal2", "1:");
        badClids.put("badVal3", ":1");
        badClids.put("badVal4", ",1");
        badClids.put("badVal5", "123:321");
        badClids.put("badVal6", "123,321");
        badClids.put("good", "123321");

        Map<String, String> resultClids = StartupUtils.validateClids(badClids);

        assertThat(resultClids.size()).isEqualTo(1);
        assertThat(resultClids.containsKey("good")).isTrue();
    }

    @Test
    public void testValidateClidsForNull() {
        assertThat(StartupUtils.validateClids(null)).isNotNull();
    }

    @RunWith(RobolectricTestRunner.class)
    public static class DescriptionMyMessageAndExceptionTest {

        @Test
        public void testIsNullOrEmptyCursorNull() {
            assertThat(Utils.isNullOrEmpty((Cursor) null)).isTrue();
        }

        @Test
        public void testIsNullOrEmptyCursorEmpty() {
            Cursor cursor = mock(Cursor.class);
            when(cursor.getCount()).thenReturn(0);
            assertThat(Utils.isNullOrEmpty(cursor)).isTrue();
        }

        @Test
        public void testIsNullOrEmptyCursorFilled() {
            Cursor cursor = mock(Cursor.class);
            when(cursor.getCount()).thenReturn(1);
            assertThat(Utils.isNullOrEmpty(cursor)).isFalse();
        }

    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ConvertLongArrayToStringArrayTests {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Test convert long array {2} to string array")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {new long[]{}, new String[]{}, "{}"},
                    {new long[]{1}, new String[]{"1"}, "{1}"},
                    {new long[]{1, 2, 3}, new String[]{"1", "2", "3"}, "{1,2,3}"},
                    {new long[]{-1}, new String[]{"-1"}, "{-1}"},
                    {new long[]{0}, new String[]{"0"}, "{0}"}
            });
        }

        private long[] mInputArray;
        private String[] mExpectedArray;
        private String mStringPresentation;

        public ConvertLongArrayToStringArrayTests(Object inputArray, Object expectedArray, Object stringPresentation) {
            mInputArray = (long[]) inputArray;
            mExpectedArray = (String[]) expectedArray;
            mStringPresentation = (String) stringPresentation;
        }

        @Test
        public void test() {
            assertThat(Utils.convertToStringArray(mInputArray)).isEqualTo(mExpectedArray);
        }
    }

    @Test
    public void testGetStackTraceSafely() {
        StackTraceElement[] stacktrace = new StackTraceElement[5];
        Throwable throwable = mock(Throwable.class);
        when(throwable.getStackTrace()).thenReturn(stacktrace);
        assertThat(Utils.getStackTraceSafely(throwable)).isEqualTo(stacktrace);
    }

    @Test
    public void testGetStackTraceSafelyNull() {
        assertThat(Utils.getStackTraceSafely(null)).isNotNull().isEmpty();
    }

    @Test
    public void testGetStackTraceSafelyException() {
        Throwable throwable = mock(Throwable.class);
        when(throwable.getStackTrace()).thenThrow(new NullPointerException());
        assertThat(Utils.getStackTraceSafely(throwable)).isNotNull().isEmpty();
    }

    @Test
    public void trimToSizeNull() {
        assertThat(Utils.trimToSize((String) null, 0)).isNull();
    }

    @Test
    public void trimToSizeSmallValue() {
        final String input = "aabbccddааббввггдд";
        assertThat(Utils.trimToSize(input, 20)).isEqualTo(input);
    }

    @Test
    public void trimToSizeLimitValue() {
        final String input = "aabbccddааббввггдд";
        assertThat(Utils.trimToSize(input, 18)).isEqualTo(input);
    }

    @Test
    public void trimToSizeBigValue() {
        assertThat(Utils.trimToSize("aabbccddааббввггдд", 15)).isEqualTo("aabbccddааббввг");
    }

    @Test
    public void areAllNullOrEmptyForSingleNonNull() {
        assertThat(Utils.areAllNullOrEmpty("value")).isFalse();
    }

    @Test
    public void areAllNullOrEmptyForSingleNull() {
        assertThat(Utils.areAllNullOrEmpty((String) null)).isTrue();
    }

    @Test
    public void areAllNullOrEmptyForSingleEmpty() {
        assertThat(Utils.areAllNullOrEmpty("")).isTrue();
    }

    @Test
    public void areAllNullOrEmptyForMultipleWithNullAndEmpty() {
        assertThat(Utils.areAllNullOrEmpty("value", null, "")).isFalse();
    }

    @Test
    public void areAllNullOrEmptyForAllNull() {
        assertThat(Utils.areAllNullOrEmpty(null, null, null)).isTrue();
    }

    @Test
    public void areAllNullOrEmptyForAllEmpty() {
        assertThat(Utils.areAllNullOrEmpty("", "", "")).isTrue();
    }

    @Test
    public void areAllNullOrEmptyForNullAndEmpty() {
        assertThat(Utils.areAllNullOrEmpty("", null, null, "")).isTrue();
    }

    @Test
    public void getBooleanOrNullNoKey() {
        assertThat(Utils.getBooleanOrNull(new Bundle(), "key")).isNull();
    }

    @Test
    public void getBooleanOrNullTrue() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("key", true);
        assertThat(Utils.getBooleanOrNull(bundle, "key")).isTrue();
    }

    @Test
    public void getBooleanOrNullFalse() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("key", false);
        assertThat(Utils.getBooleanOrNull(bundle, "key")).isFalse();
    }

    @Test
    public void getIntOrNullNoKey() {
        assertThat(Utils.getIntOrNull(new Bundle(), "key")).isNull();
    }

    @Test
    public void geIntOrNullTrue() {
        Bundle bundle = new Bundle();
        bundle.putInt("key", 333);
        assertThat(Utils.getIntOrNull(bundle, "key")).isEqualTo(333);
    }

    @Test
    public void applicationFromContextSuccess() {
        Application application = mock(Application.class);
        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(application);
        assertThat(Utils.applicationFromContext(context)).isSameAs(application);
    }

    @Test
    public void applicationFromContextNullApplicationContext() {
        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(null);
        assertThat(Utils.applicationFromContext(context)).isNull();
    }

    @Test
    public void applicationFromContextNotApplication() {
        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context);
        assertThat(Utils.applicationFromContext(context)).isNull();
    }

    @Test
    public void convertStackTraceToInternalEmptyArray() {
        assertThat(Utils.convertStackTraceToInternal(new StackTraceElement[0])).isNotNull().isEmpty();
    }

    @Test
    public void convertStackTraceToInternalFilledArray() {
        StackTraceElement element1 = new StackTraceElement("cl1", "m1", "f1", 1);
        StackTraceElement element2 = new StackTraceElement("cl2", "m2", "f2", 2);
        assertThat(Utils.convertStackTraceToInternal(new StackTraceElement[] {element1, element2}))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new StackTraceItemInternal(element1), new StackTraceItemInternal(element2));
    }

    @Test
    public void convertStackTraceToInternalEmptyList() {
        assertThat(Utils.convertStackTraceToInternal(new ArrayList<StackTraceElement>())).isNotNull().isEmpty();
    }

    @Test
    public void convertStackTraceToInternalFilledList() {
        StackTraceElement element1 = new StackTraceElement("cl1", "m1", "f1", 1);
        StackTraceElement element2 = new StackTraceElement("cl2", "m2", "f2", 2);
        assertThat(Utils.convertStackTraceToInternal(Arrays.asList(element1, element2)))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new StackTraceItemInternal(element1), new StackTraceItemInternal(element2));
    }

    @Test
    public void isBadRequestFor200() {
        assertThat(Utils.isBadRequest(HttpsURLConnection.HTTP_OK)).isFalse();
    }

    @Test
    public void isBadRequestFor400() {
        assertThat(Utils.isBadRequest(HttpsURLConnection.HTTP_BAD_REQUEST)).isTrue();
    }

    @Test
    public void notIsBadRequestConditionFor200() {
        NetworkTask.ShouldTryNextHostCondition condition = Utils.notIsBadRequestCondition();
        assertThat(condition.shouldTryNextHost(HttpsURLConnection.HTTP_OK)).isTrue();
    }

    @Test
    public void notIsBadRequestConditionFor400() {
        NetworkTask.ShouldTryNextHostCondition condition = Utils.notIsBadRequestCondition();
        assertThat(condition.shouldTryNextHost(HttpsURLConnection.HTTP_BAD_REQUEST)).isFalse();
    }

    @Test
    public void joinToArrayForEmpty() {
        assertThat(Utils.joinToArray(Collections.<String>emptyList())).isEmpty();
    }

    @Test
    public void joinToArrayForListOnly() {
        String[] value = new String[] {"first", "second", "third"};
        assertThat(Utils.joinToArray(Arrays.asList(value))).isEqualTo(value);
    }

    @Test
    public void joinToArrayForNonListElements() {
        assertThat(Utils.joinToArray(Collections.<String>emptyList(), "first", "second", "third"))
                .containsExactly("first", "second", "third");
    }

    @Test
    public void joinToArray() {
        assertThat(Utils.joinToArray(Arrays.asList("first", "second"), "third"))
                .containsExactly("first", "second", "third");
    }
}
