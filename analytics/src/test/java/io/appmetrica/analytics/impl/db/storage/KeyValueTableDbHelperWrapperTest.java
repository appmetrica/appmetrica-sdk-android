package io.appmetrica.analytics.impl.db.storage;

import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeyValueTableDbHelperWrapperTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Mock
    private IKeyValueTableDbHelper actualHelper;
    private final String key = "some key";
    private KeyValueTableDbHelperWrapper wrapper;

    @Rule
    public ServiceMigrationCheckedRule serviceMigrationCheckedRule = new ServiceMigrationCheckedRule(true);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        wrapper = new KeyValueTableDbHelperWrapper(contextRule.getContext(), StorageType.SERVICE, actualHelper);
    }

    @Test
    public void commit() {
        wrapper.commit();
        verify(actualHelper).commit();
    }

    @Test
    public void containsKey() {
        when(actualHelper.containsKey(key)).thenReturn(true);
        assertThat(wrapper.containsKey(key)).isTrue();
    }

    @Test
    public void remove() {
        assertThat(wrapper.remove(key)).isSameAs(wrapper);
        verify(actualHelper).remove(key);
    }

    @Test
    public void getString() {
        String result = "result";
        String defaultValue = "aaa";
        when(actualHelper.getString(key, defaultValue)).thenReturn(result);
        assertThat(wrapper.getString(key, defaultValue)).isEqualTo(result);
    }

    @Test
    public void putString() {
        String value = "aaa";
        assertThat(wrapper.put(key, value)).isSameAs(wrapper);
        verify(actualHelper).put(key, value);
    }

    @Test
    public void getInt() {
        int result = 55;
        int defaultValue = 33;
        when(actualHelper.getInt(key, defaultValue)).thenReturn(result);
        assertThat(wrapper.getInt(key, defaultValue)).isEqualTo(result);
    }

    @Test
    public void putInt() {
        int value = 44;
        assertThat(wrapper.put(key, value)).isSameAs(wrapper);
        verify(actualHelper).put(key, value);
    }

    @Test
    public void getLong() {
        long result = 55L;
        long defaultValue = 33L;
        when(actualHelper.getLong(key, defaultValue)).thenReturn(result);
        assertThat(wrapper.getLong(key, defaultValue)).isEqualTo(result);
    }

    @Test
    public void putLong() {
        long value = 44L;
        assertThat(wrapper.put(key, value)).isSameAs(wrapper);
        verify(actualHelper).put(key, value);
    }

    @Test
    public void getFloat() {
        float result = 55.5f;
        float defaultValue = 33.3f;
        when(actualHelper.getFloat(key, defaultValue)).thenReturn(result);
        assertThat(wrapper.getFloat(key, defaultValue)).isEqualTo(result);
    }

    @Test
    public void putFloat() {
        float value = 44.4f;
        assertThat(wrapper.put(key, value)).isSameAs(wrapper);
        verify(actualHelper).put(key, value);
    }

    @Test
    public void getBoolean() {
        boolean result = true;
        boolean defaultValue = false;
        when(actualHelper.getBoolean(key, defaultValue)).thenReturn(result);
        assertThat(wrapper.getBoolean(key, defaultValue)).isEqualTo(result);
    }

    @Test
    public void putBoolean() {
        boolean value = true;
        assertThat(wrapper.put(key, value)).isSameAs(wrapper);
        verify(actualHelper).put(key, value);
    }

    @Test
    public void keys() {
        Set<String> keys = new HashSet<>();
        keys.add("key1");
        keys.add("key2");
        when(actualHelper.keys()).thenReturn(keys);
        assertThat(wrapper.keys()).isEqualTo(keys);
    }
}
