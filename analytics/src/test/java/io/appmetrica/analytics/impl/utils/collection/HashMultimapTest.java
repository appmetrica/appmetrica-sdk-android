package io.appmetrica.analytics.impl.utils.collection;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class HashMultimapTest extends CommonTest {

    HashMultimap<Integer, Integer> mMap = new HashMultimap<Integer, Integer>();

    @Test
    public void testPut() {
        assertThat(mMap.get(1)).isNull();
        assertThat(mMap.put(1, 1)).isNull();
        assertThat(mMap.put(1, 2)).hasSize(1);
    }

    @Test
    public void testContainsValue() {
        mMap.put(1, 1);
        assertThat(mMap.containsValue(1)).isTrue();
        assertThat(mMap.containsValue(2)).isFalse();
    }

    @Test
    public void testContainsKey() {
        mMap.put(1, 1);
        assertThat(mMap.containsKey(1)).isTrue();
        assertThat(mMap.containsKey(2)).isFalse();
    }

    @Test
    public void testIsEmpty() {
        assertThat(mMap.isEmpty()).isTrue();
        mMap.put(1, 1);
        assertThat(mMap.isEmpty()).isFalse();
    }

    @Test
    public void testSize() {
        assertThat(mMap.size()).isZero();
        mMap.put(1, 1);
        mMap.put(1, 2);
        assertThat(mMap.size()).isEqualTo(1);
        mMap.put(2, 1);
        assertThat(mMap.size()).isEqualTo(2);
    }

    @Test
    public void testClear() {
        mMap.put(1, 1);
        assertThat(mMap.size()).isGreaterThan(0);
        mMap.clear();
        assertThat(mMap.get(1)).isNull();
        assertThat(mMap.size()).isZero();
    }

    @Test
    public void testKeySet() {
        mMap.put(1, 2);
        mMap.put(2, 1);
        mMap.put(43, 2);
        assertThat(mMap.keySet()).containsOnly(43, 2, 1);
    }

    @Test
    public void testValues() {
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.put(1, 3);
        mMap.put(2, 1);
        mMap.put(2, 2);
        mMap.put(2, 3);
        Collection<? extends Collection<Integer>> values = mMap.values();
        assertThat(values).hasSize(2);
        assertThat(values).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1, 1, 2, 2, 3, 3);
    }

    @Test
    public void testRemove() {
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.put(1, 3);
        mMap.put(2, 1);
        mMap.put(2, 2);
        mMap.put(2, 3);
        mMap.put(2, 4);
        Collection<? extends Collection<Integer>> values = mMap.values();
        assertThat(values).hasSize(2);
        assertThat(values).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1, 1, 2, 2, 3, 3, 4);
        mMap.remove(2, 4);
        assertThat(mMap.values()).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1, 1, 2, 2, 3, 3);
    }

    @Test
    public void removeAllOneByOneShouldNotRemoveEmptyCollection() {
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.remove(1, 1);
        mMap.remove(1, 2);
        assertThat(mMap.size()).isEqualTo(1);
        assertThat(mMap.isEmpty()).isFalse();
    }

    @Test
    public void removeAllOneByOneShouldRemoveEmptyCollection() {
        mMap = new HashMultimap<Integer, Integer>(true);
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.remove(1, 1);
        mMap.remove(1, 2);
        assertThat(mMap.size()).isZero();
        assertThat(mMap.isEmpty()).isTrue();
    }

    @Test
    public void testRemoveNotExisted() {
        mMap.put(1, 1);
        assertThat(mMap.values()).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1);
        assertThat(mMap.remove(2, 1)).isNull();
        assertThat(mMap.values()).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1);
    }

    @Test
    public void testRemoveAll() {
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.put(1, 3);
        mMap.put(2, 1);
        mMap.put(2, 2);
        mMap.put(2, 3);
        mMap.put(2, 4);
        Collection<? extends Collection<Integer>> values = mMap.values();
        assertThat(values).hasSize(2);
        assertThat(values).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1, 1, 2, 2, 3, 3, 4);
        Collection<Integer> removed = mMap.removeAll(2);
        assertThat(removed).containsOnly(1, 2, 3, 4);
    }

    @Test
    public void testRemoveAllForNotExistedKey() {
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.put(1, 3);
        mMap.put(2, 1);
        mMap.put(2, 2);
        mMap.put(2, 3);
        mMap.put(2, 4);
        Collection<? extends Collection<Integer>> values = mMap.values();
        assertThat(values).hasSize(2);
        assertThat(values).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1, 1, 2, 2, 3, 3, 4);
        assertThat(mMap.removeAll(3)).isNull();
    }

    @Test
    public void testEntrySet() {
        mMap.put(1, 1);
        mMap.put(1, 2);
        mMap.put(1, 3);
        mMap.put(2, 1);
        mMap.put(2, 2);
        mMap.put(2, 3);
        mMap.put(2, 4);
        final Map<Integer, List<Integer>> expected = new HashMap<>();
        expected.put(1, Arrays.asList(1, 2, 3));
        expected.put(2, Arrays.asList(1, 2, 3, 4));
        assertThat(mMap.entrySet()).extracting(item -> Tuple.tuple(item.getKey(), item.getValue()))
            .containsExactlyInAnyOrder(
                Tuple.tuple(1, Arrays.asList(1, 2, 3)),
                Tuple.tuple(2, Arrays.asList(1, 2, 3, 4))
            );
    }

    @Test
    public void testPutAll() {
        mMap.put(1, 1);
        assertThat(mMap.values()).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1);
        mMap.putAll(new HashMap<Integer, Integer>() {
            {
                put(1, 2);
                put(2, 2);
            }
        });
        assertThat(mMap.values()).flatExtracting(new ListOfListsToListExctractor<Integer>()).containsOnly(1, 1, 2);
    }

    @Test
    public void toStringForEmpty() {
        assertThat(mMap.toString()).isEqualTo("{}");
    }

    @Test
    public void toStringForSinglePair() {
        mMap.put(1, 10);
        assertThat(mMap.toString()).isEqualTo("{1=[10]}");
    }

    @Test
    public void toStringForMultipleValuesForKey() {
        mMap.put(1, 10);
        mMap.put(1, 20);
        assertThat(mMap.toString()).isEqualTo("{1=[10, 20]}");
    }

    private static class ListOfListsToListExctractor<T> implements Function<Collection<T>, Collection<T>> {
        public Collection<T> apply(Collection<T> input) {
            return input;
        }
    }
}
