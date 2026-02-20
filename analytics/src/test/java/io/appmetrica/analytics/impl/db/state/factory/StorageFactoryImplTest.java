package io.appmetrica.analytics.impl.db.state.factory;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageFactoryImplTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Mock
    private ProtobufStateStorage<Integer> mainStorage;
    @Mock
    private ProtobufStateStorage<Integer> migrationStorage;
    @Mock
    private IBinaryDataHelper mainHelper;
    @Mock
    private IBinaryDataHelper migrationHelper;

    private StorageFactoryImpl<Integer> storageFactory;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = contextRule.getContext();
        storageFactory = new StorageFactoryImpl<Integer>() {
            @NonNull
            @Override
            protected ProtobufStateStorage<Integer> createWithHelper(@NonNull Context context,
                                                                     @NonNull IBinaryDataHelper helper) {
                if (helper == mainHelper) {
                    return mainStorage;
                } else if (helper == migrationHelper) {
                    return migrationStorage;
                } else {
                    throw new RuntimeException("Unexpected helper");
                }
            }

            @NonNull
            @Override
            protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                return mainHelper;
            }

            @NonNull
            @Override
            protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                return migrationHelper;
            }
        };
    }

    @Test
    public void create() {
        assertThat(storageFactory.create(context)).isSameAs(mainStorage);
    }

    @Test
    public void createForMigration() {
        assertThat(storageFactory.createForMigration(context)).isSameAs(migrationStorage);
    }
}
