package io.appmetrica.analytics.coreutils.internal.services;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;

public class FrameworkDetector {

    private static final String UNITY_CLASS = "com.unity3d.player.UnityPlayer";
    private static final String XAMARIN_CLASS = "mono.MonoPackageManager";
    private static final String CORDOVA_CLASS = "org.apache.cordova.CordovaPlugin";
    private static final String REACT_CLASS = "com.facebook.react.ReactRootView";
    private static final String FLUTTER_CLASS = "io.flutter.embedding.engine.FlutterEngine";
    // When adding class here also add it to proguard-consumer, otherwise it might end up obfuscated.

    private static final String FRAMEWORK_UNITY = "unity";
    private static final String FRAMEWORK_XAMARIN = "xamarin";
    private static final String FRAMEWORK_CORDOVA = "cordova";
    private static final String FRAMEWORK_REACT = "react";
    private static final String FRAMEWORK_FLUTTER = "flutter";
    private static final String FRAMEWORK_DEFAULT = "native";

    @NonNull
    private final static String FRAMEWORK = new FrameworkDetector().detectFramework();

    @VisibleForTesting
    public FrameworkDetector() {

    }

    @NonNull
    public static String framework() {
        return FRAMEWORK;
    }

    @VisibleForTesting
    @NonNull
    public String detectFramework() {
        String framework = FRAMEWORK_DEFAULT;
        //Unity and xamarin both use mono. So at first we need to detect unity.
        if (ReflectionUtils.detectClassExists(UNITY_CLASS)) {
            framework = FRAMEWORK_UNITY;
        } else if (ReflectionUtils.detectClassExists(XAMARIN_CLASS)) {
            framework = FRAMEWORK_XAMARIN;
        } else if (ReflectionUtils.detectClassExists(CORDOVA_CLASS)) {
            framework = FRAMEWORK_CORDOVA;
        } else if (ReflectionUtils.detectClassExists(REACT_CLASS)) {
            framework = FRAMEWORK_REACT;
        } else if (ReflectionUtils.detectClassExists(FLUTTER_CLASS)) {
            framework = FRAMEWORK_FLUTTER;
        }
        return framework;
    }
}
