package io.appmetrica.analytics.plugins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class describing the error.
 * @see AppMetricaPlugins
 */
public class PluginErrorDetails {

    /**
     * Class containing popular constants used in {@link PluginErrorDetails.Builder#withPlatform(String)}.
     */
    public static final class Platform {
        /**
         * Constant for Native Android
         */
        public static final String NATIVE = "native";
        /**
         * Constant for Flutter
         */
        public static final String FLUTTER = "flutter";
        /**
         * Constant for Unity
         */
        public static final String UNITY = "unity";
        /**
         * Constant for React Native
         */
        public static final String REACT_NATIVE = "react_native";
        /**
         * Constant for Cordova
         */
        public static final String CORDOVA = "cordova";
        /**
         * Constant for Xamarin
         */
        public static final String XAMARIN = "xamarin";
    }

    @Nullable
    private final String exceptionClass;
    @Nullable
    private final String message;
    @NonNull
    private final List<StackTraceItem> stacktrace;
    @Nullable
    private final String platform;
    @Nullable
    private final String virtualMachineVersion;
    @NonNull
    private final Map<String, String> pluginEnvironment;

    private PluginErrorDetails(
            @Nullable String exceptionClass,
            @Nullable String message,
            @NonNull List<StackTraceItem> stacktrace,
            @Nullable String platform,
            @Nullable String virtualMachineVersion,
            @NonNull Map<String, String> pluginEnvironment
    ) {
        this.exceptionClass = exceptionClass;
        this.message = message;
        this.stacktrace = new ArrayList<>(stacktrace);
        this.platform = platform;
        this.virtualMachineVersion = virtualMachineVersion;
        this.pluginEnvironment = CollectionUtils.getMapFromList(CollectionUtils.getListFromMap(pluginEnvironment));
    }

    /**
     * @return class name if it is defined or null otherwise
     */
    @Nullable
    public String getExceptionClass() {
        return exceptionClass;
    }

    /**
     * @return message if it is defined or null otherwise
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * @return list of {@link StackTraceItem}
     */
    @NonNull
    public List<StackTraceItem> getStacktrace() {
        return stacktrace;
    }

    /**
     * @return platform if it is defined or null otherwise
     */
    @Nullable
    public String getPlatform() {
        return platform;
    }

    /**
     * @return virtual machine version if it is defined or null otherwise
     */
    @Nullable
    public String getVirtualMachineVersion() {
        return virtualMachineVersion;
    }

    /**
     * @return plugin environment
     */
    @NonNull
    public Map<String, String> getPluginEnvironment() {
        return pluginEnvironment;
    }

    /**
     * Builder for {@link PluginErrorDetails}.
     */
    public static class Builder {

        @Nullable
        private String exceptionClass;
        @Nullable
        private String message;
        @Nullable
        private List<StackTraceItem> stacktrace;
        @Nullable
        private String platform;
        @Nullable
        private String virtualMachineVersion;
        @Nullable
        private Map<String, String> pluginEnvironment;

        /**
         * Sets exception class.
         *
         * @param value name of the class/interface/symbol (depending on the plugin you are using) of the error.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withExceptionClass(@Nullable String value) {
            exceptionClass = value;
            return this;
        }

        /**
         * Sets message.
         *
         * @param value error message briefly describing the error.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withMessage(@Nullable String value) {
            message = value;
            return this;
        }

        /**
         * Sets stacktrace.
         * @see StackTraceItem
         *
         * @param value error stacktrace.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withStacktrace(@Nullable List<StackTraceItem> value) {
            stacktrace = value;
            return this;
        }

        /**
         * Sets platform.
         * Use constants defined in {@link PluginErrorDetails.Platform} for popular plugins
         * or a custom string for a plugin that does not have a corresponding constant.
         *
         * @param value name of the plugin in which the error occurred.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withPlatform(@Nullable String value) {
            platform = value;
            return this;
        }

        /**
         * Sets virtual machine version.
         * Use this method to specify the version of plugin you are using (e. g. Unity version, Flutter version, etc.).
         *
         * @param value version.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withVirtualMachineVersion(@Nullable String value) {
            virtualMachineVersion = value;
            return this;
        }

        /**
         * Sets plugin environment: arbitrary map containing any additional information about the plugin.
         * <br>
         * This environment is not the same as {@link AppMetricaConfig#errorEnvironment}.
         * The latter is applied to all following errors and crashes
         * while the former is only applied to this particular error.
         *
         * @param value plugin environment
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withPluginEnvironment(@Nullable Map<String, String> value) {
            pluginEnvironment = value;
            return this;
        }

        /**
         * Creates an instance of {@link PluginErrorDetails}.
         *
         * @return {@link PluginErrorDetails} object.
         */
        @NonNull
        public PluginErrorDetails build() {
            return new PluginErrorDetails(
                    exceptionClass,
                    message,
                    WrapUtils.getOrDefault(stacktrace, new ArrayList<StackTraceItem>()),
                    platform,
                    virtualMachineVersion,
                    WrapUtils.getOrDefault(pluginEnvironment, new HashMap<String, String>())
            );
        }
    }
}
