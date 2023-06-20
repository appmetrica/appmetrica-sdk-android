package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.proxy.validation.ConfigChecker;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterConfigTest extends CommonTest {

    private final int oldMaxReportsInDatabaseCount = 10;
    private final int newMaxReportsInDatabaseCount = 100;
    @Rule
    public final MockedConstructionRule<ConfigChecker> configCheckerRule =
            new MockedConstructionRule<>(ConfigChecker.class,
                    new MockedConstruction.MockInitializer<ConfigChecker>() {
                        @Override
                        public void prepare(ConfigChecker mock, MockedConstruction.Context context) {
                            when(mock.getCheckedMaxReportsInDatabaseCount(anyInt())).thenAnswer(new Answer<Integer>() {
                                @Override
                                public Integer answer(InvocationOnMock invocation) throws Throwable {
                                    return invocation.getArgument(0);
                                }
                            });
                            when(mock.getCheckedMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount))
                                    .thenReturn(newMaxReportsInDatabaseCount);
                        }
                    });
    private final String additionalConfigKeyFirst = "key1";
    private final String additionalConfigValueFirst = "value1";
    private final String additionalConfigKeySecond = "key2";
    private final String additionalConfigValueSecond = "value2";
    private final Map<String, Object> additionalConfigMap = new HashMap<String, Object>() {
        {
            put(additionalConfigKeyFirst, additionalConfigValueFirst);
            put(additionalConfigKeySecond, additionalConfigValueSecond);
        }
    };

    private final String mApiKey = UUID.randomUUID().toString();
    private static final int SESSION_TIMEOUT = 44;
    private static final boolean STATISTICS_SENDING = true;
    private static final int MAX_REPORTS_IN_DB_COUNT = 2000;
    private static final String USER_PROFILE_ID = "user_profile_id";
    private static final int DISPATCH_PERIOD = 122;
    private static final int MAX_REPORTS_COUNT = 22;
    private final String appEnvironmentMapKeyFirst = "appEnvironmentMap_key1";
    private final String appEnvironmentMapValueFirst = "appEnvironmentMap_value1";
    private final String appEnvironmentMapKeySecond = "appEnvironmentMap_key2";
    private final String appEnvironmentMapValueSecond = "appEnvironmentMap_value2";
    private final Map<String, Object> appEnvironmentMap = new HashMap<String, Object>() {
        {
            put(appEnvironmentMapKeyFirst, appEnvironmentMapValueFirst);
            put(appEnvironmentMapKeySecond, appEnvironmentMapValueSecond);
        }
    };
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuilder() throws Exception {
        ReporterConfig config = ReporterConfig.newConfigBuilder(mApiKey)
                .withSessionTimeout(SESSION_TIMEOUT)
                .withLogs()
                .withStatisticsSending(STATISTICS_SENDING)
                .withMaxReportsInDatabaseCount(MAX_REPORTS_IN_DB_COUNT)
                .withUserProfileID(USER_PROFILE_ID)
                .withDispatchPeriodSeconds(DISPATCH_PERIOD)
                .withMaxReportsCount(MAX_REPORTS_COUNT)
                .withAppEnvironmentValue(appEnvironmentMapKeyFirst, appEnvironmentMapValueFirst)
                .withAppEnvironmentValue(appEnvironmentMapKeySecond, appEnvironmentMapValueSecond)
                .withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
                .withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
                .build();

        ObjectPropertyAssertions(config)
                .checkField("apiKey", mApiKey)
                .checkField("sessionTimeout", SESSION_TIMEOUT)
                .checkField("logs", true)
                .checkField("statisticsSending", STATISTICS_SENDING)
                .checkField("maxReportsInDatabaseCount", MAX_REPORTS_IN_DB_COUNT)
                .checkField("userProfileID", USER_PROFILE_ID)
                .checkField("dispatchPeriodSeconds", DISPATCH_PERIOD)
                .checkField("maxReportsCount", MAX_REPORTS_COUNT)
                .checkField("appEnvironment", appEnvironmentMap)
                .checkField("additionalConfig", additionalConfigMap)
                .checkAll();
    }

    @Test
    public void buildObjectWithDefaults() throws Exception {
        ReporterConfig config = ReporterConfig.newConfigBuilder(mApiKey).build();

        ObjectPropertyAssertions(config)
                .checkField("apiKey", mApiKey)
                .checkField("sessionTimeout", null)
                .checkField("logs", null)
                .checkField("statisticsSending", null)
                .checkField("maxReportsInDatabaseCount", null)
                .checkField("userProfileID", null)
                .checkField("dispatchPeriodSeconds", null)
                .checkField("maxReportsCount", null)
                .checkField("appEnvironment", Collections.emptyMap())
                .checkField("additionalConfig", Collections.emptyMap())
                .checkAll();
    }

    @Test(expected = ValidationException.class)
    public void testInvalidApiKey() {
        ReporterConfig.newConfigBuilder("");
    }

    @Test
    public void testInvalidMaxReportsInDatabaseCount() {
        ReporterConfig config = ReporterConfig.newConfigBuilder(mApiKey)
                .withMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount)
                .build();

        assertThat(config.maxReportsInDatabaseCount).isEqualTo(newMaxReportsInDatabaseCount);
    }
}
