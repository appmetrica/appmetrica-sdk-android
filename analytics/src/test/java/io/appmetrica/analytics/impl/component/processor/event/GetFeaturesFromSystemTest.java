package io.appmetrica.analytics.impl.component.processor.event;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.ContextRule;
import java.util.Random;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

// Robolectric needs for working with PackageInfo public fields
@SuppressLint({"NewApi", "RobolectricUsage"})
@RunWith(RobolectricTestRunner.class)
public class GetFeaturesFromSystemTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    private ComponentUnit mComponentUnit;
    private Context mContext;
    private PackageManager mManager;
    private PackageInfo mPackageInfo;

    @Before
    public void setUp() throws PackageManager.NameNotFoundException {
        mComponentUnit = mock(ComponentUnit.class);
        mContext = contextRule.getContext();
        mManager = mock(PackageManager.class);
        doReturn(mManager).when(mContext).getPackageManager();
        doReturn(mContext).when(mComponentUnit).getContext();
        mPackageInfo = new PackageInfo();
        String packageName = mContext.getPackageName();
        doReturn(mPackageInfo).when(mManager).getPackageInfo(eq(packageName), eq(PackageManager.GET_CONFIGURATIONS));
    }

    @Test
    public void testNoRequestedFeatures() {
        assertThat(new ReportFeaturesHandler(mComponentUnit).getFeaturesFromSystem()).isEmpty();
    }

    @Test
    public void testHasSomeFeatures() {
        Random random = new Random();
        ReportFeaturesHandler handler = new ReportFeaturesHandler(mComponentUnit);
        String name = UUID.randomUUID().toString();
        int version = random.nextInt(100) + 10;
        FeatureInfo feature = new FeatureInfo();
        feature.name = name;
        feature.version = version;
        boolean required = random.nextBoolean();
        if (required) {
            feature.flags |= FeatureInfo.FLAG_REQUIRED;
        }
        mPackageInfo.reqFeatures = new FeatureInfo[]{feature};
        assertThat(handler.getFeaturesFromSystem()).extracting("name", "version", "required")
            .containsOnly(
                Tuple.tuple(name, version, required)
            );
    }
}
