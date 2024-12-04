package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.reflection.Constants
import io.appmetrica.analytics.impl.id.reflection.ReflectionAdvIdProvider
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AdvertisingIdGetterTest : CommonTest() {

    private val context: Context = mock()
    private val executor: ICommonExecutor = mock {
        on { execute(any()) } doAnswer { invocationOnMock ->
            val runnable = invocationOnMock.arguments.first() as Runnable
            runnable.run()
        }
    }
    private val startupState: StartupState = mock()

    private val trackingDisabledByApi = "advertising identifiers collecting is forbidden by client configuration"
    private val featureDisabled = "advertising identifiers collecting is forbidden by startup"
    private val unknownProblem = "advertising identifiers collecting is forbidden by unknown reason"

    @get:Rule
    val reflectionAdvIdProviderRule = constructionRule<ReflectionAdvIdProvider>()
    private val googleReflectiveAdvIdProvider by lazy { reflectionAdvIdProviderRule.constructionMock.constructed()[0] }
    private val huaweiReflectiveAdvIdProvider by lazy { reflectionAdvIdProviderRule.constructionMock.constructed()[1] }
    private val yandexReflectiveAdvProvider by lazy { reflectionAdvIdProviderRule.constructionMock.constructed()[2] }

    @get:Rule
    val advIdProviderRule = constructionRule<AdvIdProviderWrapper>()

    private val googleAdvIdProvider by lazy { advIdProviderRule.constructionMock.constructed()[0] }
    private val huaweiAdvIdProvider by lazy { advIdProviderRule.constructionMock.constructed()[1] }
    private val yandexAdvIdProvider by lazy { advIdProviderRule.constructionMock.constructed()[2] }

    private val googleAdTrackingInfo: AdTrackingInfo = mock()
    private val huaweiAdTrackingInfo: AdTrackingInfo = mock()
    private val yandexAdTrackingInfo: AdTrackingInfo = mock()

    private val googleAdTrackingInfoResult = AdTrackingInfoResult(googleAdTrackingInfo, IdentifierStatus.OK, "")
    private val huaweiAdTrackingInfoResult = AdTrackingInfoResult(huaweiAdTrackingInfo, IdentifierStatus.OK, "")
    private val yandexAdTrackingInfoResult = AdTrackingInfoResult(yandexAdTrackingInfo, IdentifierStatus.OK, "")

    private val canTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
        on { canTrackGaid } doReturn AdvIdGetterController.State.ALLOWED
        on { canTrackHoaid } doReturn AdvIdGetterController.State.ALLOWED
        on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.ALLOWED
    }

    @get:Rule
    val controllerRule = constructionRule<AdvIdGetterController> {
        on { canTrackIdentifiers() } doReturn canTrackIdentifiers
    }
    private val controller by controllerRule

    @get:Rule
    val noRetryStrategyRule = constructionRule<NoRetriesStrategy>()

    private val forbiddenByClientResult =
        AdTrackingInfoResult(null, IdentifierStatus.FORBIDDEN_BY_CLIENT_CONFIG, trackingDisabledByApi)
    private val forbiddenByFeatureResult =
        AdTrackingInfoResult(null, IdentifierStatus.FEATURE_DISABLED, featureDisabled)
    private val unknownErrorResult =
        AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, unknownProblem)

    private lateinit var advertisingIdGetter: AdvertisingIdGetter

    @Before
    fun setUp() {
        advertisingIdGetter = AdvertisingIdGetter(context, executor, startupState)
        whenever(googleAdvIdProvider.getAdTrackingInfo(context)).thenReturn(googleAdTrackingInfoResult)
        whenever(huaweiAdvIdProvider.getAdTrackingInfo(context)).thenReturn(huaweiAdTrackingInfoResult)
        whenever(yandexAdvIdProvider.getAdTrackingInfo(context)).thenReturn(yandexAdTrackingInfoResult)
        whenever(yandexAdvIdProvider.getAdTrackingInfo(eq(context), any())).thenReturn(yandexAdTrackingInfoResult)
    }

    @Test
    fun `constructor - reflective adv id provider`() {
        assertThat(reflectionAdvIdProviderRule.constructionMock.constructed()).hasSize(3)
        assertThat(reflectionAdvIdProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(
                Constants.Providers.GOOGLE,
                Constants.Providers.HUAWEI,
                Constants.Providers.YANDEX
            )
    }

    @Test
    fun `constructor - provider`() {
        assertThat(advIdProviderRule.constructionMock.constructed()).hasSize(3)
        assertThat(advIdProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(googleReflectiveAdvIdProvider, huaweiReflectiveAdvIdProvider, yandexReflectiveAdvProvider)
    }

    @Test
    fun `constructor - controller`() {
        assertThat(controllerRule.constructionMock.constructed()).hasSize(1)
        assertThat(controllerRule.argumentInterceptor.flatArguments()).containsExactly(startupState)
    }

    @Test
    fun init() {
        repeat(10) { advertisingIdGetter.init() }
        verify(executor).execute(any())
    }

    @Test
    fun onStartupStateChanged() {
        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)
        verify(controller).updateStartupState(updatedStartupState)
    }

    @Test
    fun updateStateFromClientConfig() {
        advertisingIdGetter.updateStateFromClientConfig(true)
        verify(controller).updateStateFromClientConfig(true)
    }

    @Test
    fun `getIdentifiers if allowed`() {
        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.getIdentifiers(context)
        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkField("mGoogle", googleAdTrackingInfoResult)
            .checkField("mHuawei", huaweiAdTrackingInfoResult)
            .checkField("yandex", yandexAdTrackingInfoResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers if forbid by client config`() {
        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
        whenever(canTrackIdentifiers.canTrackYandexAdvId)
            .thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)

        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.getIdentifiers(context)

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", forbiddenByClientResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", forbiddenByClientResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", forbiddenByClientResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers if google forbid by client config`() {
        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG)
        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.getIdentifiers(context)

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", forbiddenByFeatureResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", huaweiAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", yandexAdTrackingInfoResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers if huawei forbid by client config`() {
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG)
        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.getIdentifiers(context)

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", googleAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", forbiddenByFeatureResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", yandexAdTrackingInfoResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers if yandex forbid by client config`() {
        whenever(canTrackIdentifiers.canTrackYandexAdvId).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG)
        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.getIdentifiers(context)

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", googleAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", huaweiAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", forbiddenByFeatureResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers for unknown error`() {
        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.UNKNOWN)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.UNKNOWN)
        whenever(canTrackIdentifiers.canTrackYandexAdvId).thenReturn(AdvIdGetterController.State.UNKNOWN)

        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.getIdentifiers(context)

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", unknownErrorResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", unknownErrorResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", unknownErrorResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiersForced - valid after empty`() {
        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.UNKNOWN)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.UNKNOWN)
        whenever(canTrackIdentifiers.canTrackYandexAdvId).thenReturn(AdvIdGetterController.State.UNKNOWN)

        advertisingIdGetter.init()

        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.ALLOWED)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.ALLOWED)
        whenever(canTrackIdentifiers.canTrackYandexAdvId).thenReturn(AdvIdGetterController.State.ALLOWED)

        val identifiers = advertisingIdGetter.identifiersForced
        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkField("mGoogle", googleAdTrackingInfoResult)
            .checkField("mHuawei", huaweiAdTrackingInfoResult)
            .checkField("yandex", yandexAdTrackingInfoResult)
            .checkAll()
    }

    @Test
    fun `getIdentifierForced - problem after valid`() {
        advertisingIdGetter.init()

        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.UNKNOWN)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.UNKNOWN)
        whenever(canTrackIdentifiers.canTrackYandexAdvId).thenReturn(AdvIdGetterController.State.UNKNOWN)

        val identifiers = advertisingIdGetter.identifiersForced
        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively(
                "mGoogle",
                AdTrackingInfoResult(googleAdTrackingInfo, IdentifierStatus.UNKNOWN, unknownProblem)
            ).checkFieldComparingFieldByFieldRecursively(
                "mHuawei",
                AdTrackingInfoResult(huaweiAdTrackingInfo, IdentifierStatus.UNKNOWN, unknownProblem)
            ).checkFieldComparingFieldByFieldRecursively(
                "yandex",
                AdTrackingInfoResult(yandexAdTrackingInfo, IdentifierStatus.UNKNOWN, unknownProblem)
            ).checkAll()
    }
}
