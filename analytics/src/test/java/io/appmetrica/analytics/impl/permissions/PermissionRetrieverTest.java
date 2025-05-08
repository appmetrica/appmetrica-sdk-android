package io.appmetrica.analytics.impl.permissions;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class PermissionRetrieverTest extends CommonTest {

    public static final String PACKAGE_NAME = "com.test.package";
    private Context mContext;
    private PackageManager mPackageManager;
    private PackageInfo mPackageInfo;

    @Before
    public void setUp() throws PackageManager.NameNotFoundException {
        mContext = TestUtils.createMockedContext();
        mPackageManager = mock(PackageManager.class);
        mPackageInfo = new PackageInfo();

        doReturn(mPackageInfo).when(mPackageManager).getPackageInfo(eq(PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS));

        doReturn(PACKAGE_NAME).when(mContext).getPackageName();
        doReturn(mPackageManager).when(mContext).getPackageManager();
    }

    @Test
    public void testPermissionsCheckerWithEqualPermissions() {
        PermissionsChecker checker = new PermissionsChecker();
        mPackageInfo.requestedPermissions = new String[]{
            "permissionA",
            "permissionB"
        };
        mPackageInfo.requestedPermissionsFlags = new int[]{
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0
        };
        List<PermissionState> result = checker.check(mContext, Arrays.asList(new PermissionState("permissionA", true), new PermissionState("permissionB", false)));
        assertThat(result).isNull();
    }

    @Test
    public void testPermissionsCheckerWithChangedState() {
        PermissionsChecker checker = new PermissionsChecker();
        mPackageInfo.requestedPermissions = new String[]{
            "permissionA",
            "permissionB"
        };
        mPackageInfo.requestedPermissionsFlags = new int[]{
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0
        };
        List<PermissionState> result = checker.check(mContext, Arrays.asList(new PermissionState("permissionA", true), new PermissionState("permissionB", true)));
        assertThat(result).extracting("name", "granted").contains(
            tuple("permissionA", true),
            tuple("permissionB", false)
        );
    }

    @Test
    public void testPermissionsCheckerWithChangedPermissionsList() {
        PermissionsChecker checker = new PermissionsChecker();
        mPackageInfo.requestedPermissions = new String[]{
            "permissionA",
            "permissionB",
            "permissionC"
        };
        mPackageInfo.requestedPermissionsFlags = new int[]{
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0
        };
        List<PermissionState> result = checker.check(mContext, Arrays.asList(new PermissionState("permissionA", true), new PermissionState("permissionB", true)));
        assertThat(result).extracting("name", "granted").contains(
            tuple("permissionA", true),
            tuple("permissionB", true),
            tuple("permissionC", false)
        );
    }

    @Test
    public void testRuntimePermissionRetriever() {
        mPackageInfo.requestedPermissions = new String[]{
            "permissionA",
            "permissionB",
            "permissionC",
            "permissionD"
        };
        mPackageInfo.requestedPermissionsFlags = new int[]{
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0,
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0
        };
        PermissionRetriever retriever = new RuntimePermissionsRetriever(mContext);
        List<PermissionState> permissions = retriever.getPermissionsState();
        assertThat(permissions).size().isEqualTo(4);
        assertThat(permissions).extracting("granted").contains(true, false, true, false);
    }

}
