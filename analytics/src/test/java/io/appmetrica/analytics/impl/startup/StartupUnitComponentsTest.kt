package io.appmetrica.analytics.impl.startup

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommutationComponentId
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class StartupUnitComponentsTest : CommonTest() {

    private val context: Context = mock()
    private val packageName = "test.package.name"
    private val requestConfigArguments: StartupRequestConfig.Arguments = mock()
    private val listener: StartupResultListener = mock()
    private val startupState: StartupState = mock()

    @get:Rule
    val commutationComponentIdConstructionRule = constructionRule<CommutationComponentId>()

    @get:Rule
    val startupStateStorageMockedConstructionRule = constructionRule<StartupState.Storage> {
        on { read() } doReturn startupState
    }

    @get:Rule
    val deviceIdGeneratorMockedConstructionRule = constructionRule<DeviceIdGenerator>()

    @get:Rule
    val systemTimeProviderMockedConstructionRule = constructionRule<SystemTimeProvider>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val clidsStateCheckerMockedConstructionRule = constructionRule<ClidsStateChecker>()

    @get:Rule
    val requestConfigLoaderMockedConstructionRule = constructionRule<StartupRequestConfig.Loader>()

    @get:Rule
    val startupConfigurationHolderMockedConstructionRule = constructionRule<StartupConfigurationHolder> {
        on { startupState } doReturn startupState
    }

    @get:Rule
    val uuidValidatorMockedConstructionRule = constructionRule<UuidValidator>()

    private lateinit var startupUnitComponents: StartupUnitComponents

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().startupStateHolder.getStartupState()).thenReturn(startupState)
        startupUnitComponents = StartupUnitComponents(context, packageName, requestConfigArguments, listener)
    }

    @Test
    fun componentId() {
        assertThat(startupUnitComponents.componentId)
            .isEqualTo(commutationComponentIdConstructionRule.constructionMock.constructed().first())
        assertThat(commutationComponentIdConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(commutationComponentIdConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(packageName)
    }

    @Test
    fun startupStateStorage() {
        assertThat(startupUnitComponents.startupStateStorage)
            .isEqualTo(startupStateStorageMockedConstructionRule.constructionMock.constructed().first())
        assertThat(startupStateStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupStateStorageMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
    }

    @Test
    fun deviceIdGenerator() {
        assertThat(startupUnitComponents.deviceIdGenerator)
            .isEqualTo(deviceIdGeneratorMockedConstructionRule.constructionMock.constructed().first())
        assertThat(deviceIdGeneratorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(deviceIdGeneratorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
    }

    @Test
    fun timeProvider() {
        assertThat(startupUnitComponents.timeProvider)
            .isEqualTo(systemTimeProviderMockedConstructionRule.constructionMock.constructed().first())
        assertThat(systemTimeProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(systemTimeProviderMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun clidsStorage() {
        assertThat(startupUnitComponents.clidsStorage).isEqualTo(GlobalServiceLocator.getInstance().clidsStorage)
    }

    @Test
    fun clidsStateChecker() {
        assertThat(startupUnitComponents.clidsStateChecker)
            .isEqualTo(clidsStateCheckerMockedConstructionRule.constructionMock.constructed().first())
        assertThat(clidsStateCheckerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(clidsStateCheckerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun startupConfigurationHolder() {
        assertThat(startupUnitComponents.startupConfigurationHolder)
            .isEqualTo(startupConfigurationHolderMockedConstructionRule.constructionMock.constructed().first())
        assertThat(startupConfigurationHolderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupConfigurationHolderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                requestConfigLoaderMockedConstructionRule.constructionMock.constructed().first(),
                startupState,
                requestConfigArguments
            )
        assertThat(requestConfigLoaderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(requestConfigLoaderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, packageName)
    }

    @Test
    fun multiProcessSafeUuidProvider() {
        assertThat(startupUnitComponents.multiProcessSafeUuidProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().multiProcessSafeUuidProvider)
    }

    @Test
    fun uuidValidator() {
        assertThat(startupUnitComponents.uuidValidator)
            .isEqualTo(uuidValidatorMockedConstructionRule.constructionMock.constructed().first())
        assertThat(uuidValidatorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(uuidValidatorMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun startupStateHolder() {
        assertThat(startupUnitComponents.startupStateHolder)
            .isEqualTo(GlobalServiceLocator.getInstance().startupStateHolder)
    }
}
