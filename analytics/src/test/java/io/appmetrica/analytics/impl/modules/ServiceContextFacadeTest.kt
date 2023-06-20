package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.LocaleHolder
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.modulesapi.internal.ModuleLifecycleController
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ServiceContextFacadeTest : CommonTest() {

    private val moduleLifecycleController = mock<ModuleLifecycleController>()
    private val databaseStorageFactory = mock<DatabaseStorageFactory>()
    private val storageForService = mock<DatabaseStorage>()
    private val localeHolder = mock<LocaleHolder>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val networkContextImplMockedRule = MockedConstructionRule(NetworkContextImpl::class.java)

    @get:Rule
    val moduleSelfReporterImplMockedRule = MockedConstructionRule(ModuleSelfReporterImpl::class.java)

    @get:Rule
    val serviceWakeLockImplMockedRule = MockedConstructionRule(ServiceWakeLockImpl::class.java)

    @get:Rule
    val serviceWakeLockBinderMockedRule = MockedConstructionRule(ServiceWakeLockBinder::class.java)

    @get:Rule
    val appMetricaServiceWakeLockIntentProviderMockedRule =
        MockedConstructionRule(AppMetricaServiceWakeLockIntentProvider::class.java)

    @get:Rule
    val storageProviderImplMockedRule = MockedConstructionRule(StorageProviderImpl::class.java)

    @get:Rule
    val executorProviderImplMockedRule = MockedConstructionRule(ExecutorProviderImpl::class.java)

    @get:Rule
    val databaseStorageFactoryMockedStaticRule = MockedStaticRule(DatabaseStorageFactory::class.java)

    @get:Rule
    val localeHolderMockedStaticRule = MockedStaticRule(LocaleHolder::class.java)

    private lateinit var serviceContextFacade: ServiceContextFacade

    @Before
    fun setUp() {
        whenever(DatabaseStorageFactory.getInstance(GlobalServiceLocator.getInstance().context))
            .thenReturn(databaseStorageFactory)
        whenever(databaseStorageFactory.storageForService).thenReturn(storageForService)
        whenever(LocaleHolder.getInstance(GlobalServiceLocator.getInstance().context)).thenReturn(localeHolder)
        serviceContextFacade = ServiceContextFacade(moduleLifecycleController)
    }

    @Test
    fun context() {
        assertThat(serviceContextFacade.context).isEqualTo(GlobalServiceLocator.getInstance().context)
    }

    @Test
    fun networkContext() {
        assertThat(serviceContextFacade.networkContext)
            .isEqualTo(networkContextImplMockedRule.constructionMock.constructed()[0])
        assertThat(networkContextImplMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(networkContextImplMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(GlobalServiceLocator.getInstance().context)
    }

    @Test
    fun selfReporter() {
        assertThat(serviceContextFacade.selfReporter)
            .isEqualTo(moduleSelfReporterImplMockedRule.constructionMock.constructed()[0])
        assertThat(moduleSelfReporterImplMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(moduleSelfReporterImplMockedRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun advertisingIdGetter() {
        assertThat(serviceContextFacade.advertisingIdGetter)
            .isEqualTo(GlobalServiceLocator.getInstance().serviceInternalAdvertisingIdGetter)
    }

    @Test
    fun `advertisingIdGetter twice`() {
        val first = serviceContextFacade.advertisingIdGetter
        whenever(GlobalServiceLocator.getInstance().serviceInternalAdvertisingIdGetter)
            .thenReturn(mock())
        val second = serviceContextFacade.advertisingIdGetter
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun serviceWakeLock() {
        assertThat(serviceContextFacade.serviceWakeLock)
            .isEqualTo(serviceWakeLockImplMockedRule.constructionMock.constructed()[0])
        assertThat(serviceWakeLockImplMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(serviceWakeLockImplMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                GlobalServiceLocator.getInstance().context,
                serviceWakeLockBinderMockedRule.constructionMock.constructed()[0]
            )
        assertThat(serviceWakeLockBinderMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(serviceWakeLockBinderMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(appMetricaServiceWakeLockIntentProviderMockedRule.constructionMock.constructed()[0])
        assertThat(appMetricaServiceWakeLockIntentProviderMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(appMetricaServiceWakeLockIntentProviderMockedRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun storageProvider() {
        assertThat(serviceContextFacade.storageProvider)
            .isEqualTo(storageProviderImplMockedRule.constructionMock.constructed()[0])
        assertThat(storageProviderImplMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(storageProviderImplMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                GlobalServiceLocator.getInstance().servicePreferences,
                storageForService
            )
    }

    @Test
    fun executorProvider() {
        assertThat(serviceContextFacade.executorProvider)
            .isEqualTo(executorProviderImplMockedRule.constructionMock.constructed()[0])
        assertThat(executorProviderImplMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(executorProviderImplMockedRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun locationServiceApi() {
        assertThat(serviceContextFacade.locationServiceApi)
            .isEqualTo(GlobalServiceLocator.getInstance().locationServiceApi)
    }

    @Test
    fun `locationServiceApi twice`() {
        val first = serviceContextFacade.locationServiceApi
        whenever(GlobalServiceLocator.getInstance().locationServiceApi).thenReturn(mock())
        val second = serviceContextFacade.locationServiceApi
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun applicationStateProvider() {
        assertThat(serviceContextFacade.applicationStateProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().applicationStateProvider)
    }

    @Test
    fun `applicationStateProvider twice`() {
        val first = serviceContextFacade.applicationStateProvider
        whenever(GlobalServiceLocator.getInstance().applicationStateProvider).thenReturn(mock())
        val second = serviceContextFacade.applicationStateProvider
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun chargeTypeProvider() {
        assertThat(serviceContextFacade.chargeTypeProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().batteryInfoProvider)
    }

    @Test
    fun `chargeTypeProvider twice`() {
        val first = serviceContextFacade.chargeTypeProvider
        whenever(GlobalServiceLocator.getInstance().batteryInfoProvider).thenReturn(mock())
        val second = serviceContextFacade.chargeTypeProvider
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun statisticsRestrictionController() {
        assertThat(serviceContextFacade.statisticsRestrictionController)
            .isEqualTo(GlobalServiceLocator.getInstance().statisticsRestrictionController)
    }

    @Test
    fun `statisticsRestrictionController twice`() {
        val first = serviceContextFacade.statisticsRestrictionController
        whenever(GlobalServiceLocator.getInstance().statisticsRestrictionController).thenReturn(mock())
        val second = serviceContextFacade.statisticsRestrictionController
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun localeProvider() {
        assertThat(serviceContextFacade.localeProvider).isEqualTo(localeHolder)
    }
}
