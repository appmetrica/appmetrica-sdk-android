package io.appmetrica.analytics.coreutils.internal.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SafePackageManagerTest extends CommonTest {

    @Mock
    private Context mContext;
    @Mock
    private PackageManager mPackageManager;
    @Mock
    private PackageInfo mPackageInfo;
    @Mock
    private Intent mIntent;
    @Mock
    private ComponentName mComponentName;
    private List<ResolveInfo> mResolveInfoList;
    private List<PackageInfo> mPackageInfoList;
    private final String mPackageName = "package name";

    private final SafePackageManager mSafePackageManager = new SafePackageManager();

    @Rule
    public final MockedStaticRule<SafePackageManagerHelperForR> sSafePackageManagerHelperForR =
        new MockedStaticRule<>(SafePackageManagerHelperForR.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
        when(mContext.getPackageName()).thenReturn(mPackageName);
        mResolveInfoList = new ArrayList<ResolveInfo>();
        mPackageInfoList = new ArrayList<PackageInfo>();
        mResolveInfoList.add(mock(ResolveInfo.class));
        mPackageInfoList.add(mock(PackageInfo.class));
    }

    @Test
    public void testGetPackageInfoOK() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getPackageInfo((String) any(), anyInt())).thenReturn(mPackageInfo);
        assertThat(mSafePackageManager.getPackageInfo(mContext, mPackageName)).isEqualTo(mPackageInfo);
    }

    @Test
    public void testGetPackageInfoException() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getPackageInfo((String) any(), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getPackageInfo(mContext, "packageName")).isNull();
    }

    @Test
    public void testGetPackageInfoWithFlagsOK() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getPackageInfo((String) any(), anyInt())).thenReturn(mPackageInfo);
        assertThat(mSafePackageManager.getPackageInfo(mContext, "packageName", 0)).isEqualTo(mPackageInfo);
    }

    @Test
    public void testGetPackageInfoWithFlagsException() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getPackageInfo((String) any(), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getPackageInfo(mContext, "packageName", 0)).isNull();
    }

    @Test
    public void testGetServiceInfoOK() throws PackageManager.NameNotFoundException {
        ServiceInfo serviceInfo = mock(ServiceInfo.class);
        when(mPackageManager.getServiceInfo(any(ComponentName.class), anyInt())).thenReturn(serviceInfo);
        assertThat(mSafePackageManager.getServiceInfo(mContext, mComponentName, 0)).isEqualTo(serviceInfo);
    }

    @Test
    public void testGetServiceInfoException() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getServiceInfo(any(ComponentName.class), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getServiceInfo(mContext, mComponentName, 0)).isNull();
    }

    @Test
    public void testResolveServiceOK() {
        ResolveInfo resolveInfo = mock(ResolveInfo.class);
        when(mPackageManager.resolveService(any(Intent.class), anyInt())).thenReturn(resolveInfo);
        assertThat(mSafePackageManager.resolveService(mContext, mIntent, 0)).isEqualTo(resolveInfo);
    }

    @Test
    public void testResolveServiceException() {
        when(mPackageManager.resolveService(any(Intent.class), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.resolveService(mContext, mIntent, 0)).isNull();
    }

    @Test
    public void testResolveActivityOK() {
        ResolveInfo resolveInfo = mock(ResolveInfo.class);
        when(mPackageManager.resolveActivity(any(Intent.class), anyInt())).thenReturn(resolveInfo);
        assertThat(mSafePackageManager.resolveActivity(mContext, mIntent, 0)).isEqualTo(resolveInfo);
    }

    @Test
    public void testResolveActivityException() {
        when(mPackageManager.resolveActivity(any(Intent.class), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.resolveActivity(mContext, mIntent, 0)).isNull();
    }

    @Test
    public void testGetApplicationInfoOK() throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
        when(mPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(applicationInfo);
        assertThat(mSafePackageManager.getApplicationInfo(mContext, mPackageName, 0)).isEqualTo(applicationInfo);
    }

    @Test
    public void testGetApplicationInfoException() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getApplicationInfo(anyString(), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getApplicationInfo(mContext, mPackageName, 0)).isNull();
    }

    @Test
    public void getApplicationMetaData() throws Exception {
        Bundle metaData = new Bundle();
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.metaData = metaData;
        when(mPackageManager.getApplicationInfo(mPackageName, PackageManager.GET_META_DATA))
            .thenReturn(applicationInfo);
        assertThat(mSafePackageManager.getApplicationMetaData(mContext)).isEqualTo(metaData);
    }

    @Test
    public void getApplicationMetaDataThrowsException() throws Exception {
        when(mPackageManager.getApplicationInfo(mPackageName, PackageManager.GET_META_DATA))
            .thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getApplicationMetaData(mContext)).isNull();
    }

    @Test
    public void getApplicationMetaDataIfPackageManagerReturnNull() throws Exception {
        when(mPackageManager.getApplicationInfo(mPackageName, PackageManager.GET_META_DATA))
            .thenReturn(null);
        assertThat(mSafePackageManager.getApplicationMetaData(mContext)).isNull();
    }

    @Test
    public void getApplicationMetaDataIfNull() throws Exception {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.metaData = null;
        when(mPackageManager.getApplicationInfo(mPackageName, PackageManager.GET_META_DATA))
            .thenReturn(applicationInfo);
        assertThat(mSafePackageManager.getApplicationMetaData(mContext)).isNull();
    }

    @Test
    public void testGetActivityInfoOK() throws PackageManager.NameNotFoundException {
        ActivityInfo activityInfo = mock(ActivityInfo.class);
        when(mPackageManager.getActivityInfo(any(ComponentName.class), anyInt())).thenReturn(activityInfo);
        assertThat(mSafePackageManager.getActivityInfo(mContext, mock(ComponentName.class), 0)).isSameAs(activityInfo);
    }

    @Test
    public void testGetActivityInfoException() throws PackageManager.NameNotFoundException {
        when(mPackageManager.getActivityInfo(any(ComponentName.class), anyInt())).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getActivityInfo(mContext, mock(ComponentName.class), 0)).isNull();
    }

    @Test
    public void testSetComponentEnabledSettingOK() {
        final int newState = 0;
        final int flags = 0;
        mSafePackageManager.setComponentEnabledSetting(mContext, mComponentName, newState, flags);
        verify(mPackageManager).setComponentEnabledSetting(same(mComponentName), eq(newState), eq(flags));
    }

    @Test
    public void testSetComponentEnabledSettingException() {
        doThrow(new RuntimeException()).when(mPackageManager).setComponentEnabledSetting(any(ComponentName.class), anyInt(), anyInt());
        mSafePackageManager.setComponentEnabledSetting(mContext, mComponentName, 0, 0);
    }

    @Test
    public void testHasSystemFeatureTrue() {
        final String feature = "feature";
        when(mPackageManager.hasSystemFeature(feature)).thenReturn(true);
        assertThat(mSafePackageManager.hasSystemFeature(mContext, feature)).isTrue();
    }

    @Test
    public void testHasSystemFeatureFalse() {
        final String feature = "feature";
        when(mPackageManager.hasSystemFeature(feature)).thenReturn(false);
        assertThat(mSafePackageManager.hasSystemFeature(mContext, feature)).isFalse();
    }

    @Test
    public void testHasSystemFeatureException() {
        final String feature = "feature";
        when(mPackageManager.hasSystemFeature(feature)).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.hasSystemFeature(mContext, feature)).isFalse();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.O_MR1)
    public void testGetInstallerPackageNameOK() {
        final String installer = "yandex.store";
        when(mPackageManager.getInstallerPackageName(mPackageName)).thenReturn(installer);
        assertThat(mSafePackageManager.getInstallerPackageName(mContext, mPackageName)).isEqualTo(installer);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.O_MR1)
    public void testGetInstallerPackageNameException() {
        when(mPackageManager.getInstallerPackageName(mPackageName)).thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getInstallerPackageName(mContext, mPackageName)).isNull();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.R)
    public void testGetInstallerPackageNameAndroid11OK() {
        final String installer = "yandex.store";
        when(SafePackageManagerHelperForR.extractPackageInstaller(mPackageManager, mPackageName)).thenReturn(installer);
        assertThat(mSafePackageManager.getInstallerPackageName(mContext, mPackageName)).isEqualTo(installer);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.R)
    public void testGetInstallerPackageNameAndroid11ForNull() {
        when(SafePackageManagerHelperForR.extractPackageInstaller(mPackageManager, mPackageName)).thenReturn(null);
        assertThat(mSafePackageManager.getInstallerPackageName(mContext, mPackageName)).isNull();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.R)
    public void testGetInstallerPackageNameAndroid11ForEmpty() {
        when(SafePackageManagerHelperForR.extractPackageInstaller(mPackageManager, mPackageName)).thenReturn("");
        assertThat(mSafePackageManager.getInstallerPackageName(mContext, mPackageName)).isEqualTo("");
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.R)
    public void testGetInstallerPackageNameAndroid11Exception() {
        when(SafePackageManagerHelperForR.extractPackageInstaller(mPackageManager, mPackageName))
            .thenThrow(new RuntimeException());
        assertThat(mSafePackageManager.getInstallerPackageName(mContext, mPackageName)).isNull();
    }
}
