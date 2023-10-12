package io.appmetrica.analytics.impl.location.stub

import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions

class LocationApiStubTest : CommonTest() {

    private val wakelock = mock<Any>()
    private val locationControllerObserver = mock<LocationControllerObserver>()
    private val lastKnownLocationExtractorProvider = mock<LastKnownExtractorProviderStub>()
    private val locationReceiverProvider = mock<LocationReceiverProviderStub>()
    private val locationFilter = mock<LocationFilter>()

    private lateinit var locationApiStub: LocationApiStub

    @get:Rule
    val permissionExtractorStubMockedConstructionRule = MockedConstructionRule(PermissionExtractorStub::class.java)

    @get:Rule
    val lastKnownExtractorProviderFactoryStubMockedConstructionRule =
        MockedConstructionRule(LastKnownExtractorProviderFactoryStub::class.java)

    @get:Rule
    val locationReceiverProviderFactoryStubMockedConstructionRule =
        MockedConstructionRule(LocationReceiverProviderFactoryStub::class.java)

    @Before
    fun setUp() {
        locationApiStub = LocationApiStub()
    }

    @Test
    fun init() {
        locationApiStub.init()
    }

    @Test
    fun getLocation() {
        assertThat(locationApiStub.getLocation()).isNull()
    }

    @Test
    fun registerWakelock() {
        locationApiStub.registerWakelock(wakelock)
        verifyNoMoreInteractions(wakelock)
    }

    @Test
    fun removeWakelock() {
        locationApiStub.removeWakelock(wakelock)
        verifyNoMoreInteractions(wakelock)
    }

    @Test
    fun updateTrackingStatusFromClient() {
        locationApiStub.updateTrackingStatusFromClient(true)
    }

    @Test
    fun registerControllerObserver() {
        locationApiStub.registerControllerObserver(locationControllerObserver)
        verifyNoMoreInteractions(locationControllerObserver)
    }

    @Test
    fun `registerSource for last known`() {
        locationApiStub.registerSource(lastKnownLocationExtractorProvider)
        verifyNoMoreInteractions(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `unregisterSource for last known`() {
        locationApiStub.unregisterSource(lastKnownLocationExtractorProvider)
        verifyNoMoreInteractions(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `registerSource for receiver`() {
        locationApiStub.registerSource(locationReceiverProvider)
        verifyNoMoreInteractions(locationReceiverProvider)
    }

    @Test
    fun `unregisterSource for receiver`() {
        locationApiStub.unregisterSource(locationReceiverProvider)
        verifyNoMoreInteractions(locationReceiverProvider)
    }

    @Test
    fun permissionExtractor() {
        assertThat(locationApiStub.permissionExtractor)
            .isEqualTo(permissionExtractorStubMockedConstructionRule.constructionMock.constructed().first())
        assertThat(permissionExtractorStubMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(permissionExtractorStubMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun lastKnownExtractorProviderFactory() {
        assertThat(locationApiStub.lastKnownExtractorProviderFactory)
            .isEqualTo(
                lastKnownExtractorProviderFactoryStubMockedConstructionRule.constructionMock.constructed().first()
            )
        assertThat(lastKnownExtractorProviderFactoryStubMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(lastKnownExtractorProviderFactoryStubMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun locationReceiverProviderFactory() {
        assertThat(locationApiStub.locationReceiverProviderFactory)
            .isEqualTo(locationReceiverProviderFactoryStubMockedConstructionRule.constructionMock.constructed().first())
        assertThat(locationReceiverProviderFactoryStubMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(locationReceiverProviderFactoryStubMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun updateLocationFilter() {
        locationApiStub.updateLocationFilter(locationFilter)
        verifyNoMoreInteractions(locationFilter)
    }
}
