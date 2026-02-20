package io.appmetrica.analytics.impl.startup;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class ClidsStateCheckerDoRequestClidsMatchTest extends CommonTest {

    private static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();
    private static final Map<String, String> MAP1 = new HashMap<String, String>();
    private static final Map<String, String> MAP2 = new HashMap<String, String>();
    private static final Map<String, String> MAP3 = new HashMap<String, String>();
    private static final Map<String, String> MAP4 = new HashMap<String, String>();

    static {
        MAP1.put("clid0", "0");
        MAP1.put("clid1", "1");

        MAP2.put("clid1", "1");
        MAP2.put("clid2", "2");

        MAP3.put("clid2", "2");
        MAP3.put("clid3", "3");

        MAP4.put("clid3", "3");
        MAP4.put("clid4", "4");
    }

    @Parameters(name = "{index}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // #0
            {null, null, null, null, true},
            {null, EMPTY_MAP, null, EMPTY_MAP, true},
            {null, null, EMPTY_MAP, EMPTY_MAP, true},
            {EMPTY_MAP, EMPTY_MAP, null, null, true},
            {EMPTY_MAP, EMPTY_MAP, EMPTY_MAP, EMPTY_MAP, true},

            // #5
            {null, null, EMPTY_MAP, MAP1, true},
            {null, null, MAP1, EMPTY_MAP, true},
            {null, null, MAP1, MAP1, true},
            {null, null, MAP1, MAP2, true},
            {null, EMPTY_MAP, EMPTY_MAP, MAP1, true},

            // #10
            {null, EMPTY_MAP, MAP1, EMPTY_MAP, true},
            {null, EMPTY_MAP, MAP1, MAP1, true},
            {null, EMPTY_MAP, MAP1, MAP2, true},
            {null, MAP1, null, MAP1, true},
            {null, MAP1, EMPTY_MAP, EMPTY_MAP, false},

            // #15
            {null, MAP1, EMPTY_MAP, MAP1, true},
            {null, MAP1, EMPTY_MAP, MAP2, false},
            {null, MAP1, MAP1, EMPTY_MAP, false},
            {null, MAP1, MAP1, MAP1, true},
            {null, MAP1, MAP1, MAP2, false},

            // #20
            {null, MAP1, MAP2, EMPTY_MAP, false},
            {null, MAP1, MAP2, MAP1, true},
            {null, MAP1, MAP2, MAP2, false},
            {null, MAP1, MAP2, MAP3, false},
            {EMPTY_MAP, EMPTY_MAP, null, MAP1, true},

            // #25
            {EMPTY_MAP, EMPTY_MAP, MAP1, EMPTY_MAP, true},
            {EMPTY_MAP, EMPTY_MAP, MAP1, MAP1, true},
            {EMPTY_MAP, EMPTY_MAP, MAP1, MAP2, true},
            {EMPTY_MAP, MAP1, null, null, false},
            {EMPTY_MAP, MAP1, null, EMPTY_MAP, false},

            // #30
            {EMPTY_MAP, MAP1, null, MAP1, true},
            {EMPTY_MAP, MAP1, null, MAP2, false},
            {EMPTY_MAP, MAP1, EMPTY_MAP, MAP1, true},
            {EMPTY_MAP, MAP1, MAP1, EMPTY_MAP, false},
            {EMPTY_MAP, MAP1, MAP1, MAP1, true},

            // #35
            {EMPTY_MAP, MAP1, MAP1, MAP2, false},
            {EMPTY_MAP, MAP1, MAP2, EMPTY_MAP, false},
            {EMPTY_MAP, MAP1, MAP2, MAP1, true},
            {EMPTY_MAP, MAP1, MAP2, MAP2, false},
            {EMPTY_MAP, MAP1, MAP2, MAP3, false},

            // #40
            {MAP1, EMPTY_MAP, null, null, true},
            {MAP1, EMPTY_MAP, null, EMPTY_MAP, true},
            {MAP1, EMPTY_MAP, null, MAP1, true},
            {MAP1, EMPTY_MAP, null, MAP2, true},
            {MAP1, EMPTY_MAP, EMPTY_MAP, EMPTY_MAP, true},

            // #45
            {MAP1, EMPTY_MAP, EMPTY_MAP, MAP1, true},
            {MAP1, EMPTY_MAP, EMPTY_MAP, MAP2, true},
            {MAP1, EMPTY_MAP, MAP1, EMPTY_MAP, true},
            {MAP1, EMPTY_MAP, MAP2, EMPTY_MAP, true},
            {MAP1, EMPTY_MAP, MAP2, MAP1, true},

            // #50
            {MAP1, EMPTY_MAP, MAP2, MAP2, true},
            {MAP1, EMPTY_MAP, MAP2, MAP3, true},
            {MAP1, MAP1, null, null, false},
            {MAP1, MAP1, null, EMPTY_MAP, false},
            {MAP1, MAP1, null, MAP1, true},

            // #55
            {MAP1, MAP1, EMPTY_MAP, EMPTY_MAP, false},
            {MAP1, MAP1, EMPTY_MAP, MAP1, true},
            {MAP1, MAP1, EMPTY_MAP, MAP2, false},
            {MAP1, MAP1, MAP1, MAP1, true},
            {MAP1, MAP1, MAP2, EMPTY_MAP, false},

            // #60
            {MAP1, MAP1, MAP2, MAP1, true},
            {MAP1, MAP1, MAP2, MAP2, false},
            {MAP1, MAP1, MAP2, MAP3, false},
            {MAP1, MAP2, null, null, false},
            {MAP1, MAP2, null, EMPTY_MAP, false},

            // #65
            {MAP1, MAP2, null, MAP1, false},
            {MAP1, MAP2, null, MAP2, true},
            {MAP1, MAP2, null, MAP3, false},
            {MAP1, MAP2, EMPTY_MAP, EMPTY_MAP, false},
            {MAP1, MAP2, EMPTY_MAP, MAP1, false},

            // #70
            {MAP1, MAP2, EMPTY_MAP, MAP2, true},
            {MAP1, MAP2, EMPTY_MAP, MAP3, false},
            {MAP1, MAP2, MAP1, EMPTY_MAP, false},
            {MAP1, MAP2, MAP1, MAP2, true},
            {MAP1, MAP2, MAP2, EMPTY_MAP, false},

            // #75
            {MAP1, MAP2, MAP2, MAP1, false},
            {MAP1, MAP2, MAP2, MAP2, true},
            {MAP1, MAP2, MAP2, MAP3, false},
            {MAP1, MAP2, MAP3, EMPTY_MAP, false},
            {MAP1, MAP2, MAP3, MAP1, false},

            // #80
            {MAP1, MAP2, MAP3, MAP2, true},
            {MAP1, MAP2, MAP3, MAP3, false},
            {MAP1, MAP2, MAP3, MAP4, false}
        });
    }

    @Mock
    private ClidsInfoStorage clidsStorage;
    @Nullable
    private final Map<String, String> newClientClids;
    @Nullable
    private final Map<String, String> newRequestChosenClids;
    @Nullable
    private final Map<String, String> lastClientClidsForRequest;
    @Nullable
    private final Map<String, String> lastRequestChosenClids;
    private final boolean expectedMatch;
    private final ClidsStateChecker clidsStateChecker;
    private final StartupState startupState;

    public ClidsStateCheckerDoRequestClidsMatchTest(
        @Nullable Map<String, String> newClientClids,
        @Nullable Map<String, String> newRequestChosenClids,
        @Nullable Map<String, String> lastClientClidsForRequest,
        @Nullable Map<String, String> lastRequestChosenClids,
        boolean expectedMatch
    ) {
        this.newClientClids = newClientClids;
        this.newRequestChosenClids = newRequestChosenClids;
        this.lastClientClidsForRequest = lastClientClidsForRequest;
        this.lastRequestChosenClids = lastRequestChosenClids;
        this.expectedMatch = expectedMatch;
        clidsStateChecker = new ClidsStateChecker();
        startupState = TestUtils.createDefaultStartupStateBuilder()
            .withDeviceId("did")
            .withUuid("uuid")
            .withDeviceIdHash("hash")
            .withReportAdUrl("url1")
            .withGetAdUrl("url2")
            .withLastClientClidsForStartupRequest(StartupUtils.encodeClids(lastClientClidsForRequest))
            .withLastChosenForRequestClids(StartupUtils.encodeClids(lastRequestChosenClids))
            .withObtainTime(System.currentTimeMillis() / 1000)
            .build();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(clidsStorage.updateAndRetrieveData(new ClidsInfo.Candidate(lastClientClidsForRequest, DistributionSource.APP)))
            .thenReturn(new ClidsInfo.Candidate(lastRequestChosenClids, DistributionSource.APP));
        when(clidsStorage.updateAndRetrieveData(new ClidsInfo.Candidate(newClientClids, DistributionSource.APP)))
            .thenReturn(new ClidsInfo.Candidate(newRequestChosenClids, DistributionSource.APP));
    }

    @Test
    public void test() {
        assertThat(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(newClientClids, startupState, clidsStorage))
            .isEqualTo(expectedMatch);
    }
}
