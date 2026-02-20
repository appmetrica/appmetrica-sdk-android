package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapWithDefaultTests extends CommonTest {

    public static class MapWithDefaultBaseTests extends CommonTest {
        protected Map mContainer;
        protected Object mDefaultValue;

        protected MapWithDefault mMapWithDefault;

        @Before
        public void setUp() throws Exception {
            mContainer = mock(Map.class);
            mDefaultValue = mock(Object.class);
            mMapWithDefault = new MapWithDefault(mContainer, mDefaultValue);
        }
    }

    @RunWith(RobolectricTestRunner.class)
    public static class MapWithDefaultSimpleTests extends MapWithDefaultBaseTests {
        @Test
        public void testCreationByDefaultValue() {
            MapWithDefault mapWithDefault = new MapWithDefault(mDefaultValue);
            assertThat(mapWithDefault.get("Missing key")).isEqualTo(mDefaultValue);
        }

        @Test
        public void testCreationByNullableDefaultValue() {
            MapWithDefault mapWithDefault = new MapWithDefault(null);
            assertThat(mapWithDefault.get("Missing key")).isNull();
        }

        @Test
        public void testContainerMapCreatedByDefault() {
            MapWithDefault mapWithDefault = new MapWithDefault(mDefaultValue);
            assertThat(mapWithDefault.getMap()).isInstanceOf(HashMap.class);
        }

        @Test(expected = NullPointerException.class)
        public void testCreationByNullableContainerMap() {
            MapWithDefault mapWithDefault = new MapWithDefault(null, mDefaultValue);
            mapWithDefault.get("MissingKey");
        }

        @Test
        public void testReturnValueFromContainerIfExists() {
            Object expectedValue = mock(Object.class);
            when(mContainer.get(any(Object.class))).thenReturn(expectedValue);
            assertThat(mMapWithDefault.get(mock(Object.class))).isEqualTo(expectedValue);
        }

        @Test
        public void testReturnDefaultValueIfNotExistsInContainer() {
            when(mContainer.get(anyString())).thenReturn(null);
            assertThat(mMapWithDefault.get(mock(Object.class))).isEqualTo(mDefaultValue);
        }

        @Test
        public void testKeySet() {
            final Set set = mock(Set.class);
            when(mContainer.keySet()).thenReturn(set);
            assertThat(mMapWithDefault.keySet()).isEqualTo(set);
        }
    }

    @RunWith(Parameterized.class)
    public static class MapWithDefaultPutTests extends MapWithDefaultBaseTests {

        private final Object mKey;
        private final Object mValue;

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {null, null},
                {null, new Object()},
                {new Object(), null},
                {new Object(), new Object()}
            });
        }

        public MapWithDefaultPutTests(final Object key, final Object value) {
            mKey = key;
            mValue = value;
        }

        @Test
        public void dispatchKeyValueToContainer() {
            mMapWithDefault.put(mKey, mValue);
            verify(mContainer, times(1)).put(mKey, mValue);
        }
    }

    @RunWith(Parameterized.class)
    public static class MapWithDefaultGetTests extends MapWithDefaultBaseTests {

        private final Object mKey;

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {null}, {"ewffsdfd"}
            });
        }

        public MapWithDefaultGetTests(final Object key) {
            mKey = key;
        }

        @Test
        public void testGetValueFromStorage() {
            mMapWithDefault.get(mKey);
            verify(mContainer, times(1)).get(mKey);
        }
    }
}
