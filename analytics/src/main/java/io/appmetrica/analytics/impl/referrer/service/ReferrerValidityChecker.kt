package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import android.content.pm.PackageInfo
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.MapWithDefault
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sign

internal class ReferrerValidityChecker(
    private val context: Context,
    private val packageManager: SafePackageManager = SafePackageManager(),
    private val selfReporter: IReporterExtended = AppMetricaSelfReportFacade.getReporter()
) {

    private val sourcePriorities = MapWithDefault<ReferrerInfo.Source, Int>(0).apply {
        put(ReferrerInfo.Source.HMS, 1)
        put(ReferrerInfo.Source.GP, 2)
    }

    private val tag = "[ReferrerValidityChecker]"
    private val maxInstallTimeDiffDeltaSeconds = TimeUnit.DAYS.toSeconds(1)
    private val googleInstaller = "com.android.vending"
    private val huaweiInstaller = "com.huawei.appmarket"

    fun doesInstallerMatchReferrer(referrerInfo: ReferrerInfo?): Boolean {
        DebugLogger.info(tag, "Checking if %s matches package installer", referrerInfo)
        if (referrerInfo == null) {
            DebugLogger.info(tag, "Referrer is null")
            return false
        }
        val packageInstaller = packageManager.getInstallerPackageName(context, context.packageName)
        val matches = when (referrerInfo.source) {
            ReferrerInfo.Source.GP -> googleInstaller == packageInstaller
            ReferrerInfo.Source.HMS -> huaweiInstaller == packageInstaller
            else -> false
        }
        DebugLogger.info(tag, "Package installer: %s, matches: %b", packageInstaller, matches)
        return matches
    }

    fun chooseReferrerFromValid(referrers: List<ReferrerInfo>): ReferrerInfo? {
        if (referrers.isEmpty()) {
            return null
        }
        if (referrers.size == 1) {
            return referrers[0]
        }
        var chosenState: ReferrerInfo? = null
        DebugLogger.info(tag, "Choosing from %s by install time", referrers)
        val packageInfo = packageManager.getPackageInfo(context, context.packageName, 0)
        if (packageInfo != null) {
            val installTime = TimeUnit.MILLISECONDS.toSeconds(packageInfo.firstInstallTime)
            val (referrerWithMinDiff, minDiff) = referrers.minOfWith(compareBy { it.second }) {
                it to abs(it.installBeginTimestampSeconds - installTime)
            }
            if (minDiff < maxInstallTimeDiffDeltaSeconds) {
                chosenState = referrerWithMinDiff
            }
        }
        if (chosenState == null) {
            DebugLogger.info(tag, "Choose referrer with latest install timestamp. Candidates: %s", referrers)
            val referrerWithMaxTimestamp = referrers.maxOfWith({ first, second ->
                val timestampDiff = (first.installBeginTimestampSeconds - second.installBeginTimestampSeconds).sign
                if (timestampDiff == 0) {
                    sourcePriorities[first.source] - sourcePriorities[second.source]
                } else timestampDiff
            }) { it }
            chosenState = referrerWithMaxTimestamp
        }
        selfReporter.reportEvent(
            "several_filled_referrers",
            createSeveralFilledReferrersEventValue(referrers, chosenState, packageInfo).toString()
        )
        return chosenState
    }

    fun hasReferrer(referrerInfo: ReferrerInfo?): Boolean = !referrerInfo?.installReferrer.isNullOrEmpty()

    private fun createSeveralFilledReferrersEventValue(
        referrers: List<ReferrerInfo>,
        chosenReferrer: ReferrerInfo,
        packageInfo: PackageInfo?
    ): JSONObject {
        return JSONObject()
            .put("candidates", JsonHelper.listToJson(referrers.map { referrerInfoToJson(it) }))
            .put("chosen", referrerInfoToJson(chosenReferrer))
            .putOpt("install_time", packageInfo?.firstInstallTime)
    }

    private fun referrerInfoToJson(referrerInfo: ReferrerInfo): JSONObject {
        return JSONObject()
            .put("referrer", referrerInfo.installReferrer)
            .put("install_timestamp_seconds", referrerInfo.installBeginTimestampSeconds)
            .put("click_timestamp_seconds", referrerInfo.referrerClickTimestampSeconds)
            .put("source", referrerInfo.source.value)
    }
}
