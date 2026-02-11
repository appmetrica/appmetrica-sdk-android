package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider
import io.appmetrica.analytics.impl.AutoCollectedDataSubscribersHolder
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter
import io.appmetrica.analytics.impl.request.CoreRequestConfig.CoreDataSource
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

@RunWith(Parameterized::class)
internal class ReportLoaderDefaultValuesTest(
    private val fieldName: String,
    private val value: Any
) : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule
    private val componentId: ComponentId = mock {
        on { `package` } doReturn ContextRule.PACKAGE_NAME
    }
    private val certificatesFingerprintsProvider: CertificatesFingerprintsProvider = mock()
    private val vitalComponentDataProvider: VitalComponentDataProvider = mock()

    private val autoCollectedDataSubscribersHolder = mock<AutoCollectedDataSubscribersHolder> {
        on { getSubscribers() } doReturn emptySet()
    }

    private val componentUnit: ComponentUnit by setUp {
        mock {
            on { context } doReturn context
            on { componentId } doReturn componentId
            on { certificatesFingerprintsProvider } doReturn certificatesFingerprintsProvider
            on { vitalComponentDataProvider } doReturn vitalComponentDataProvider
            on { autoCollectedDataSubscribersHolder } doReturn autoCollectedDataSubscribersHolder
        }
    }

    private val sdkEnvironmentProvider: SdkEnvironmentProvider = mock()

    private val appSetId = AppSetId(UUID.randomUUID().toString(), AppSetIdScope.DEVELOPER)

    private val appSetIdProvider: AppSetIdProvider = mock {
        on { getAppSetId() } doReturn appSetId
    }

    private val advertisingIdGetter: AdvertisingIdGetter = mock()

    private val platformIdentifiers: PlatformIdentifiers = mock {
        on { appSetIdProvider } doReturn appSetIdProvider
        on { advIdentifiersProvider } doReturn advertisingIdGetter
    }

    private val loader: ReportRequestConfig.Loader by setUp { ReportRequestConfig.Loader(componentUnit, mock()) }

    private val dataSource: CoreDataSource<ReportRequestConfig.Arguments> by setUp {
        CoreDataSource(
            TestUtils.createDefaultStartupState(),
            sdkEnvironmentProvider,
            platformIdentifiers,
            ReportRequestConfig.Arguments(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        )
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().clidsStorage.updateAndRetrieveData(any()))
            .thenReturn(ClidsInfo.Candidate(null, DistributionSource.APP))
    }

    @Test
    fun load() {
        assertThat(loader.load(dataSource)).extracting(fieldName).isEqualTo(value)
    }

    companion object {

        @Parameterized.Parameters(name = "{0} should be equal {1}")
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("locationTracking", BuildConfig.DEFAULT_LOCATION_COLLECTING),
            arrayOf("firstActivationAsUpdate", false),
            arrayOf("sessionTimeout", 10),
            arrayOf("maxReportsCount", 7),
            arrayOf("dispatchPeriod", 90),
            arrayOf("logEnabled", false)
        )
    }
}
