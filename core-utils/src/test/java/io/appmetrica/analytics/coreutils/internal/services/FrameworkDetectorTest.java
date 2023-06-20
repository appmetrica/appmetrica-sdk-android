package io.appmetrica.analytics.coreutils.internal.services;

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FrameworkDetectorTest extends CommonTest {

    private static final String UNITY_CLASS = "com.unity3d.player.UnityPlayer";
    private static final String XAMARIN_CLASS = "mono.MonoPackageManager";
    private static final String CORDOVA_CLASS = "org.apache.cordova.CordovaPlugin";
    private static final String REACT_CLASS = "com.facebook.react.ReactRootView";
    private static final String FLUTTER_CLASS = "io.flutter.embedding.engine.FlutterEngine";

    private static final String FRAMEWORK_UNITY = "unity";
    private static final String FRAMEWORK_XAMARIN = "xamarin";
    private static final String FRAMEWORK_CORDOVA = "cordova";
    private static final String FRAMEWORK_REACT = "react";
    private static final String FRAMEWORK_FLUTTER = "flutter";
    private static final String FRAMEWORK_DEFAULT = "native";

    @Rule
    public MockedStaticRule<ReflectionUtils> reflectiveUtilsMockedRule = new MockedStaticRule<>(ReflectionUtils.class);

    private FrameworkDetector detector;

    @Before
    public void setUp() {
        detector = new FrameworkDetector();
    }

    @Test
    public void testXamarin() {
        when(ReflectionUtils.detectClassExists(XAMARIN_CLASS)).thenReturn(true);
        assertThat(detector.detectFramework()).isEqualTo(FRAMEWORK_XAMARIN);
    }

    @Test
    public void nativeFramework() {
        assertThat(detector.detectFramework()).isEqualTo(FRAMEWORK_DEFAULT);
    }

    @Test
    public void testUnity() {
        when(ReflectionUtils.detectClassExists(XAMARIN_CLASS)).thenReturn(true);
        when(ReflectionUtils.detectClassExists(UNITY_CLASS)).thenReturn(true);
        assertThat(detector.detectFramework()).isEqualTo(FRAMEWORK_UNITY);
    }

    @Test
    public void testCordova() {
        when(ReflectionUtils.detectClassExists(CORDOVA_CLASS)).thenReturn(true);
        assertThat(detector.detectFramework()).isEqualTo(FRAMEWORK_CORDOVA);
    }

    @Test
    public void testReact() {
        when(ReflectionUtils.detectClassExists(REACT_CLASS)).thenReturn(true);
        assertThat(detector.detectFramework()).isEqualTo(FRAMEWORK_REACT);
    }

    @Test
    public void flutter() {
        when(ReflectionUtils.detectClassExists(FLUTTER_CLASS)).thenReturn(true);
        assertThat(detector.detectFramework()).isEqualTo(FRAMEWORK_FLUTTER);
    }
}
