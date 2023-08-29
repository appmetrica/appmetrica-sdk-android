package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class UuidFromClientPreferencesImporterTest extends CommonTest {

    @Mock
    private Context context;
    private IdentifiersResult preferencesUuid;
    private String expectedValue;

    private static final String FIRST_UUID = UUID.randomUUID().toString();

    public UuidFromClientPreferencesImporterTest(IdentifiersResult preferencesUuid,
                                                 String expectedValue,
                                                 String description) {
        this.preferencesUuid = preferencesUuid;
        this.expectedValue = expectedValue;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                new IdentifiersResult(FIRST_UUID, IdentifierStatus.OK, null),
                FIRST_UUID, "same uuid"
            },
            {
                new IdentifiersResult(FIRST_UUID, IdentifierStatus.UNKNOWN, null),
                FIRST_UUID, "different uuid in sources if status is unknown"
            },
            {
                new IdentifiersResult(FIRST_UUID, IdentifierStatus.UNKNOWN, "Some error"),
                FIRST_UUID, "different uuid in sources if status error is not null"
            },
            {
                new IdentifiersResult(null, IdentifierStatus.OK, null),
                null, "actual preferences value is null"
            },
            {
                new IdentifiersResult("", IdentifierStatus.OK, null),
                null, "actual preferences value is empty"
            },
        });
    }

    @Mock
    private PreferencesClientDbStorage preferencesClientDbStorage;
    @Mock
    private DatabaseStorageFactory databaseStorageFactory;
    @Mock
    private IKeyValueTableDbHelper clientDbHelper;

    @Rule
    public MockedStaticRule<DatabaseStorageFactory> databaseStorageFactoryMockedStaticRule =
        new MockedStaticRule<>(DatabaseStorageFactory.class);

    @Rule
    public MockedConstructionRule<PreferencesClientDbStorage> preferencesClientDbStorageMockedConstructionRule =
        new MockedConstructionRule<>(
            PreferencesClientDbStorage.class,
            new MockedConstruction.MockInitializer<PreferencesClientDbStorage>() {
                @Override
                public void prepare(PreferencesClientDbStorage mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getUuidResult()).thenReturn(preferencesUuid);
                }
            }
        );

    private UuidFromClientPreferencesImporter uuidFromClientPreferencesImporter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(context.getApplicationContext()).thenReturn(context);
        when(DatabaseStorageFactory.getInstance(context)).thenReturn(databaseStorageFactory);
        when(databaseStorageFactory.getClientDbHelper()).thenReturn(clientDbHelper);
        when(preferencesClientDbStorage.getUuidResult()).thenReturn(preferencesUuid);

        uuidFromClientPreferencesImporter = new UuidFromClientPreferencesImporter();
    }

    @Test
    public void get() {
        assertThat(uuidFromClientPreferencesImporter.get(context)).isEqualTo(expectedValue);

        assertThat(preferencesClientDbStorageMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(preferencesClientDbStorageMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(clientDbHelper);
    }
}
