package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReferrerValidityCheckerGeneralTest extends CommonTest {

    private final long maxAllowedDeltaSeconds = TimeUnit.DAYS.toSeconds(1);
    private Context context;
    @Mock
    private SafePackageManager packageManager;
    @Mock
    private IReporterExtended selfReporter;
    private final String packageName = "ru.yandex.test";
    private final long installTimeSeconds = 111222333444L;
    private final PackageInfo packageInfo = new PackageInfo();
    private ReferrerValidityChecker referrerValidityChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(context.getPackageName()).thenReturn(packageName);
        packageInfo.firstInstallTime = installTimeSeconds * 1000;
        referrerValidityChecker = new ReferrerValidityChecker(context, packageManager, selfReporter);
    }

    @Test
    public void hasReferrerNullInfo() {
        assertThat(referrerValidityChecker.hasReferrer(null)).isFalse();
    }

    @Test
    public void hasReferrerNullReferrer() {
        assertThat(referrerValidityChecker.hasReferrer(new ReferrerInfo(null, 10, 20, ReferrerInfo.Source.HMS))).isFalse();
    }

    @Test
    public void hasReferrerEmptyReferrer() {
        assertThat(referrerValidityChecker.hasReferrer(new ReferrerInfo("", 10, 20, ReferrerInfo.Source.HMS))).isFalse();
    }

    @Test
    public void hasReferrerFilledReferrer() {
        assertThat(referrerValidityChecker.hasReferrer(
            new ReferrerInfo("referrer", 10, 20, ReferrerInfo.Source.HMS)
        )).isTrue();
    }

    @Test
    public void doesInstallerMatchReferrerNullInfo() {
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(null)).isFalse();
        verifyNoMoreInteractions(packageManager);
    }

    @Test
    public void chooseReferrerFromEmptyList() {
        assertThat(referrerValidityChecker.chooseReferrerFromValid(new ArrayList<ReferrerInfo>())).isNull();
    }

    @Test
    public void chooseReferrerFromFilledNullPackageInfoGoogleTimeIsGreater() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(null);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, 19, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledNullPackageInfoHuaweiTimeIsGreater() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(null);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, 21, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(huawei);
    }

    @Test
    public void chooseReferrerFromFilledNullPackageInfoSecondTimeIsGreater() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(null);
        ReferrerInfo firstReferrer = new ReferrerInfo("referrer", 0, 19, ReferrerInfo.Source.GP);
        ReferrerInfo secondReferrer = new ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(firstReferrer, secondReferrer))).isSameAs(secondReferrer);
    }

    @Test
    public void chooseReferrerFromFilledNullPackageInfoTimesAreEqual() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(null);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, 20, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledNullPackageInfoEventIsSent() throws JSONException {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(null);
        ReferrerInfo google = new ReferrerInfo("google_referrer", 10, 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("huawei_referrer", 11, 20, ReferrerInfo.Source.HMS);
        referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei));
        ArgumentCaptor<String> eventValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(selfReporter).reportEvent(eq("several_filled_referrers"), eventValueCaptor.capture());
        JSONAssert.assertEquals(
            new JSONObject()
                .put("candidates", new JSONArray()
                    .put(new JSONObject()
                        .put("referrer", "google_referrer")
                        .put("install_timestamp_seconds", 20)
                        .put("click_timestamp_seconds", 10)
                        .put("source", "gpl")
                    )
                    .put(new JSONObject()
                        .put("referrer", "huawei_referrer")
                        .put("install_timestamp_seconds", 20)
                        .put("click_timestamp_seconds", 11)
                        .put("source", "hms-content-provider")
                    )
                )
                .put("chosen", new JSONObject()
                    .put("referrer", "google_referrer")
                    .put("install_timestamp_seconds", 20)
                    .put("click_timestamp_seconds", 10)
                    .put("source", "gpl")
                )
                .toString(),
            eventValueCaptor.getValue(),
            true
        );
    }

    @Test
    public void chooseReferrerFromFilledBothDiffsAreValidAndGreaterGoogleIsCloser() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds + 21, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledBothDiffsAreValidAndGreaterSecondIsCloser() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo firstReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 21, ReferrerInfo.Source.HMS);
        ReferrerInfo secondReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(firstReferrer, secondReferrer))).isSameAs(secondReferrer);
    }

    @Test
    public void chooseReferrerFromFilledBothDiffsAreValidAndLessGoogleIsCloser() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds - 21, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledBothDiffsAreValidAndLessFirstIsCloser() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo firstReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP);
        ReferrerInfo secondReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds - 21, ReferrerInfo.Source.GP);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(firstReferrer, secondReferrer))).isSameAs(firstReferrer);
    }

    @Test
    public void chooseReferrerFromFilledBothDiffsAreValidAndGreaterHuaweiIsCloser() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds + 19, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(huawei);
    }

    @Test
    public void chooseReferrerFromFilledBothDiffsAreValidAndLessHuaweiIsCloser() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds - 19, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(huawei);
    }

    @Test
    public void chooseReferrerFromFilledSameValidDiffGreaterThanActual() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledSameValidDiffLessThanActual() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds - 20, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledSameInvalidDiffGreaterThanActual() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledSameInvalidDiffLessThanActual() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds - maxAllowedDeltaSeconds - 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds - maxAllowedDeltaSeconds - 20, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledInvalidDiffGoogleIsGreater() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 19, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(google);
    }

    @Test
    public void chooseReferrerFromFilledInvalidDiffHuaweiIsGreater() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 21, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei))).isSameAs(huawei);
    }

    @Test
    public void chooseReferrerFromFilledInvalidDiffSecondIsGreater() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo firstReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 19, ReferrerInfo.Source.GP);
        ReferrerInfo secondReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(firstReferrer, secondReferrer))).isSameAs(secondReferrer);
    }

    @Test
    public void chooseReferrerFromFilledEmptyListEventIsNotSent() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        referrerValidityChecker.chooseReferrerFromValid(new ArrayList<ReferrerInfo>());
        verifyNoInteractions(selfReporter);
    }

    @Test
    public void chooseReferrerFromFilledSingleElementEventIsNotSent() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("google_referrer", 10, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        referrerValidityChecker.chooseReferrerFromValid(Collections.singletonList(google));
        verifyNoInteractions(selfReporter);
    }

    @Test
    public void chooseReferrerFromFilledHasPackageInfoEventIsSent() throws JSONException {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo google = new ReferrerInfo("google_referrer", 10, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo huawei = new ReferrerInfo("huawei_referrer", 11, installTimeSeconds + maxAllowedDeltaSeconds + 21, ReferrerInfo.Source.HMS);
        referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(google, huawei));
        ArgumentCaptor<String> eventValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(selfReporter).reportEvent(eq("several_filled_referrers"), eventValueCaptor.capture());
        JSONAssert.assertEquals(
            new JSONObject()
                .put("candidates", new JSONArray()
                    .put(new JSONObject()
                        .put("referrer", "google_referrer")
                        .put("install_timestamp_seconds", installTimeSeconds + maxAllowedDeltaSeconds + 20)
                        .put("click_timestamp_seconds", 10)
                        .put("source", "gpl")
                    )
                    .put(new JSONObject()
                        .put("referrer", "huawei_referrer")
                        .put("install_timestamp_seconds", installTimeSeconds + maxAllowedDeltaSeconds + 21)
                        .put("click_timestamp_seconds", 11)
                        .put("source", "hms-content-provider")
                    )
                )
                .put("chosen", new JSONObject()
                    .put("referrer", "huawei_referrer")
                    .put("install_timestamp_seconds", installTimeSeconds + maxAllowedDeltaSeconds + 21)
                    .put("click_timestamp_seconds", 11)
                    .put("source", "hms-content-provider")
                )
                .put("install_time", installTimeSeconds * 1000)
                .toString(),
            eventValueCaptor.getValue(),
            true
        );
    }

    @Test
    public void chooseReferrerFromFilledManyElementsNullPackageInfo() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(null);
        ReferrerInfo firstReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo secondReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 21, ReferrerInfo.Source.HMS);
        ReferrerInfo thirdReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 19, ReferrerInfo.Source.HMS);
        ReferrerInfo fourthReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 18, ReferrerInfo.Source.HMS);
        ReferrerInfo fifthReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + maxAllowedDeltaSeconds + 17, ReferrerInfo.Source.HMS);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(
            firstReferrer,
            secondReferrer,
            thirdReferrer,
            fourthReferrer,
            fifthReferrer
        ))).isSameAs(secondReferrer);
    }

    @Test
    public void chooseReferrerFromFilledManyElementsHasPackageInfo() {
        when(packageManager.getPackageInfo(context, packageName, 0)).thenReturn(packageInfo);
        ReferrerInfo firstReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 20, ReferrerInfo.Source.GP);
        ReferrerInfo secondReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 21, ReferrerInfo.Source.HMS);
        ReferrerInfo thirdReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 19, ReferrerInfo.Source.HMS);
        ReferrerInfo fourthReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 18, ReferrerInfo.Source.GP);
        ReferrerInfo fifthReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 18, ReferrerInfo.Source.HMS);
        ReferrerInfo sixthReferrer = new ReferrerInfo("referrer", 0, installTimeSeconds + 19, ReferrerInfo.Source.GP);
        assertThat(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(
            firstReferrer,
            secondReferrer,
            thirdReferrer,
            fourthReferrer,
            fifthReferrer,
            sixthReferrer
        ))).isSameAs(fourthReferrer);
    }
}
