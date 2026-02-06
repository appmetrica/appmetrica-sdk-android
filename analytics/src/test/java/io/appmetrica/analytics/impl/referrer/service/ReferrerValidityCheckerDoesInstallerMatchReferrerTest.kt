package io.appmetrica.analytics.impl.referrer.service

import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
internal class ReferrerValidityCheckerDoesInstallerMatchReferrerTest(
    source: ReferrerInfo.Source,
    private val matchesGoogle: Boolean,
    private val matchesHuawei: Boolean
) : CommonTest() {

    private val packageManager: SafePackageManager = mock()
    private val selfReporter: IReporterExtended = mock()
    private val referrerInfo = ReferrerInfo("", 0, 0, source)

    @get:Rule
    val contextRule = ContextRule()
    val context by contextRule

    private val referrerValidityChecker by setUp { ReferrerValidityChecker(context, packageManager, selfReporter) }

    @Test
    fun googleInstaller() {
        whenever(packageManager.getInstallerPackageName(context, context.packageName))
            .thenReturn(GOOGLE_INSTALLER)
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isEqualTo(matchesGoogle)
    }

    @Test
    fun huaweiInstaller() {
        whenever(packageManager.getInstallerPackageName(context, context.packageName))
            .thenReturn(HUAWEI_INSTALLER)
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isEqualTo(matchesHuawei)
    }

    @Test
    fun strangeInstaller() {
        whenever(packageManager.getInstallerPackageName(context, context.packageName))
            .thenReturn("bad installer")
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isFalse()
    }

    @Test
    fun nullInstaller() {
        whenever(packageManager.getInstallerPackageName(context, context.packageName)).thenReturn(null)
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isFalse()
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(ReferrerInfo.Source.UNKNOWN, false, false),
            arrayOf(ReferrerInfo.Source.HMS, false, true),
            arrayOf(ReferrerInfo.Source.GP, true, false)
        )

        private const val GOOGLE_INSTALLER = "com.android.vending"
        private const val HUAWEI_INSTALLER = "com.huawei.appmarket"
    }
}
