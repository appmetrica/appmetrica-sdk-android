package io.appmetrica.analytics.impl.startup;

import android.text.TextUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.startup.parsing.StartupParser;
import io.appmetrica.analytics.impl.utils.ServerTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StartupUnitClidsFromStartupTest extends StartupUnitBaseTest {

    private static final String STARTUP_RESPONSE_PATTERN = "{\"device_id\":{\"value\":\"abcae254a259c453a02d0e1bca02c4ff\"},%s\"query_hosts\":{\"list\":{\"check_updates\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"},\"get_ad\":{\"url\":\"https:\\/\\/mobile.yandexadexchange.net\"},\"report\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"},\"report_ad\":{\"url\":\"https:\\/\\/mobile.yandexadexchange.net\"},\"search\":{\"url\":\"\"},\"url_schemes\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"}}},\"uuid\":{\"value\":\"ecf192096fb1c3e0b247a08edb0a60ce\"},\"features\":{\"list\":{\"socket\":{\"enabled\":\"true\"}}}}\n";
    private static final String STARTUP_MISSING_DISTRIBUTION_CUSTOMIZATION = "";
    private static final String STARTUP_MISSING_CLIDS = "\"distribution_customization\" : {},";
    private static final String STARTUP_EMPTY_CLIDS = "\"distribution_customization\" : {\"brand_id\" : \"\",\"clids\" : {},\"switch_search_widget_to_yandex\" : \"0\"},";
    private static final String STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE = "\"distribution_customization\" : {\"brand_id\" : \"\",\"clids\" : {\"clid0\" : {\"value\":\"\"}},\"switch_search_widget_to_yandex\" : \"0\"},";
    private static final String STARTUP_CLIDS_WITH_INVALID_CLID_VALUE = "\"distribution_customization\" : {\"brand_id\" : \"\",\"clids\" : {\"clid0\" : {\"value\":\"A\"}},\"switch_search_widget_to_yandex\" : \"0\"},";
    private static final String STARTUP_VALID_CLIDS = "\"distribution_customization\" : {\"brand_id\" : \"\",\"clids\" : {\"clid0\" : {\"value\":\"55\"}},\"switch_search_widget_to_yandex\" : \"0\"},";
    private static final String STARTUP_VALID_CLIDS_ENCODED = "clid0:55";

    private static final String STORED_VALID_CLIDS = "clid0:0";
    private static final String STORED_INVALID_CLIDS_WITHOUT_VALUE = "clid0:";
    private static final String STORED_INVALID_CLIDS_WITH_NON_NUMBER_VALUE = "clid0:A";

    private String mStoredClids;
    private String mClidsFromStartup;
    private String mExpectedClids;

    private String mStartupResponse;

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> getData() throws JSONException {
        return Arrays.asList(new Object[][]{
                {"[1]Should store null if distribution customization block from startup is missed and stored clids are null",
                        null, STARTUP_MISSING_DISTRIBUTION_CUSTOMIZATION, null},
                {"[2]Should store null if distribution customization block from startup is missed and stored clids are empty",
                        "", STARTUP_MISSING_DISTRIBUTION_CUSTOMIZATION, null},
                {"[3]Should not change clids if distribution customization block from startup is missed and and stored clids are valid",
                        STORED_VALID_CLIDS, STARTUP_MISSING_DISTRIBUTION_CUSTOMIZATION, STORED_VALID_CLIDS},
                {"[4]Should store null if clids block from startup is missed and stored clids are null",
                        null, STARTUP_MISSING_CLIDS, null},
                {"[5]Should store null if clids block from startup is missed and stored clids are empty", "",
                        STARTUP_MISSING_CLIDS, null},
                {"[6]Should not change clids if clids block from startup is missed and stored clids are valid",
                        STORED_VALID_CLIDS, STARTUP_MISSING_CLIDS, STORED_VALID_CLIDS},
                {"[7]Should store null if clids block from startup is empty and stored clids are empty", "",
                        STARTUP_EMPTY_CLIDS, null},
                {"[8]Should store null if clids block from startup is empty and stored clids are empty",
                        "", STARTUP_EMPTY_CLIDS, null},
                {"[9]Should not change clids block from startup is empty and stored clids are valid",
                        STORED_VALID_CLIDS, STARTUP_EMPTY_CLIDS, STORED_VALID_CLIDS},
                {"[10]Should store null if clids from startup contains empty value and stored clids are null",
                        null, STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE, null},
                {"[11]Should store null if clids from startup contains empty value and stored clids are empty",
                        "", STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE, null},
                {"[12]Should not change clids if clids from startup contains empty value and stored clids are valid",
                        STORED_VALID_CLIDS, STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE, STORED_VALID_CLIDS},
                {"[13]Should store null if clids from startup contains invalid value and stored clids are null",
                        null, STARTUP_CLIDS_WITH_INVALID_CLID_VALUE, null},
                {"[14]Should null if clids from startup contains invalid value and stored clids are empty",
                        "", STARTUP_CLIDS_WITH_INVALID_CLID_VALUE, null},
                {"[15]Should not change clids if clids from startup contains invalid value and store clids are valid",
                        STORED_VALID_CLIDS, STARTUP_CLIDS_WITH_INVALID_CLID_VALUE, STORED_VALID_CLIDS},
                {"[16]Should store clids from startup if startup contains valid clids and stored clids are null",
                        null, STARTUP_VALID_CLIDS, STARTUP_VALID_CLIDS_ENCODED},
                {"[17]Should store clids from startup if startup contains valid clids and stored clids are empty",
                        "", STARTUP_VALID_CLIDS, STARTUP_VALID_CLIDS_ENCODED},
                {"[18]Should store clids from startup if startup contains valid clids and stored clids are invalid",
                        "", STARTUP_VALID_CLIDS, STARTUP_VALID_CLIDS_ENCODED},
                {"[19]Should store clids from startup if startup contains valid clids and store clids are valid",
                        STORED_VALID_CLIDS, STARTUP_VALID_CLIDS, STARTUP_VALID_CLIDS_ENCODED},
                {"[20]Should clear stored clids with empty value if clids are missed in startup",
                        STORED_INVALID_CLIDS_WITHOUT_VALUE, STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE, null},
                {"[21]Should clear stored invalid clids if clids are missed in startup",
                        STORED_INVALID_CLIDS_WITH_NON_NUMBER_VALUE, STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE, null},
                {"[22]Should clear stored clids with empty value if startup contains clids with empty value",
                        STORED_INVALID_CLIDS_WITHOUT_VALUE, STARTUP_CLIDS_WITH_EMPTY_CLID_VALUE, null},
                {"[23]Should clear invalid stored clids if startup contains invalid clids",
                        STORED_INVALID_CLIDS_WITH_NON_NUMBER_VALUE, STARTUP_CLIDS_WITH_INVALID_CLID_VALUE, null}

        });
    }

    public StartupUnitClidsFromStartupTest(String description, String storedClids, String clidsFromStartup,
                                           String expectedClids) {
        mStoredClids = storedClids;
        mClidsFromStartup = clidsFromStartup;
        mExpectedClids = expectedClids;
    }

    @Override
    @Before
    public void setup() {
        super.setup();
        mStartupResponse = String.format(STARTUP_RESPONSE_PATTERN, mClidsFromStartup);
        ServerTime.getInstance().init(mock(PreferencesServiceDbStorage.class), mock(TimeProvider.class));
    }

    @Test
    public void testClids() {
        clearInvocations(startupStateStorage);
        mStartupUnit.setStartupState(new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build()).withEncodedClidsFromResponse(mStoredClids).build());
        mStartupUnit.onRequestComplete(new StartupParser().parseStartupResponse(mStartupResponse.getBytes()), mStartupRequestConfig, new HashMap<String, List<String>>());
        assertThat(mStartupUnit.getStartupState().getEncodedClidsFromResponse()).isEqualTo(mExpectedClids);
        verify(startupStateStorage, times(1)).save(argThat(new ArgumentMatcher<StartupState>() {
            @Override
            public boolean matches(StartupState argument) {
                if (!(argument != null)) {
                    return false;
                } else {
                    return TextUtils.equals(mExpectedClids, argument.getEncodedClidsFromResponse());
                }
            }
        }));
    }
}
