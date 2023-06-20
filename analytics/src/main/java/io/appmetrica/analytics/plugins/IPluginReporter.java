package io.appmetrica.analytics.plugins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>{@link IPluginReporter} is an extension of {@link io.appmetrica.analytics.IReporter}.</p>
 * Instance of object that implements {@link IPluginReporter} can be obtained via
 * {@link io.appmetrica.analytics.IReporter#getPluginExtension()} method call.
 * <p>For every reporter only one {@link IPluginReporter} instance is created.
 * You can either query it each time you need it, or save the reference by yourself.</p>
 *
 * @see io.appmetrica.analytics.IReporter#getPluginExtension()
 */
public interface IPluginReporter {

    /**
     * Reports unhandled exception from plugin.
     *
     * @param errorDetails Object describing the exception.
     * @see PluginErrorDetails
     */
    void reportUnhandledException(@NonNull PluginErrorDetails errorDetails);

    /**
     * Reports error from plugin. Use this method to report an unexpected situation.
     * If you use this method errors will be grouped by {@code errorDetails} stacktrace.
     * If you want to influence the way errors are grouped use
     * {@link IPluginReporter#reportError(String, String, PluginErrorDetails)}
     *
     * @param errorDetails Object describing the error.
     *                     <b>For the error to be sent, errorDetails must contain non empty stacktrace.</b>
     *                     Otherwise it will be ignored. If you can't provide stacktrace,
     *                     use {@link IPluginReporter#reportError(String, String, PluginErrorDetails)}
     *                     with filled identifier.
     * @param message      Short description or name of the error.
     * @see PluginErrorDetails
     */
    void reportError(@NonNull PluginErrorDetails errorDetails, @Nullable String message);

    /**
     * Reports error from plugin. Use this method to report an unexpected situation.
     * This method should be used if you want to customize error grouping.
     * If not use {@link IPluginReporter#reportError(PluginErrorDetails, String)}
     *
     * @param identifier   An identifier used for grouping errors.
     *                     Errors that have the same identifiers will belong in one group.
     *                     Do not use dynamically formed strings or exception messages as identifiers
     *                     to avoid having too many small crash groups.
     * @param message      Short description or name of the error.
     * @param errorDetails Object describing the error.
     * @see PluginErrorDetails
     */
    void reportError(@NonNull String identifier, @Nullable String message, @Nullable PluginErrorDetails errorDetails);
}
