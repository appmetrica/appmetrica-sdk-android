package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.id.AppSetIdGetter;
import io.appmetrica.analytics.impl.id.RetryStrategy;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class StartupUnitBaseTest extends CommonTest {

    static final String TEST_RESPONSE = "{\"device_id\":{\"value\":\"abcae254a259c453a02d0e1bca02c4ff\"},\"distribution_customization\":{\"brand_id\":\"\",\"clids\":{\"clid1\":{\"value\":\"1\"}},\"switch_search_widget_to_yandex\":\"0\"},\"query_hosts\":{\"list\":{\"check_updates\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"},\"get_ad\":{\"url\":\"https:\\/\\/mobile.yandexadexchange.net\"},\"report\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"},\"report_ad\":{\"url\":\"https:\\/\\/mobile.yandexadexchange.net\"},\"search\":{\"url\":\"\"},\"url_schemes\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"}}},\"uuid\":{\"value\":\"ecf192096fb1c3e0b247a08edb0a60ce\"},\"retry_policy\":{\"max_interval_seconds\":1000,\"exponential_multiplier\":2}}\n";

    @Mock
    StartupResult mStartupResult;
    @Mock
    StartupRequestConfig mStartupRequestConfig;
    @Mock
    StartupResultListener mStartupResultListener;
    @Mock
    AdvertisingIdsHolder mAdvertisingIdsHolder;
    @Mock
    TimeProvider timeProvider;
    @Mock
    ClidsInfoStorage clidsStorage;
    @Mock
    ClidsStateChecker clidsStateChecker;
    @Mock
    AdvertisingIdGetter advertisingIdGetter;
    @Mock
    AppSetIdGetter appSetIdGetter;
    @Mock
    StartupState.Storage startupStateStorage;
    StartupRequestConfig.Arguments mSdkConfig = StartupArgumentsTest.empty();
    StartupUnit mStartupUnit;
    Long mObtainServerTime = 1234567L;
    Long mFirstStartupServerTime = 5000L;
    StartupConfigurationHolder mConfigurationHolder;
    DataResultReceiver mDataResultReceiver;

    @Rule
    public RuleChain mRuleChain = RuleChain.outerRule(new GlobalServiceLocatorRule()).around(new ServiceMigrationCheckedRule());

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider().readUuid())
            .thenReturn(new IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null));
        when(GlobalServiceLocator.getInstance().getServiceInternalAdvertisingIdGetter().getIdentifiers(any(Context.class)))
                .thenReturn(new AdvertisingIdsHolder());
        mDataResultReceiver = mock(DataResultReceiver.class);
        mSdkConfig = new StartupRequestConfig.Arguments(new ClientConfiguration(new ProcessConfiguration(RuntimeEnvironment.getApplication(), mDataResultReceiver), new CounterConfiguration()));
        when(startupStateStorage.read()).thenReturn(TestUtils.createDefaultStartupState());
        when(GlobalServiceLocator.getInstance().getServiceInternalAdvertisingIdGetter()).thenReturn(advertisingIdGetter);
        when(GlobalServiceLocator.getInstance().getAppSetIdGetter()).thenReturn(appSetIdGetter);
        when(advertisingIdGetter.getIdentifiersForced(any(Context.class), any(RetryStrategy.class))).thenReturn(new AdvertisingIdsHolder());
        when(appSetIdGetter.getAppSetId()).thenReturn(new AppSetId(null, AppSetIdScope.UNKNOWN));
        mStartupUnit = new StartupUnit(
                RuntimeEnvironment.getApplication(),
                RuntimeEnvironment.getApplication().getPackageName(),
                mSdkConfig,
                mStartupResultListener,
                startupStateStorage,
                timeProvider,
                clidsStorage,
                clidsStateChecker
        );
        when(mStartupRequestConfig.getChosenClids()).thenReturn(mock(ClidsInfo.Candidate.class));
        when(mStartupResult.getCollectionFlags()).thenReturn(mock(CollectingFlags.class));
        mConfigurationHolder = spy(mStartupUnit.getConfigHolder());
        mConfigurationHolder.updateArguments(mSdkConfig);
        doReturn(mStartupRequestConfig).when(mConfigurationHolder).get();
        when(mStartupRequestConfig.getAdvertisingIdsHolder()).thenReturn(mAdvertisingIdsHolder);
        mStartupUnit.setConfigHolder(mConfigurationHolder);
    }

}
