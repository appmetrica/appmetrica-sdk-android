package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UuidFromClientPreferencesImporterTest extends CommonTest {

    @Mock
    private Context context;
    private final IdentifiersResult preferencesUuid;
    private final String expectedValue;

    private static final String FIRST_UUID = UUID.randomUUID().toString();

    public UuidFromClientPreferencesImporterTest(IdentifiersResult preferencesUuid,
                                                 String expectedValue,
                                                 String description) {
        this.preferencesUuid = preferencesUuid;
        this.expectedValue = expectedValue;
    }

    @Parameters(name = "{2}")
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

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    private UuidFromClientPreferencesImporter uuidFromClientPreferencesImporter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(context.getApplicationContext()).thenReturn(context);
        when(ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context).getUuidResult())
            .thenReturn(preferencesUuid);

        uuidFromClientPreferencesImporter = new UuidFromClientPreferencesImporter();
    }

    @Test
    public void get() {
        assertThat(uuidFromClientPreferencesImporter.get(context)).isEqualTo(expectedValue);
    }
}
