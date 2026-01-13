package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.reflection.Constants
import io.appmetrica.analytics.impl.id.reflection.ReflectionAdvIdExtractor
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

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
    val reflectionAdvIdProviderRule = constructionRule<ReflectionAdvIdExtractor>()
    private val googleReflectiveAdvIdProvider by lazy { reflectionAdvIdProviderRule.constructionMock.constructed()[0] }
    private val huaweiReflectiveAdvIdProvider by lazy { reflectionAdvIdProviderRule.constructionMock.constructed()[1] }
    private val yandexReflectiveAdvProvider by lazy { reflectionAdvIdProviderRule.constructionMock.constructed()[2] }

    @get:Rule
    val advIdProviderRule = constructionRule<AdvIdExtractorWrapper>()

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
        whenever(googleAdvIdProvider.extractAdTrackingInfo(context)).thenReturn(googleAdTrackingInfoResult)
        whenever(huaweiAdvIdProvider.extractAdTrackingInfo(context)).thenReturn(huaweiAdTrackingInfoResult)
        whenever(yandexAdvIdProvider.extractAdTrackingInfo(context)).thenReturn(yandexAdTrackingInfoResult)
        whenever(yandexAdvIdProvider.extractAdTrackingInfo(eq(context), any()))
            .thenReturn(yandexAdTrackingInfoResult)
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
    fun `setInitialStateFromClientConfigIfNotDefined if not defined`() {
        advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(true)
        verify(controller).updateStateFromClientConfig(true)
    }

    @Test
    fun `setInitialStateFromClientConfigIfNotDefined second time`() {
        advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(true)
        clearInvocations(controller)
        advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(true)
        verifyNoInteractions(controller)
    }

    @Test
    fun `setInitialStateFromClientConfigIfNotDefined after updateStateFromClientConfig`() {
        advertisingIdGetter.updateStateFromClientConfig(true)
        clearInvocations(controller)
        advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(true)
        verifyNoInteractions(controller)
    }

    @Test
    fun updateStateFromClientConfig() {
        advertisingIdGetter.updateStateFromClientConfig(true)
        verify(controller).updateStateFromClientConfig(true)
    }

    @Test
    fun `getIdentifiers if allowed`() {
        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.identifiers
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
        val identifiers = advertisingIdGetter.identifiers

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
        val identifiers = advertisingIdGetter.identifiers

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
        val identifiers = advertisingIdGetter.identifiers

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", googleAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", forbiddenByFeatureResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", yandexAdTrackingInfoResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers if yandex forbid by client config`() {
        whenever(canTrackIdentifiers.canTrackYandexAdvId).thenReturn(
            AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG
        )
        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.identifiers

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
        val identifiers = advertisingIdGetter.identifiers

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", unknownErrorResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", unknownErrorResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", unknownErrorResult)
            .checkAll()
    }

    @Test
    fun `onStartupStateChanged triggers refresh`() {
        advertisingIdGetter.init()
        clearInvocations(executor)

        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        verify(controller).updateStartupState(updatedStartupState)
        verify(executor).execute(any())
    }

    @Test
    fun `updateStateFromClientConfig triggers refresh`() {
        advertisingIdGetter.init()
        clearInvocations(executor)

        advertisingIdGetter.updateStateFromClientConfig(false)

        verify(controller).updateStateFromClientConfig(false)
        verify(executor).execute(any())
    }

    @Test
    fun `refreshIdentifiers not triggered when canTrackIdentifiers unchanged`() {
        advertisingIdGetter.init()
        clearInvocations(executor)

        // Call onStartupStateChanged but controller returns same canTrackIdentifiers
        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        verify(executor).execute(any())
    }

    @Test
    fun `refreshIdentifiers triggered when canTrackIdentifiers changed`() {
        advertisingIdGetter.init()

        val newCanTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
            on { canTrackGaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackHoaid } doReturn AdvIdGetterController.State.ALLOWED
            on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.ALLOWED
        }

        clearInvocations(executor, controller)
        whenever(controller.canTrackIdentifiers()).thenReturn(newCanTrackIdentifiers)

        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        verify(controller).updateStartupState(updatedStartupState)
        verify(executor).execute(any())
    }

    @Test
    fun `getIdentifiers with mixed states`() {
        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.ALLOWED)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
        whenever(canTrackIdentifiers.canTrackYandexAdvId)
            .thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG)

        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.identifiers

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", googleAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", forbiddenByClientResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", forbiddenByFeatureResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers after state change from allowed to forbidden`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // First call with ALLOWED state

        val newCanTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
            on { canTrackGaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackHoaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
        }

        whenever(controller.canTrackIdentifiers()).thenReturn(newCanTrackIdentifiers)
        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        val identifiers = advertisingIdGetter.identifiers

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", forbiddenByClientResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", forbiddenByClientResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", forbiddenByClientResult)
            .checkAll()
    }

    @Test
    fun `getIdentifiers after state change from forbidden to allowed`() {
        whenever(canTrackIdentifiers.canTrackGaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
        whenever(canTrackIdentifiers.canTrackHoaid).thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
        whenever(canTrackIdentifiers.canTrackYandexAdvId)
            .thenReturn(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)

        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // First call with FORBIDDEN state

        val newCanTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
            on { canTrackGaid } doReturn AdvIdGetterController.State.ALLOWED
            on { canTrackHoaid } doReturn AdvIdGetterController.State.ALLOWED
            on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.ALLOWED
        }

        whenever(controller.canTrackIdentifiers()).thenReturn(newCanTrackIdentifiers)
        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        val identifiers = advertisingIdGetter.identifiers

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", googleAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", huaweiAdTrackingInfoResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", yandexAdTrackingInfoResult)
            .checkAll()
    }

    @Test
    fun `multiple updateStateFromClientConfig calls with different values`() {
        advertisingIdGetter.updateStateFromClientConfig(true)
        verify(controller).updateStateFromClientConfig(true)

        clearInvocations(controller)
        advertisingIdGetter.updateStateFromClientConfig(false)
        verify(controller).updateStateFromClientConfig(false)

        clearInvocations(controller)
        advertisingIdGetter.updateStateFromClientConfig(true)
        verify(controller).updateStateFromClientConfig(true)
    }

    @Test
    fun `setInitialStateFromClientConfigIfNotDefined with true then false`() {
        advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(true)
        verify(controller).updateStateFromClientConfig(true)

        clearInvocations(controller)
        advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(false)
        verifyNoInteractions(controller)
    }

    @Test
    fun `init multiple times does not reinitialize`() {
        advertisingIdGetter.init()
        verify(executor).execute(any())

        clearInvocations(executor)
        repeat(5) {
            advertisingIdGetter.init()
        }
        verifyNoInteractions(executor)
    }

    @Test
    fun `getIdentifiers with all providers returning different statuses`() {
        val googleResult = AdTrackingInfoResult(googleAdTrackingInfo, IdentifierStatus.OK, "")
        val huaweiResult = AdTrackingInfoResult(null, IdentifierStatus.FORBIDDEN_BY_CLIENT_CONFIG, "No startup")
        val yandexResult = AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, "Unavailable")

        whenever(googleAdvIdProvider.extractAdTrackingInfo(context)).thenReturn(googleResult)
        whenever(huaweiAdvIdProvider.extractAdTrackingInfo(context)).thenReturn(huaweiResult)
        whenever(yandexAdvIdProvider.extractAdTrackingInfo(eq(context), any())).thenReturn(yandexResult)

        advertisingIdGetter.init()
        val identifiers = advertisingIdGetter.identifiers

        ObjectPropertyAssertions(identifiers)
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("mGoogle", googleResult)
            .checkFieldComparingFieldByFieldRecursively("mHuawei", huaweiResult)
            .checkFieldComparingFieldByFieldRecursively("yandex", yandexResult)
            .checkAll()
    }

    @Test
    fun `backgroundRefreshTask is scheduled after init`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // Wait for blocking task to complete

        verify(executor).executeDelayed(any(), eq(90L), eq(TimeUnit.SECONDS))
    }

    @Test
    fun `backgroundRefreshTask is removed before refresh`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // Wait for initial task
        clearInvocations(executor)

        val newCanTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
            on { canTrackGaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackHoaid } doReturn AdvIdGetterController.State.ALLOWED
            on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.ALLOWED
        }
        whenever(controller.canTrackIdentifiers()).thenReturn(newCanTrackIdentifiers)

        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        verify(executor).remove(any())
    }

    @Test
    fun `backgroundRefreshTask is rescheduled after update`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // Wait for initial task
        clearInvocations(executor)

        val newCanTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
            on { canTrackGaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackHoaid } doReturn AdvIdGetterController.State.ALLOWED
            on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.ALLOWED
        }
        whenever(controller.canTrackIdentifiers()).thenReturn(newCanTrackIdentifiers)

        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)
        advertisingIdGetter.identifiers // Wait for refresh task

        verify(executor).executeDelayed(any(), eq(90L), eq(TimeUnit.SECONDS))
    }

    @Test
    fun `backgroundRefreshTask uses correct interval`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers

        verify(executor).executeDelayed(any(), eq(90L), eq(TimeUnit.SECONDS))
    }

    @Test
    fun `backgroundRefreshTask is not scheduled when identifiers unchanged`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // Wait for initial task
        clearInvocations(executor)

        // onStartupStateChanged with same canTrackIdentifiers
        val updatedStartupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        advertisingIdGetter.onStartupStateChanged(updatedStartupState)

        // Should execute the blocking task but not touch background task
        verify(executor).execute(any())
        verify(executor, never()).remove(any())
        verify(executor, never()).executeDelayed(any(), any(), any())
    }

    @Test
    fun `backgroundRefreshTask is removed and rescheduled on updateStateFromClientConfig`() {
        advertisingIdGetter.init()
        advertisingIdGetter.identifiers // Wait for initial task
        clearInvocations(executor)

        val newCanTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers = mock {
            on { canTrackGaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackHoaid } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            on { canTrackYandexAdvId } doReturn AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
        }
        whenever(controller.canTrackIdentifiers()).thenReturn(newCanTrackIdentifiers)

        advertisingIdGetter.updateStateFromClientConfig(false)
        advertisingIdGetter.identifiers // Wait for refresh task

        verify(executor).remove(any())
        verify(executor).executeDelayed(any(), eq(90L), eq(TimeUnit.SECONDS))
    }
}
