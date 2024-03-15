package io.appmetrica.analytics.impl.db.storage;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@RunWith(RobolectricTestRunner.class)
public class BinaryDataHelperWrapperAssertTest extends CommonTest {

    @Mock
    private IBinaryDataHelper actualHelper;
    private Context context;
    private BinaryDataHelperWrapper dataHelper;

    @Rule
    public final MockedStaticRule<DebugAssert> sDebugAssert = new MockedStaticRule<>(DebugAssert.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.systemContext;
    }

    @Test
    public void doNotCheckOnInsert() {
        dataHelper = new BinaryDataHelperWrapper(context, StorageType.SERVICE, actualHelper);
        dataHelper.insert("key", "value".getBytes());
        sDebugAssert.getStaticMock().verify(never(), new MockedStatic.Verification() {
            @Override
            public void apply() {
                DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
            }
        });
    }

    @Test
    public void doNotCheckOnGet() {
        dataHelper = new BinaryDataHelperWrapper(context, StorageType.CLIENT, actualHelper);
        dataHelper.get("key");
        sDebugAssert.getStaticMock().verify(never(), new MockedStatic.Verification() {
            @Override
            public void apply() {
                DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
            }
        });
    }

    @Test
    public void doNotCheckOnRemove() {
        dataHelper = new BinaryDataHelperWrapper(context, StorageType.SERVICE, actualHelper);
        dataHelper.remove("key");
        sDebugAssert.getStaticMock().verify(never(), new MockedStatic.Verification() {
            @Override
            public void apply() {
                DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
            }
        });
    }
}
