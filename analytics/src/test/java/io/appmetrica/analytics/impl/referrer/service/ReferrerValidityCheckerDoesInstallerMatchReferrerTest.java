package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ReferrerValidityCheckerDoesInstallerMatchReferrerTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {ReferrerInfo.Source.UNKNOWN, false, false},
            {ReferrerInfo.Source.HMS, false, true},
            {ReferrerInfo.Source.GP, true, false},
        });
    }

    private static final String GOOGLE_INSTALLER = "com.android.vending";
    private static final String HUAWEI_INSTALLER = "com.huawei.appmarket";

    private Context context;
    @Mock
    private SafePackageManager packageManager;
    @Mock
    private IReporterExtended selfReporter;
    private final String packageName = "ru.yandex.test";
    @NonNull
    private final ReferrerInfo referrerInfo;
    private final boolean matchesGoogle;
    private final boolean machesHuawei;
    private ReferrerValidityChecker referrerValidityChecker;

    public ReferrerValidityCheckerDoesInstallerMatchReferrerTest(@NonNull ReferrerInfo.Source source,
                                                                 boolean matchesGoogle,
                                                                 boolean matchesHuawei) {
        this.referrerInfo = new ReferrerInfo(null, 0, 0, source);
        this.matchesGoogle = matchesGoogle;
        this.machesHuawei = matchesHuawei;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(context.getPackageName()).thenReturn(packageName);
        referrerValidityChecker = new ReferrerValidityChecker(context, packageManager, selfReporter);
    }

    @Test
    public void googleInstaller() {
        when(packageManager.getInstallerPackageName(context, packageName)).thenReturn(GOOGLE_INSTALLER);
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isEqualTo(matchesGoogle);
    }

    @Test
    public void huaweiInstaller() {
        when(packageManager.getInstallerPackageName(context, packageName)).thenReturn(HUAWEI_INSTALLER);
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isEqualTo(machesHuawei);
    }

    @Test
    public void strangeInstaller() {
        when(packageManager.getInstallerPackageName(context, packageName)).thenReturn("bad installer");
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isFalse();
    }

    @Test
    public void nullInstaller() {
        when(packageManager.getInstallerPackageName(context, packageName)).thenReturn(null);
        assertThat(referrerValidityChecker.doesInstallerMatchReferrer(referrerInfo)).isFalse();
    }
}
