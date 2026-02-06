package io.appmetrica.analytics.testutils;

import android.os.Build;
import android.util.Pair;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.util.ReflectionHelpers;

public class TestUtils {

    public static Collection<Integer> generateSequence(int from, int to) {
        List<Integer> list = new ArrayList<Integer>(to - from);
        for (int i = from; i < to; list.add(i), i++) ;
        return list;
    }

    public static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }

    public static void setSdkInt(int fieldNewValue) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", fieldNewValue);
    }

    public static Pair<String, String> pair(String first, String second) {
        return new Pair<String, String>(first, second);
    }

    public static Map<String, String> mapOf(Pair<String, String>... entries) {
        Map<String, String> map = new HashMap<String, String>();
        for (Pair<String, String> entry : entries) {
            map.put(entry.first, entry.second);
        }
        return map;
    }

    public static StartupState.Builder createDefaultStartupStateBuilder() {
        return new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder().build()
        );
    }

    public static StartupState createDefaultStartupState() {
        return createDefaultStartupStateBuilder().build();
    }
}
