package io.appmetrica.analytics.impl.startup;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ClidsStateCheckerDoClientClidsMatchClientClidsForRequestTest extends CommonTest {

    private static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();
    private static final Map<String, String> MAP_WITH_VALID_ITEMS_1 = new HashMap<String, String>();
    private static final Map<String, String> MAP_WITH_VALID_ITEMS_2 = new HashMap<String, String>();

    static {
        MAP_WITH_VALID_ITEMS_1.put("clid0", "0");
        MAP_WITH_VALID_ITEMS_1.put("clid1", "1");

        MAP_WITH_VALID_ITEMS_2.put("clid1", "1");
        MAP_WITH_VALID_ITEMS_2.put("clid2", "2");
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{index}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // #0
                {null, null, true},
                {null, EMPTY_MAP, true},
                {null, MAP_WITH_VALID_ITEMS_1, false},
                {EMPTY_MAP, null, true},
                {EMPTY_MAP, EMPTY_MAP, true},

                // #5
                {EMPTY_MAP, MAP_WITH_VALID_ITEMS_1, false},
                {MAP_WITH_VALID_ITEMS_1, null, false},
                {MAP_WITH_VALID_ITEMS_1, EMPTY_MAP, false},
                {MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1, true},
                {MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_2, false},
        });
    }

    @Nullable
    private final Map<String, String> clientClids;
    @Nullable
    private final Map<String, String> clientClidsForRequest;
    private final boolean doMatch;
    private final ClidsStateChecker clidsStateChecker;

    public ClidsStateCheckerDoClientClidsMatchClientClidsForRequestTest(@Nullable Map<String, String> clientClids,
                                                                        @Nullable Map<String, String> clientClidsForRequest,
                                                                        boolean doMatch) {
        this.clientClids = clientClids;
        this.clientClidsForRequest = clientClidsForRequest;
        this.doMatch = doMatch;
        clidsStateChecker = new ClidsStateChecker();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void doRequestClidsMatchResponseClids() {
        assertThat(clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                clientClids,
                clientClidsForRequest
        )).isEqualTo(doMatch);
    }
}
