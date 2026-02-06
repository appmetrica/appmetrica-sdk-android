package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import android.content.pm.PackageInfo
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert
import java.util.concurrent.TimeUnit

internal class ReferrerValidityCheckerGeneralTest : CommonTest() {
    private val maxAllowedDeltaSeconds = TimeUnit.DAYS.toSeconds(1)

    @get:Rule
    val contextRule = ContextRule()
    private val context: Context by contextRule

    private val packageManager: SafePackageManager = mock()
    private val selfReporter: IReporterExtended = mock()
    private val installTimeSeconds = 111222333444L
    private val packageInfo = PackageInfo().apply {
        firstInstallTime = installTimeSeconds * 1000
    }
    private val referrerValidityChecker: ReferrerValidityChecker by setUp {
        ReferrerValidityChecker(context, packageManager, selfReporter)
    }

    @Test
    fun hasReferrerNullInfo() {
        assertThat(referrerValidityChecker.hasReferrer(null)).isFalse()
    }

    @Test
    fun hasReferrerEmptyReferrer() {
        assertThat(referrerValidityChecker.hasReferrer(ReferrerInfo("", 10, 20, ReferrerInfo.Source.HMS)))
            .isFalse()
    }

    @Test
    fun hasReferrerFilledReferrer() {
        assertThat(
            referrerValidityChecker.hasReferrer(
                ReferrerInfo("referrer", 10, 20, ReferrerInfo.Source.HMS)
            )
        ).isTrue()
    }

    @Test
    fun doesInstallerMatchReferrerNullInfo() {
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(null)).isFalse()
        Mockito.verifyNoMoreInteractions(packageManager)
    }

    @Test
    fun chooseReferrerFromEmptyList() {
        assertThat(referrerValidityChecker.chooseReferrerFromValid(ArrayList())).isNull()
    }

    @Test
    fun chooseReferrerFromFilledNullPackageInfoGoogleTimeIsGreater() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(null)
        val google = ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, 19, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledNullPackageInfoHuaweiTimeIsGreater() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(null)
        val google = ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, 21, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(huawei)
    }

    @Test
    fun chooseReferrerFromFilledNullPackageInfoSecondTimeIsGreater() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(null)
        val firstReferrer = ReferrerInfo("referrer", 0, 19, ReferrerInfo.Source.GP)
        val secondReferrer = ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(firstReferrer, secondReferrer)))
            .isSameAs(secondReferrer)
    }

    @Test
    fun chooseReferrerFromFilledNullPackageInfoTimesAreEqual() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(null)
        val google = ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledNullPackageInfoEventIsSent() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(null)
        val google = ReferrerInfo("google_referrer", 10, 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("huawei_referrer", 11, 20, ReferrerInfo.Source.HMS)
        referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))
        val eventValueCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(selfReporter).reportEvent(eq("several_filled_referrers"), eventValueCaptor.capture())
        JSONAssert.assertEquals(
            JSONObject()
                .put(
                    "candidates",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put("referrer", "google_referrer")
                                .put("install_timestamp_seconds", 20)
                                .put("click_timestamp_seconds", 10)
                                .put("source", "gpl")
                        )
                        .put(
                            JSONObject()
                                .put("referrer", "huawei_referrer")
                                .put("install_timestamp_seconds", 20)
                                .put("click_timestamp_seconds", 11)
                                .put("source", "hms-content-provider")
                        )
                )
                .put(
                    "chosen",
                    JSONObject()
                        .put("referrer", "google_referrer")
                        .put("install_timestamp_seconds", 20)
                        .put("click_timestamp_seconds", 10)
                        .put("source", "gpl")
                )
                .toString(),
            eventValueCaptor.getValue(),
            true
        )
    }

    @Test
    fun chooseReferrerFromFilledBothDiffsAreValidAndGreaterGoogleIsCloser() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, installTimeSeconds + 21, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledBothDiffsAreValidAndGreaterSecondIsCloser() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val firstReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 21, ReferrerInfo.Source.HMS)
        val secondReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(firstReferrer, secondReferrer)))
            .isSameAs(secondReferrer)
    }

    @Test
    fun chooseReferrerFromFilledBothDiffsAreValidAndLessGoogleIsCloser() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, installTimeSeconds - 21, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledBothDiffsAreValidAndLessFirstIsCloser() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val firstReferrer = ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP)
        val secondReferrer = ReferrerInfo("referrer", 0, installTimeSeconds - 21, ReferrerInfo.Source.GP)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(firstReferrer, secondReferrer)))
            .isSameAs(firstReferrer)
    }

    @Test
    fun chooseReferrerFromFilledBothDiffsAreValidAndGreaterHuaweiIsCloser() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, installTimeSeconds + 19, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(huawei)
    }

    @Test
    fun chooseReferrerFromFilledBothDiffsAreValidAndLessHuaweiIsCloser() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, installTimeSeconds - 19, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(huawei)
    }

    @Test
    fun chooseReferrerFromFilledSameValidDiffGreaterThanActual() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledSameValidDiffLessThanActual() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP)
        val huawei = ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledSameInvalidDiffGreaterThanActual() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP)
        val huawei =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledSameInvalidDiffLessThanActual() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google =
            ReferrerInfo("referrer", 0, installTimeSeconds - maxAllowedDeltaSeconds - 20, ReferrerInfo.Source.GP)
        val huawei =
            ReferrerInfo("referrer", 0, installTimeSeconds - maxAllowedDeltaSeconds - 20, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledInvalidDiffGoogleIsGreater() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP)
        val huawei =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 19, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(google)
    }

    @Test
    fun chooseReferrerFromFilledInvalidDiffHuaweiIsGreater() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP)
        val huawei =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 21, ReferrerInfo.Source.HMS)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))).isSameAs(huawei)
    }

    @Test
    fun chooseReferrerFromFilledInvalidDiffSecondIsGreater() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val firstReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 19, ReferrerInfo.Source.GP)
        val secondReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP)
        assertThat(referrerValidityChecker.chooseReferrerFromValid(listOf(firstReferrer, secondReferrer)))
            .isSameAs(secondReferrer)
    }

    @Test
    fun chooseReferrerFromFilledEmptyListEventIsNotSent() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        referrerValidityChecker.chooseReferrerFromValid(listOf())
        verifyNoInteractions(selfReporter)
    }

    @Test
    fun chooseReferrerFromFilledSingleElementEventIsNotSent() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo(
            "google_referrer",
            10,
            installTimeSeconds + maxAllowedDeltaSeconds + 20,
            ReferrerInfo.Source.GP
        )
        referrerValidityChecker.chooseReferrerFromValid(mutableListOf(google))
        verifyNoInteractions(selfReporter)
    }

    @Test
    @Throws(JSONException::class)
    fun chooseReferrerFromFilledHasPackageInfoEventIsSent() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val google = ReferrerInfo(
            "google_referrer",
            10,
            installTimeSeconds + maxAllowedDeltaSeconds + 20,
            ReferrerInfo.Source.GP
        )
        val huawei = ReferrerInfo(
            "huawei_referrer",
            11,
            installTimeSeconds + maxAllowedDeltaSeconds + 21,
            ReferrerInfo.Source.HMS
        )
        referrerValidityChecker.chooseReferrerFromValid(listOf(google, huawei))
        val eventValueCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(selfReporter)
            .reportEvent(eq("several_filled_referrers"), eventValueCaptor.capture())
        JSONAssert.assertEquals(
            JSONObject()
                .put(
                    "candidates",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put("referrer", "google_referrer")
                                .put("install_timestamp_seconds", installTimeSeconds + maxAllowedDeltaSeconds + 20)
                                .put("click_timestamp_seconds", 10)
                                .put("source", "gpl")
                        )
                        .put(
                            JSONObject()
                                .put("referrer", "huawei_referrer")
                                .put("install_timestamp_seconds", installTimeSeconds + maxAllowedDeltaSeconds + 21)
                                .put("click_timestamp_seconds", 11)
                                .put("source", "hms-content-provider")
                        )
                )
                .put(
                    "chosen",
                    JSONObject()
                        .put("referrer", "huawei_referrer")
                        .put("install_timestamp_seconds", installTimeSeconds + maxAllowedDeltaSeconds + 21)
                        .put("click_timestamp_seconds", 11)
                        .put("source", "hms-content-provider")
                )
                .put("install_time", installTimeSeconds * 1000)
                .toString(),
            eventValueCaptor.getValue(),
            true
        )
    }

    @Test
    fun chooseReferrerFromFilledManyElementsNullPackageInfo() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(null)
        val firstReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP)
        val secondReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 21, ReferrerInfo.Source.HMS)
        val thirdReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 19, ReferrerInfo.Source.HMS)
        val fourthReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 18, ReferrerInfo.Source.HMS)
        val fifthReferrer =
            ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 17, ReferrerInfo.Source.HMS)
        assertThat(
            referrerValidityChecker.chooseReferrerFromValid(
                listOf(
                    firstReferrer,
                    secondReferrer,
                    thirdReferrer,
                    fourthReferrer,
                    fifthReferrer
                )
            )
        ).isSameAs(secondReferrer)
    }

    @Test
    fun chooseReferrerFromFilledManyElementsHasPackageInfo() {
        whenever(packageManager.getPackageInfo(context, context.packageName, 0)).thenReturn(packageInfo)
        val firstReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP)
        val secondReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 21, ReferrerInfo.Source.HMS)
        val thirdReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 19, ReferrerInfo.Source.HMS)
        val fourthReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 18, ReferrerInfo.Source.GP)
        val fifthReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 18, ReferrerInfo.Source.HMS)
        val sixthReferrer = ReferrerInfo("referrer", 0, installTimeSeconds + 19, ReferrerInfo.Source.GP)
        assertThat(
            referrerValidityChecker.chooseReferrerFromValid(
                listOf(
                    firstReferrer,
                    secondReferrer,
                    thirdReferrer,
                    fourthReferrer,
                    fifthReferrer,
                    sixthReferrer
                )
            )
        ).isSameAs(fourthReferrer)
    }
}
