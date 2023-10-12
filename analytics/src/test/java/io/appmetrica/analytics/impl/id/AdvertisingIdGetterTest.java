package io.appmetrica.analytics.impl.id;

import android.content.Context;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor;
import io.appmetrica.analytics.testutils.TestUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AdvertisingIdGetterTest extends CommonTest {

    @Mock
    private AdvertisingIdGetter.RestrictionsProvider mGaidRestrictionsProvider;
    @Mock
    private AdvertisingIdGetter.RestrictionsProvider hoaidRestrictionsProvider;
    @Mock
    private AdvertisingIdGetter.RestrictionsProvider yandexRestrictionsProvider;
    private ICommonExecutor mExecutor;
    @Mock
    private AdvIdProvider mGoogleAdvIdProvider;
    @Mock
    private AdvIdProvider mHuaweiAdvIdProvider;
    @Mock
    private AdvIdProvider yandexAdvIdProvider;

    private AdTrackingInfoResult gaidTrackingInfoResult = new AdTrackingInfoResult(
            new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "gaid", false),
            IdentifierStatus.OK,
            null
    );
    private AdTrackingInfoResult hoaidTrackingInfoResult = new AdTrackingInfoResult(
            new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "hoaid", false),
            IdentifierStatus.OK,
            null
    );
    private AdTrackingInfoResult yandexTrackingInfoResult = new AdTrackingInfoResult(
            new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, "yanedx", false),
            IdentifierStatus.OK,
            null
    );
    private Context mContext;
    private AdvertisingIdGetter mAdvertisingIdGetter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mExecutor = new StubbedBlockingExecutor();
        mContext = RuntimeEnvironment.getApplication();
        when(mGoogleAdvIdProvider.getAdTrackingInfo(mContext)).thenReturn(gaidTrackingInfoResult);
        when(mHuaweiAdvIdProvider.getAdTrackingInfo(mContext)).thenReturn(hoaidTrackingInfoResult);
        when(yandexAdvIdProvider.getAdTrackingInfo(same(mContext), any(RetryStrategy.class))).thenReturn(yandexTrackingInfoResult);
        mAdvertisingIdGetter = new AdvertisingIdGetter(
                mGaidRestrictionsProvider,
                hoaidRestrictionsProvider,
                yandexRestrictionsProvider,
                mExecutor,
                mGoogleAdvIdProvider,
                mHuaweiAdvIdProvider,
                yandexAdvIdProvider,
                "TAG"
        );
    }

    @Test
    public void testNotInited() throws IllegalAccessException {
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkFieldRecursively("mGoogle", new Consumer<ObjectPropertyAssertions<AdTrackingInfoResult>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<AdTrackingInfoResult> innerAssertions) {
                        try {
                            innerAssertions.withIgnoredFields("mStatus");
                            innerAssertions.checkFieldIsNull("mAdTrackingInfo");
                            innerAssertions.checkFieldNonNull("mErrorExplanation");
                            assertThat(innerAssertions.getActual().mStatus).isNotEqualTo(IdentifierStatus.OK);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .checkFieldRecursively("mHuawei", new Consumer<ObjectPropertyAssertions<AdTrackingInfoResult>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<AdTrackingInfoResult> innerAssertions) {
                        try {
                            innerAssertions.withIgnoredFields("mStatus");
                            innerAssertions.checkFieldIsNull("mAdTrackingInfo");
                            innerAssertions.checkFieldNonNull("mErrorExplanation");
                            assertThat(innerAssertions.getActual().mStatus).isNotEqualTo(IdentifierStatus.OK);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .checkFieldRecursively("yandex", new Consumer<ObjectPropertyAssertions<AdTrackingInfoResult>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<AdTrackingInfoResult> innerAssertions) {
                        try {
                            innerAssertions.withIgnoredFields("mStatus");
                            innerAssertions.checkFieldIsNull("mAdTrackingInfo");
                            innerAssertions.checkFieldNonNull("mErrorExplanation");
                            assertThat(innerAssertions.getActual().mStatus).isNotEqualTo(IdentifierStatus.OK);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .checkAll();
    }

    @Test
    public void testNoFirstStartup() throws IllegalAccessException {
        mAdvertisingIdGetter.init(mContext,
                TestUtils.createDefaultStartupStateBuilder().withHadFirstStartup(false).build());
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkFieldComparingFieldByFieldRecursively("mGoogle", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.NO_STARTUP,
                        "startup has not been received yet"
                ))
                .checkFieldComparingFieldByFieldRecursively("mHuawei", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.NO_STARTUP,
                        "startup has not been received yet"
                ))
                .checkFieldComparingFieldByFieldRecursively("yandex", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.UNKNOWN,
                        "identifiers collecting is forbidden for unknown reason"
                ))
                .checkAll();
    }

    @Test
    public void testGaidFeatureDisabled() throws IllegalAccessException {
        when(hoaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        mAdvertisingIdGetter.init(
                mContext,
                new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().withGoogleAid(false).build())
                        .withHadFirstStartup(true)
                        .build()
        );
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkFieldComparingFieldByFieldRecursively("mGoogle", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.FEATURE_DISABLED,
                        "startup forbade advertising identifiers collecting"
                ))
                .checkFieldComparingFieldByFieldRecursively("mHuawei", hoaidTrackingInfoResult)
                .checkFieldComparingFieldByFieldRecursively("yandex", yandexTrackingInfoResult)
                .checkAll();
    }

    @Test
    public void testHoaidFeatureDisabled() throws IllegalAccessException {
        when(mGaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        mAdvertisingIdGetter.init(
                mContext,
                new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().withHuaweiOaid(false).build())
                        .withHadFirstStartup(true)
                        .build()
        );
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkFieldComparingFieldByFieldRecursively("mGoogle", gaidTrackingInfoResult)
                .checkFieldComparingFieldByFieldRecursively("mHuawei", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.FEATURE_DISABLED,
                        "startup forbade advertising identifiers collecting"
                ))
                .checkFieldComparingFieldByFieldRecursively("yandex", yandexTrackingInfoResult)
                .checkAll();
    }

    @Test
    public void testCanTrackAll() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        when(mGaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        AdTrackingInfoResult googleAdTrackingInfo = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "google id", false),
                IdentifierStatus.OK,
                null
        );
        AdTrackingInfoResult huaweiAdTrackingInfo = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.HMS, "huawei id", false),
                IdentifierStatus.OK,
                null
        );
        AdTrackingInfoResult yandexAdTrackingInfo = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, "yandex id", false),
                IdentifierStatus.OK,
                null
        );
        when(mGoogleAdvIdProvider.getAdTrackingInfo(mContext)).thenReturn(googleAdTrackingInfo);
        when(mHuaweiAdvIdProvider.getAdTrackingInfo(mContext)).thenReturn(huaweiAdTrackingInfo);
        when(yandexAdvIdProvider.getAdTrackingInfo(same(mContext), any(RetryStrategy.class))).thenReturn(yandexAdTrackingInfo);
        mAdvertisingIdGetter.init(mContext, mock(StartupState.class));
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkField("mGoogle", "getGoogle", googleAdTrackingInfo)
                .checkField("mHuawei", "getHuawei", huaweiAdTrackingInfo)
                .checkField("yandex", "getYandex", yandexAdTrackingInfo)
                .checkAll();
        ArgumentCaptor<RetryStrategy> strategyCaptor = ArgumentCaptor.forClass(RetryStrategy.class);
        verify(yandexAdvIdProvider).getAdTrackingInfo(same(mContext), strategyCaptor.capture());
        assertThat(strategyCaptor.getValue()).isExactlyInstanceOf(NoRetriesStrategy.class);
    }

    @Test
    public void testCannotTrackGaidUnknownReason() throws Exception {
        when(mGaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(false);
        when(hoaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        mAdvertisingIdGetter.init(mContext, new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder()
                        .withGoogleAid(true)
                        .withHuaweiOaid(true)
                        .build()
        ).withHadFirstStartup(true).build());
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkFieldComparingFieldByFieldRecursively("mGoogle", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.UNKNOWN,
                        "identifiers collecting is forbidden for unknown reason"
                ))
                .checkField("mHuawei", "getHuawei", hoaidTrackingInfoResult)
                .checkField("yandex", "getYandex", yandexTrackingInfoResult)
                .checkAll();
    }

    @Test
    public void testCannotTrackHoaidUnknownReason() throws Exception {
        when(mGaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(false);
        when(yandexRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        mAdvertisingIdGetter.init(mContext, new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder()
                        .withGoogleAid(true)
                        .withHuaweiOaid(true)
                        .build()
        ).withHadFirstStartup(true).build());
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkField("mGoogle", "getGoogle", gaidTrackingInfoResult)
                .checkFieldComparingFieldByFieldRecursively("mHuawei", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.UNKNOWN,
                        "identifiers collecting is forbidden for unknown reason"
                ))
                .checkField("yandex", "getYandex", yandexTrackingInfoResult)
                .checkAll();
    }

    @Test
    public void cannotTrackYandexAdvIdUnknownReason() throws Exception {
        when(mGaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(any(StartupState.class))).thenReturn(false);
        mAdvertisingIdGetter.init(mContext, new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder()
                        .withGoogleAid(true)
                        .withHuaweiOaid(true)
                        .build()
        ).withHadFirstStartup(true).build());
        AdvertisingIdsHolder idsHolder = mAdvertisingIdGetter.getIdentifiers(mContext);
        ObjectPropertyAssertions(idsHolder)
                .withPrivateFields(true)
                .checkField("mGoogle", "getGoogle", gaidTrackingInfoResult)
                .checkField("mHuawei", "getHuawei", hoaidTrackingInfoResult)
                .checkFieldComparingFieldByFieldRecursively("yandex", new AdTrackingInfoResult(
                        null,
                        IdentifierStatus.UNKNOWN,
                        "identifiers collecting is forbidden for unknown reason"
                ))
                .checkAll();
        verifyNoMoreInteractions(yandexAdvIdProvider);
    }

    @Test
    public void testGetIdentifiers() {
        when(mGaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        AdvertisingIdsHolder identifiers = mAdvertisingIdGetter.getIdentifiers(mContext);
        assertThat(identifiers).isEqualToComparingFieldByField(
                new AdvertisingIdsHolder(gaidTrackingInfoResult, hoaidTrackingInfoResult, yandexTrackingInfoResult)
        );
        mAdvertisingIdGetter.getIdentifiers(mContext);
        verify(mGoogleAdvIdProvider, times(1)).getAdTrackingInfo(any(Context.class));
        verify(mHuaweiAdvIdProvider, times(1)).getAdTrackingInfo(any(Context.class));
        ArgumentCaptor<RetryStrategy> strategyCaptor = ArgumentCaptor.forClass(RetryStrategy.class);
        verify(yandexAdvIdProvider).getAdTrackingInfo(same(mContext), strategyCaptor.capture());
        assertThat(strategyCaptor.getValue()).isExactlyInstanceOf(NoRetriesStrategy.class);

    }

    @Test
    public void testGetIdentifiersForced() {
        when(mGaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        AdvertisingIdsHolder identifiers = mAdvertisingIdGetter.getIdentifiersForced(mContext);
        assertThat(identifiers).isEqualToComparingFieldByField(
                new AdvertisingIdsHolder(gaidTrackingInfoResult, hoaidTrackingInfoResult, yandexTrackingInfoResult)
        );
        mAdvertisingIdGetter.getIdentifiersForced(mContext);
        verify(mGoogleAdvIdProvider, times(2)).getAdTrackingInfo(any(Context.class));
        verify(mHuaweiAdvIdProvider, times(2)).getAdTrackingInfo(any(Context.class));
        verify(yandexAdvIdProvider, times(2)).getAdTrackingInfo(same(mContext), any(NoRetriesStrategy.class));
    }

    @Test
    public void testGetIdentifiersForcedMerge() {
        when(mGaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        mAdvertisingIdGetter.getIdentifiersForced(mContext);
        String gaidError = "gaid error";
        String hoaidError = "hoaid error";
        String yandexError = "yandex error";
        when(mGoogleAdvIdProvider.getAdTrackingInfo(mContext)).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, gaidError));
        when(mHuaweiAdvIdProvider.getAdTrackingInfo(mContext)).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, hoaidError));
        when(yandexAdvIdProvider.getAdTrackingInfo(same(mContext), any(RetryStrategy.class))).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, yandexError));
        AdvertisingIdsHolder identifiers = mAdvertisingIdGetter.getIdentifiersForced(mContext);
        assertThat(identifiers).usingRecursiveComparison().isEqualTo(new AdvertisingIdsHolder(
                new AdTrackingInfoResult(gaidTrackingInfoResult.mAdTrackingInfo, IdentifierStatus.UNKNOWN, gaidError),
                new AdTrackingInfoResult(hoaidTrackingInfoResult.mAdTrackingInfo, IdentifierStatus.UNKNOWN, hoaidError),
                new AdTrackingInfoResult(yandexTrackingInfoResult.mAdTrackingInfo, IdentifierStatus.UNKNOWN, yandexError)
        ));
    }

    @Test
    public void testGetIdentifiersForcedWithRetryStrategy() {
        RetryStrategy retryStrategy = mock(RetryStrategy.class);
        when(mGaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(hoaidRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        when(yandexRestrictionsProvider.canTrackAid(nullable(StartupState.class))).thenReturn(true);
        AdvertisingIdsHolder identifiers = mAdvertisingIdGetter.getIdentifiersForced(mContext, retryStrategy);
        assertThat(identifiers).isEqualToComparingFieldByField(
                new AdvertisingIdsHolder(gaidTrackingInfoResult, hoaidTrackingInfoResult, yandexTrackingInfoResult)
        );
        mAdvertisingIdGetter.getIdentifiersForced(mContext, retryStrategy);
        verify(mGoogleAdvIdProvider, times(2)).getAdTrackingInfo(any(Context.class));
        verify(mHuaweiAdvIdProvider, times(2)).getAdTrackingInfo(any(Context.class));
        verify(yandexAdvIdProvider, times(2)).getAdTrackingInfo(mContext, retryStrategy);
    }

    @Test
    public void testOnStartupChanged() {
        StartupState startupState = mock(StartupState.class);
        mAdvertisingIdGetter.onStartupStateChanged(startupState);
        mAdvertisingIdGetter.getIdentifiersForced(mContext);
        verify(mGaidRestrictionsProvider).canTrackAid(startupState);
        verify(hoaidRestrictionsProvider).canTrackAid(startupState);
        verify(yandexRestrictionsProvider).canTrackAid(startupState);
    }
}
