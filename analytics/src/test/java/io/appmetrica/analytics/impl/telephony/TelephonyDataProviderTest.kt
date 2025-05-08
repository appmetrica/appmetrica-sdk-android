package io.appmetrica.analytics.impl.telephony

import android.content.Context
import android.content.pm.PackageManager
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TelephonyDataProviderTest : CommonTest() {

    private val context = mock<Context>()
    private val contextWithoutTelephonyFeature = mock<Context>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val baseTelephonyInfoAdapterApplierMockedRule =
        MockedConstructionRule(BaseTelephonyInfoAdapterApplier::class.java)

    @get:Rule
    val dummyTelephonyInfoAdapterApplierMockedRule =
        MockedConstructionRule(DummyTelephonyInfoAdapterApplier::class.java)

    @get:Rule
    val simInfoExtractorMockedConstructionRule = MockedConstructionRule(SimInfoExtractor::class.java)

    @get:Rule
    val mobileConnectionDescriptionExtractorMockedRule =
        MockedConstructionRule(MobileConnectionDescriptionExtractor::class.java)

    @get:Rule
    val safePackageManagerMockedConstructionRule = MockedConstructionRule(SafePackageManager::class.java) { mock, _ ->
        whenever(mock.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)).thenReturn(true)
        whenever(mock.hasSystemFeature(contextWithoutTelephonyFeature, PackageManager.FEATURE_TELEPHONY))
            .thenReturn(false)
    }

    private val simInfoAdapter = mock<TelephonyInfoAdapter<List<SimInfo>>>()
    private val mobileConnectionDescriptionAdapter = mock<TelephonyInfoAdapter<MobileConnectionDescription>>()

    private lateinit var telephonyDataProvider: TelephonyDataProvider

    private lateinit var simInfoApplier: TelephonyInfoAdapterApplier<List<SimInfo>>
    private lateinit var mobileConnectionDescriptionApplier: TelephonyInfoAdapterApplier<MobileConnectionDescription>

    private lateinit var simInfoDummyApplier: DummyTelephonyInfoAdapterApplier<List<SimInfo>>
    private lateinit var mobileConnectionDescriptionDummyApplier:
        DummyTelephonyInfoAdapterApplier<MobileConnectionDescription>

    @Test
    fun checkSafePackageManager() {
        telephonyDataProvider = TelephonyDataProvider(contextWithoutTelephonyFeature)
        assertThat(safePackageManagerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(safePackageManagerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `adoptSimInfo without telephony feature`() {
        telephonyDataProvider = TelephonyDataProvider(contextWithoutTelephonyFeature)
        initDummyAppliers()
        telephonyDataProvider.adoptSimInfo(simInfoAdapter)
        verify(simInfoDummyApplier).applyAdapter(simInfoAdapter)
    }

    @Test
    fun `adoptSimInfo with telephony feature`() {
        telephonyDataProvider = TelephonyDataProvider(context)
        initAppliers()
        telephonyDataProvider.adoptSimInfo(simInfoAdapter)
        verify(simInfoApplier).applyAdapter(simInfoAdapter)
    }

    @Test
    fun `adoptMobileConnectionDescription without telephony feature`() {
        telephonyDataProvider = TelephonyDataProvider(contextWithoutTelephonyFeature)
        initDummyAppliers()
        telephonyDataProvider.adoptMobileConnectionDescription(mobileConnectionDescriptionAdapter)
        verify(mobileConnectionDescriptionDummyApplier).applyAdapter(mobileConnectionDescriptionAdapter)
    }

    @Test
    fun `adoptMobileConnectionDescription with telephony feature`() {
        telephonyDataProvider = TelephonyDataProvider(context)
        initAppliers()
        telephonyDataProvider.adoptMobileConnectionDescription(mobileConnectionDescriptionAdapter)
        verify(mobileConnectionDescriptionApplier).applyAdapter(mobileConnectionDescriptionAdapter)
    }

    private fun initAppliers() {
        val constructed = baseTelephonyInfoAdapterApplierMockedRule.constructionMock.constructed()
        assertThat(constructed).hasSize(2)
        assertThat(baseTelephonyInfoAdapterApplierMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                simInfoExtractor(),
                mobileConnectionDescriptionExtractor()
            )
        simInfoApplier = constructed[0] as TelephonyInfoAdapterApplier<List<SimInfo>>
        mobileConnectionDescriptionApplier = constructed[1] as TelephonyInfoAdapterApplier<MobileConnectionDescription>
    }

    private fun initDummyAppliers() {
        val constructed = dummyTelephonyInfoAdapterApplierMockedRule.constructionMock.constructed()
        assertThat(constructed).hasSize(2)
        assertThat(dummyTelephonyInfoAdapterApplierMockedRule.argumentInterceptor.flatArguments()).isEmpty()
        simInfoDummyApplier = constructed[0] as DummyTelephonyInfoAdapterApplier<List<SimInfo>>
        mobileConnectionDescriptionDummyApplier =
            constructed[1] as DummyTelephonyInfoAdapterApplier<MobileConnectionDescription>
    }

    private fun simInfoExtractor(): SimInfoExtractor {
        assertThat(simInfoExtractorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(simInfoExtractorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
        return simInfoExtractorMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun mobileConnectionDescriptionExtractor(): MobileConnectionDescriptionExtractor {
        assertThat(mobileConnectionDescriptionExtractorMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(mobileConnectionDescriptionExtractorMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
        return mobileConnectionDescriptionExtractorMockedRule.constructionMock.constructed().first()
    }
}
