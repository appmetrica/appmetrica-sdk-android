package io.appmetrica.analytics.impl.startup;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StartupUnitStartupDidNotOverrideClidsTest extends StartupUnitBaseTest {

    private static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();
    private static final Map<String, String> MAP_WITH_VALID_ITEMS_1 = new HashMap<String, String>();
    private static final Map<String, String> MAP_WITH_VALID_ITEMS_2 = new HashMap<String, String>();
    private static final String MAP_WITH_VALID_ITEMS_1_STRING;
    private static final Map<String, String> MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID = new HashMap<String, String>();

    static {
        MAP_WITH_VALID_ITEMS_1.put("clid0", "0");
        MAP_WITH_VALID_ITEMS_1.put("clid1", "1");
        MAP_WITH_VALID_ITEMS_1_STRING = StartupUtils.encodeClids(MAP_WITH_VALID_ITEMS_1);

        MAP_WITH_VALID_ITEMS_2.put("clid1", "1");
        MAP_WITH_VALID_ITEMS_2.put("clid2", "2");

        MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID.put("clid1", "1");
        MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID.put("clid2", "not_a_number");
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{index}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // #0
                {null, null, null},
                {null, EMPTY_MAP, null},
                {null, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING},
                {null, MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null},
                {EMPTY_MAP, null, null},

                // #5
                {EMPTY_MAP, EMPTY_MAP, null},
                {EMPTY_MAP, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING},
                {EMPTY_MAP, MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null},
                {MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null, null},
                {MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, EMPTY_MAP, null},

                // #10
                {MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING},
                {MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null},
                {MAP_WITH_VALID_ITEMS_1, null, MAP_WITH_VALID_ITEMS_1_STRING},
                {MAP_WITH_VALID_ITEMS_1, EMPTY_MAP, MAP_WITH_VALID_ITEMS_1_STRING},
                {MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING},

                // #15
                {MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_2, MAP_WITH_VALID_ITEMS_1_STRING},
                {MAP_WITH_VALID_ITEMS_1, MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, MAP_WITH_VALID_ITEMS_1_STRING},
        });
    }

    private final HashMap<String, String> chosenForRequestClids;
    @Nullable
    private final Map<String, String> newClidsFromResponse;
    @Nullable
    private final Map<String, String> oldClidsFromResponse;
    @Nullable
    private final String chosenResponseClids;
    @Mock
    private StartupResult parsedResult;
    @Mock
    private CollectingFlags collectingFlags;

    public StartupUnitStartupDidNotOverrideClidsTest(@Nullable Map<String, String> newClidsFromResponse,
                                                     @Nullable Map<String, String> oldClidsFromResponse,
                                                     @Nullable String chosenResponseClids) {
        this.chosenForRequestClids = new HashMap<String, String>();
        chosenForRequestClids.put("clid333", "333");
        this.newClidsFromResponse = newClidsFromResponse;
        this.oldClidsFromResponse = oldClidsFromResponse;
        this.chosenResponseClids = chosenResponseClids;
    }

    @Before
    public void setUp() {
        super.setup();
        when(mStartupRequestConfig.getChosenClids())
                .thenReturn(new ClidsInfo.Candidate(chosenForRequestClids, DistributionSource.APP));
        when(parsedResult.getEncodedClids()).thenReturn(StartupUtils.encodeClids(newClidsFromResponse));
        when(parsedResult.getCollectionFlags()).thenReturn(collectingFlags);
        when(mConfigurationHolder.getStartupState()).thenReturn(TestUtils.createDefaultStartupStateBuilder()
                .withEncodedClidsFromResponse(StartupUtils.encodeClids(oldClidsFromResponse))
                .build());
    }

    @Test
    public void startupDidNotOverrideClids() {
        boolean result = new Random().nextBoolean();
        when(clidsStateChecker.doRequestClidsMatchResponseClids(chosenForRequestClids, chosenResponseClids)).thenReturn(result);
        StartupState startupState = mStartupUnit.parseStartupResult(parsedResult, mStartupRequestConfig, 0L);
        assertThat(startupState.getStartupDidNotOverrideClids()).isEqualTo(result);
        verify(clidsStateChecker).doRequestClidsMatchResponseClids(chosenForRequestClids, chosenResponseClids);
    }
}
