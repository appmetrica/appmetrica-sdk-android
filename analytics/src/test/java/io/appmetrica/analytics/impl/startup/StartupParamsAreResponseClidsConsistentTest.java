package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StartupParamsAreResponseClidsConsistentTest extends CommonTest {

    private IdentifiersResult mInputResponseClids;
    private String mInputClientClids;
    private boolean mExpectedResult;

    public StartupParamsAreResponseClidsConsistentTest(Map<String, String> inputResponseClids,
                                                       Map<String, String> inputClientClids,
                                                       boolean expectedResult) {
        mInputResponseClids = new IdentifiersResult(JsonHelper.clidsToString(inputResponseClids), IdentifierStatus.OK, null);
        mInputClientClids = StartupUtils.encodeClids(inputClientClids);
        mExpectedResult = expectedResult;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}]Return {2} for responseClids = \"{0}\" and " +
            "clientClids = \"{1}\"")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, null, true},
                {null, Collections.emptyMap(), true},
                {null, StartupParamsTestUtils.CLIDS_MAP_2, true},
                {Collections.emptyMap(), null, true},
                {Collections.emptyMap(), Collections.emptyMap(), true},
                {Collections.emptyMap(), StartupParamsTestUtils.CLIDS_MAP_2, false},
                {StartupParamsTestUtils.CLIDS_MAP_1, null, true},
                {StartupParamsTestUtils.CLIDS_MAP_1, Collections.emptyMap(), true},
                {StartupParamsTestUtils.CLIDS_MAP_1, StartupParamsTestUtils.CLIDS_MAP_2, true}
        });
    }

    private Context context;
    @Mock
    private PreferencesClientDbStorage mStorage;

    private StartupParams mStartupParams;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mStorage);

        when(mStorage.getClientClids(nullable(String.class))).thenReturn(mInputClientClids);
        when(mStorage.getResponseClidsResult()).thenReturn(mInputResponseClids);
        when(mStorage.getCustomSdkHosts()).thenReturn(new IdentifiersResult(null, IdentifierStatus.UNKNOWN, null));
        when(mStorage.getFeatures()).thenReturn(new FeaturesInternal(null, IdentifierStatus.UNKNOWN, null));

        mStartupParams = new StartupParams(context, mStorage);
    }

    @Test
    public void testAreResponseClidsConsistent() {
        assertThat(mStartupParams.areResponseClidsConsistent()).isEqualTo(mExpectedResult);
    }
}
