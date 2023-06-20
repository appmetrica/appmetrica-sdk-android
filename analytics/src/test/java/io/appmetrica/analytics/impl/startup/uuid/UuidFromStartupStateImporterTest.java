package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UuidFromStartupStateImporterTest extends CommonTest {

    @Mock
    private Context context;
    @Mock
    private StorageFactory<StartupState> startupStateStorageFactory;
    @Mock
    private ProtobufStateStorage<StartupState> protobufStateStorage;
    @Mock
    private StartupState startupState;

    @Rule
    public MockedStaticRule<StorageFactory.Provider> storageFactoryProviderMockedStaticRule =
        new MockedStaticRule<>(StorageFactory.Provider.class);

    private UuidFromStartupStateImporter uuidFromStartupStateImporter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(StorageFactory.Provider.get(StartupState.class)).thenReturn(startupStateStorageFactory);
        when(startupStateStorageFactory.create(context)).thenReturn(protobufStateStorage);
        when(protobufStateStorage.read()).thenReturn(startupState);

        uuidFromStartupStateImporter = new UuidFromStartupStateImporter();
    }

    @Test
    public void get() {
        String uuid = UUID.randomUUID().toString();

        when(startupState.getUuid()).thenReturn(uuid);

        assertThat(uuidFromStartupStateImporter.get(context)).isEqualTo(uuid);
    }

    @Test
    public void getIfUuidDoesNotExist() {
        when(startupState.getUuid()).thenReturn(null);

        assertThat(uuidFromStartupStateImporter.get(context)).isNull();
    }
}
