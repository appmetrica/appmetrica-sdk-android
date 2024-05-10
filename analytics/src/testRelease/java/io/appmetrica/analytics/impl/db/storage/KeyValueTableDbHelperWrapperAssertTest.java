package io.appmetrica.analytics.impl.db.storage;

import android.content.Context;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
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
public class KeyValueTableDbHelperWrapperAssertTest extends CommonTest {

    @Rule
    public final MockedStaticRule<DebugAssert> sDebugAssert = new MockedStaticRule<>(DebugAssert.class);

    @Mock
    private IKeyValueTableDbHelper actualHelper;
    private Context context;
    private KeyValueTableDbHelperWrapper helper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.systemContext;
    }

    @Test
    public void doNotCheckOnGetInt() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.SERVICE, actualHelper);
        helper.getInt("key", 0);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnGetLong() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.getLong("key", 0);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnGetBoolean() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.getBoolean("key", false);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnGetFloat() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.SERVICE, actualHelper);
        helper.getFloat("key", 0);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnGetString() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.getString("key", "");
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnPutInt() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.SERVICE, actualHelper);
        helper.put("key", 0);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnPutLong() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.put("key", 0L);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnPutBoolean() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.SERVICE, actualHelper);
        helper.put("key", true);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnPutFloat() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.SERVICE, actualHelper);
        helper.put("key", 0f);
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnPutString() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.put("key", "value");
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnRemove() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.SERVICE, actualHelper);
        helper.remove("key");
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnContainsKey() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.containsKey("key");
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }

    @Test
    public void doNotCheckOnCommit() {
        helper = new KeyValueTableDbHelperWrapper(context, StorageType.CLIENT, actualHelper);
        helper.commit();
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertMigrated(any(Context.class), any(StorageType.class));
                }
            },
            never()
        );
    }
}
