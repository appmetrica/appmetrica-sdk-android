package io.appmetrica.analytics.testutils;

import androidx.annotation.NonNull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.assertj.core.api.SoftAssertions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToStringTestUtils {

    private static final List<String> valueQuotes = Arrays.asList("", "'", "\"");

    public static List<Predicate<String>> extractFieldsAndValues(Class clazz, Object object, int modifierPreconditions)
        throws Exception {
        return extractFieldsAndValues(clazz, object, modifierPreconditions, new HashSet<String>());
    }

    public static List<Predicate<String>> extractFieldsAndValues(Class clazz, Object object, int modifierPreconditions,
                                                                 @NonNull Set<String> excludedFields)
        throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        List<Predicate<String>> result = new ArrayList<>();
        for (final Field field : fields) {
            if (modifiersMatchPreconditions(field.getModifiers(), modifierPreconditions) && !excludedFields.contains(field.getName())) {
                field.setAccessible(true);
                final String value = extractStringFromFieldValue(field, object);
                result.add(new Predicate<String>() {

                    @Override
                    public boolean test(String s) {
                        for (String valueQuote : valueQuotes) {
                            final String pair = String.format("%s=%s%s%s", field.getName(), valueQuote, value, valueQuote);
                            if (s.contains(pair)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @NonNull
                    @Override
                    public String toString() {
                        return String.format("contains pair: %s=%s", field.getName(), value);
                    }
                });
            }
        }
        return result;
    }

    private static String extractStringFromFieldValue(Field field, Object object) throws Exception {
        String result;
        Object value = field.get(object);
        if (field.getType().equals(int[].class)) {
            result = Arrays.toString((int[]) value);
        } else if (field.getType().equals(String[].class)) {
            result = Arrays.toString((String[]) value);
        } else if (field.getType().equals(long[].class)) {
            result = Arrays.toString((long[]) value);
        } else {
            result = String.valueOf(value);
        }
        return result;
    }

    private static boolean modifiersMatchPreconditions(int modifier, int modifierPreconditions) {
        if (Modifier.isStatic(modifier)) {
            return false;
        }
        for (int i = 0; i < 32; i++) {
            int currentBit = 1 << i;
            if ((currentBit & modifierPreconditions) != 0 && (modifier & currentBit) == 0) {
                return false;
            }
        }
        return true;
    }

    public static <T> T mockValue(String clazzName) throws Exception {
        return (T) mockValue(Class.forName(clazzName));
    }

    public static <T> T mockValue(Class<T> clazz) {
        T mock = mock(clazz);
        when(mock.toString()).thenCallRealMethod();
        return mock;
    }

    public static <T> T mockField(Class<T> clazz) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(100);
        T mock = mock(clazz);
        when(mock.toString()).thenReturn(randomStringGenerator.nextString());
        return mock;
    }

    public static void testToString(@NonNull Object actualValue, @NonNull List<Predicate<String>> extractedFieldAndValuesPredicates) {
        String toStringValue = actualValue.toString();

        SoftAssertions softly = new SoftAssertions();
        for (final Predicate<String> fieldAndValuePredicate : extractedFieldAndValuesPredicates) {
            softly.assertThat(toStringValue).matches(fieldAndValuePredicate, String.format("Containing pair: %s)", fieldAndValuePredicate));
        }
        softly.assertAll();
    }
}
