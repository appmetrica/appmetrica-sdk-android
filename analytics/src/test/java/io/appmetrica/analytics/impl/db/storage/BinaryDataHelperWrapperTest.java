package io.appmetrica.analytics.impl.db.storage;

import io.appmetrica.analytics.impl.db.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BinaryDataHelperWrapperTest extends CommonTest {

    @Mock
    private IBinaryDataHelper actualHelper;
    private final String key = "some key";
    private BinaryDataHelperWrapper binaryDataHelperWrapper;

    @Rule
    public ServiceMigrationCheckedRule powerMockRule = new ServiceMigrationCheckedRule(true);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        binaryDataHelperWrapper = new BinaryDataHelperWrapper(TestUtils.createMockedContext(), StorageType.SERVICE, actualHelper);
    }

    @Test
    public void insert() {
        byte[] value = "some value".getBytes();
        binaryDataHelperWrapper.insert(key, value);
        verify(actualHelper).insert(key, value);
    }

    @Test
    public void get() {
        byte[] value = "some value".getBytes();
        when(actualHelper.get(key)).thenReturn(value);
        assertThat(binaryDataHelperWrapper.get(key)).isEqualTo(value);
    }

    @Test
    public void remove() {
        binaryDataHelperWrapper.remove(key);
        verify(actualHelper).remove(key);
    }
}
