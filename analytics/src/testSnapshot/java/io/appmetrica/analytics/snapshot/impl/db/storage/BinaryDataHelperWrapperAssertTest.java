package io.appmetrica.analytics.snapshot.impl.db.storage;

import android.content.Context;

import io.appmetrica.analytics.impl.db.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.storage.BinaryDataHelperWrapperTestProxy;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.impl.db.StorageType;
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

@RunWith(RobolectricTestRunner.class)
public class BinaryDataHelperWrapperAssertTest extends CommonTest {

    @Rule
    public final MockedStaticRule<DebugAssert> sDebugAssert = new MockedStaticRule<>(DebugAssert.class);

    @Mock
    private IBinaryDataHelper actualHelper;
    private Context context;
    private BinaryDataHelperWrapperTestProxy dataHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.systemContext;
    }

    @Test
    public void checkOnInsert() {
        dataHelper = new BinaryDataHelperWrapperTestProxy(context, StorageType.SERVICE, actualHelper);
        dataHelper.insert("key", "value".getBytes());
        sDebugAssert.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                DebugAssert.assertMigrated(context, StorageType.SERVICE);
            }
        });
    }

    @Test
    public void checkOnGet() {
        dataHelper = new BinaryDataHelperWrapperTestProxy(context, StorageType.CLIENT, actualHelper);
        dataHelper.get("key");

        sDebugAssert.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                DebugAssert.assertMigrated(context, StorageType.CLIENT);
            }
        });
    }

    @Test
    public void checkOnRemove() {
        dataHelper = new BinaryDataHelperWrapperTestProxy(context, StorageType.SERVICE, actualHelper);
        dataHelper.remove("key");

        sDebugAssert.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                DebugAssert.assertMigrated(context, StorageType.SERVICE);
            }
        });
    }
}
