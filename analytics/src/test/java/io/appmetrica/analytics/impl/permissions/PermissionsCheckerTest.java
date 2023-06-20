package io.appmetrica.analytics.impl.permissions;

import android.os.Build;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PermissionsCheckerTest extends CommonTest {

    private int mSdkIntBackup;

    @Before
    public void setUp() {
        mSdkIntBackup = Build.VERSION.SDK_INT;
    }

    @After
    public void tearDown() {
        TestUtils.setSdkInt(mSdkIntBackup);
    }

    @Test
    public void testStaticRetriever() {
        TestUtils.setSdkInt(10);
        assertThat(new PermissionsChecker().createPermissionsRetriever(RuntimeEnvironment.getApplication())).isInstanceOf(StaticPermissionRetriever.class);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
    public void testRuntimeRetriever() {
        assertThat(new PermissionsChecker().createPermissionsRetriever(RuntimeEnvironment.getApplication())).isInstanceOf(RuntimePermissionsRetriever.class);
    }

}
