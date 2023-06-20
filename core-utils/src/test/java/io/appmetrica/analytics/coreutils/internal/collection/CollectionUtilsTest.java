package io.appmetrica.analytics.coreutils.internal.collection;

import android.util.ArrayMap;
import android.util.Pair;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CollectionUtilsTest extends CommonTest {

    @Test
    public void testCompareTrue() {
        Pair<List<String>, List<String>> lists = generateTwoIdenticalLists();
        List<String> list2 = lists.second;
        String temp = list2.get(1);
        temp = list2.set(7, temp);
        list2.set(1, temp);
        assertThat(CollectionUtils.areCollectionsEqual(lists.first, list2)).isTrue();
    }

    @Test
    public void testComparePermissionChanged() {
        Pair<List<String>, List<String>> lists = generateTwoIdenticalLists();
        lists.first.set(0, "00");
        assertThat(CollectionUtils.areCollectionsEqual(lists.first, lists.second)).isFalse();
    }

    @Test
    public void testCompareDifferentLists() {
        Pair<List<String>, List<String>> lists = generateTwoIdenticalLists();
        lists.first.remove(2);
        assertThat(CollectionUtils.areCollectionsEqual(lists.first, lists.second)).isFalse();
    }

    @Test
    public void testAllListNull() {
        assertThat(CollectionUtils.areCollectionsEqual(null, null)).isTrue();
    }

    @Test
    public void testOnlyLeftIsNull() {
        assertThat(CollectionUtils.areCollectionsEqual(null, Arrays.asList("0"))).isFalse();
    }

    @Test
    public void testOnlyRightIsNull() {
        assertThat(CollectionUtils.areCollectionsEqual(Arrays.asList("0"), null)).isFalse();
    }

    @Test
    public void testWithLeftHashSet() {
        Pair<List<String>, List<String>> lists = generateTwoIdenticalLists();
        HashSet set = new HashSet(lists.second);
        assertThat(CollectionUtils.areCollectionsEqual(set, lists.second)).isTrue();
    }

    @Test
    public void testWithRightHashSet() {
        Pair<List<String>, List<String>> lists = generateTwoIdenticalLists();
        HashSet set = new HashSet(lists.second);
        assertThat(CollectionUtils.areCollectionsEqual(lists.first, set)).isTrue();
    }

    @Test
    public void testPutOptNullKey() {
        Map<String, String> map = new HashMap<String, String>();
        CollectionUtils.putOpt(map, null, "value");
        assertThat(map).isEmpty();
    }

    @Test
    public void testPutOptNullValue() {
        Map<String, String> map = new HashMap<String, String>();
        CollectionUtils.putOpt(map, "key", null);
        assertThat(map).isEmpty();
    }

    @Test
    public void testPutOptBothNotNull() {
        Map<String, String> map = new HashMap<String, String>();
        CollectionUtils.putOpt(map, "key", "value");
        assertThat(map).contains(new AbstractMap.SimpleEntry<String, String>("key", "value"));
    }

    @Test
    public void testGetFromMapIgnoreCase() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("Content-Encoding", "encrypted");
            put("etag", "1337");
            put("Authorization", "Basic eWFuZGV4OnlhbmRleA");
        }};

        assertThat(CollectionUtils.getFromMapIgnoreCase(map, "content-encoding")).isEqualTo("encrypted");
        assertThat(CollectionUtils.getFromMapIgnoreCase(map, "ETag")).isEqualTo("1337");
        assertThat(CollectionUtils.getFromMapIgnoreCase(map, "AuThOrIzAtIoN")).isEqualTo("Basic eWFuZGV4OnlhbmRleA");
    }

    @Test
    public void testConvertMapKeysToLowerCase() {
        Map<String, String> map = new HashMap<String, String>() {{
            put(null, "test");
            put("Content-Encoding", "encrypted");
            put("etag", "1337");
            put("Authorization", "Basic eWFuZGV4OnlhbmRleA");
        }};

        Map<String, String> convertedMap = CollectionUtils.convertMapKeysToLowerCase(map);

        assertThat(convertedMap.size()).isEqualTo(map.size());

        for (Map.Entry<String, String> entry : map.entrySet()) {
            final String entryKey = entry.getKey();
            if (entryKey != null) {
                assertThat(convertedMap).containsKey(entryKey.toLowerCase());
            }
        }
    }

    @Test
    public void testGetOrDefaultReturnValueIfExists() {
        Map<Object, Object> map = new HashMap<>();
        Object key = new Object();
        Object value = new Object();
        Object defValue = new Object();
        map.put(key, value);
        assertThat(CollectionUtils.getOrDefault(map, key, defValue)).isEqualTo(value).isNotEqualTo(defValue);
    }

    @Test
    public void testGetOrDefaultReturnDefValueIfNotExists() {
        Map<Object, Object> map = new HashMap<>();
        Object key = new Object();
        Object value = new Object();
        Object defValue = new Object();
        assertThat(CollectionUtils.getOrDefault(map, key, defValue)).isNotEqualTo(value).isEqualTo(defValue);
    }

    @Test
    public void testGetOrDefaultReturnDefValueIfExistingValueIsNull() {
        Map<Object, Object> map = new HashMap<>();
        Object key = new Object();
        Object defValue = new Object();
        map.put(key, null);
        assertThat(CollectionUtils.getOrDefault(map, key, defValue)).isEqualTo(defValue);
    }

    @Test
    public void testCopyOfNull() {
        assertThat(CollectionUtils.copyOf(null)).isNull();
    }

    @Test
    public void testCopyOfEmptyMap() {
        assertThat(CollectionUtils.copyOf(new HashMap<String, String>())).isNull();
    }

    @Test
    public void testCopyOf() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        assertThat(CollectionUtils.copyOf(map)).isEqualTo(map);
    }

    @Test
    public void unmodifiableListCopyDoesNotNoticeChangesToOriginalCollection() {
        final List<String> original = new ArrayList<>();
        original.add("a");
        original.add("b");
        final List<String> listCopy = CollectionUtils.unmodifiableListCopy(original);
        assertThat(listCopy).containsExactly("a", "b");
        original.add("c");
        assertThat(listCopy).containsExactly("a", "b");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableListCopyDoesAllowModifications() {
        final List<String> original = new ArrayList<>();
        original.add("a");
        original.add("b");
        final List<String> listCopy = CollectionUtils.unmodifiableListCopy(original);
        listCopy.add("c");
    }

    @Test
    public void unmodifiableMapCopyDoesNotNoticeChangesToOriginalCollection() {
        final Map<String, String> original = new HashMap<>();
        final Map<String, String> originalCopy = new HashMap<>();
        original.put("key1", "value1");
        originalCopy.put("key1", "value1");
        original.put("key2", "value2");
        originalCopy.put("key2", "value2");
        final Map<String, String> mapCopy = CollectionUtils.unmodifiableMapCopy(original);
        assertThat(mapCopy).containsExactlyInAnyOrderEntriesOf(originalCopy);
        original.put("key3", "value3");
        assertThat(mapCopy).containsExactlyInAnyOrderEntriesOf(originalCopy);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableMapCopyDoesAllowModifications() {
        final Map<String, String> original = new HashMap<>();
        original.put("key1", "value1");
        original.put("key2", "value2");
        final Map<String, String> mapCopy = CollectionUtils.unmodifiableMapCopy(original);
        mapCopy.put("key3", "value3");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableSameOrderMapCopyDoesAllowModifications() {
        final LinkedHashMap<String, String> original = new LinkedHashMap<>();
        original.put("key1", "value1");
        original.put("key2", "value2");
        final Map<String, String> mapCopy = CollectionUtils.unmodifiableMapCopy(original);
        mapCopy.put("key3", "value3");
    }

    @Test
    public void unmodifiableSameOrderMapCopyDoesNotNoticeChangesToOriginalCollection() {
        final LinkedHashMap<String, String> original = new LinkedHashMap<>();
        final LinkedHashMap<String, String> originalCopy = new LinkedHashMap<>();
        original.put("key1", "value1");
        originalCopy.put("key1", "value1");
        original.put("key2", "value2");
        originalCopy.put("key2", "value2");
        final Map<String, String> mapCopy = CollectionUtils.unmodifiableSameOrderMapCopy(original);
        assertThat(mapCopy).containsExactlyEntriesOf(originalCopy);
        original.put("key3", "value3");
        assertThat(mapCopy).containsExactlyEntriesOf(originalCopy);
    }

    @Test
    public void testSetOf() {
        assertThat(CollectionUtils.unmodifiableSetOf("value1", "value2")).containsExactlyInAnyOrder("value1", "value2");
    }

    @Test
    public void testSetOfEmpty() {
        assertThat(CollectionUtils.unmodifiableSetOf()).isEmpty();
    }

    @Test(expected = Exception.class)
    public void testSetIsUnmodifiable() {
        CollectionUtils.unmodifiableSetOf("value").add("another value");
    }

    @Test
    public void testColumnListIsSorted() {
        assertThat(CollectionUtils.createSortedListWithoutRepetitions("C", "H", "A", "Z", "D")).containsExactly("A", "C", "D", "H", "Z");
    }

    @Test
    public void testColumnListContainsDistinctValues() {
        assertThat(CollectionUtils.createSortedListWithoutRepetitions("C", "H", "A", "Z", "H", "D")).containsExactly("A", "C", "D", "H", "Z");
    }

    @Test
    public void testGetListFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertThat(CollectionUtils.getListFromMap(map)).hasSize(2).containsExactlyInAnyOrder(
            new AbstractMap.SimpleEntry<String, Object>("k1", "v1"),
            new AbstractMap.SimpleEntry<String, Object>("k2", "v2")
        );
    }

    @Test
    public void testGetListFromArrayMap() {
        Map<String, Object> map = new ArrayMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertThat(CollectionUtils.getListFromMap(map)).hasSize(2).containsExactlyInAnyOrder(
            new AbstractMap.SimpleEntry<String, Object>("k1", "v1"),
            new AbstractMap.SimpleEntry<String, Object>("k2", "v2")
        );
    }

    @Test
    public void testGetMapFromList() {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleEntry<String, Object>("k1", "v1"));
        list.add(new AbstractMap.SimpleEntry<String, Object>("k2", 2));
        assertThat(CollectionUtils.getMapFromList(list)).containsOnly(
            new AbstractMap.SimpleEntry<String, Object>("k1", "v1"),
            new AbstractMap.SimpleEntry<String, Object>("k2", 2)
        );
    }

    @Test
    public void testListCopyOfNullableCollection() {
        Set<Integer> set = new HashSet<Integer>() {{
            add(4);
            add(2);
            add(7);
        }};
        List<Integer> copy = CollectionUtils.arrayListCopyOfNullableCollection(set);
        assertThat(copy.size()).isEqualTo(set.size());
        assertThat(copy.containsAll(set)).isTrue();
        assertThat(CollectionUtils.arrayListCopyOfNullableCollection(null)).isNull();
    }

    @Test
    public void testMapCopyOfNullableMap() {
        Map<String, Integer> map = new HashMap<String, Integer>() {{
            put("t1", 37);
            put("t2", 1337);
            put("t3", 42);
        }};
        Map<String, Integer> copy = CollectionUtils.mapCopyOfNullableMap(map);
        assertThat(copy.size()).isEqualTo(map.size());
        assertThat(map.entrySet().containsAll(copy.entrySet())).isTrue();
        assertThat(CollectionUtils.mapCopyOfNullableMap(null)).isNull();
    }

    private Pair generateTwoIdenticalLists() {
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            String state = UUID.randomUUID().toString();
            list1.add(state);
            list2.add(state);
        }
        return Pair.create(list1, list2);
    }

}
