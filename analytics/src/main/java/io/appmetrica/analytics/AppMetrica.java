package io.appmetrica.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.webkit.WebView;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.AppMetricaPluginsImplProvider;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider;
import io.appmetrica.analytics.plugins.AppMetricaPlugins;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class assistant for analytic processing.
 */
public final class AppMetrica {

    private AppMetrica() {
    }

    /**
     * Initializes {@link AppMetrica} with {@link Context} and {@link AppMetricaConfig}.
     *
     * @param context {@link Context} object. Any application context.
     * @param config AppMetrica configuration object.
     *
     * @see AppMetricaConfig
     */
    public static void activate(@NonNull Context context, @NonNull AppMetricaConfig config) {
        AppMetricaProxyProvider.getProxy().activate(context, config);
    }

    /**
     * Initiates forced sending of all stored events from the buffer.<p>
     * AppMetrica SDK doesn't send events immediately after they occurred. It stores events data in the buffer.
     * This method forcibly initiates sending all the data from the buffer and flushes it.<p>
     * Use the method after important checkpoints of user scenarios.
     *
     * <p> <b>WARNING:</b> Frequent use of the method can lead to increasing outgoing internet traffic and
     * energy consumption.
     */
    public static void sendEventsBuffer() {
        AppMetricaProxyProvider.getProxy().sendEventsBuffer();
    }

    /**
     * <p>Helper method for tracking the life cycle of the application.</p>
     * It should be called into {@link android.app.Activity#onResume()} ()}.
     *
     * @param activity {@link Activity} object.
     *
     * @throws java.lang.IllegalArgumentException If {@code activity} is null.
     * @see Activity
     * @see android.app.Activity#onResume()
     * @see AppMetrica#pauseSession(android.app.Activity)
     */
    public static void resumeSession(@Nullable Activity activity) {
        AppMetricaProxyProvider.getProxy().resumeSession(activity);
    }

    /**
     * <p>Helper method for tracking the life cycle of the application.</p>
     * It should be called into {@link android.app.Activity#onPause()}.
     *
     * @param activity {@link Activity} object.
     *
     * @throws java.lang.IllegalArgumentException If {@code activity} is null.
     * @see Activity
     * @see android.app.Activity#onPause()
     * @see AppMetrica#resumeSession(android.app.Activity)
     */
    public static void pauseSession(@Nullable Activity activity) {
        AppMetricaProxyProvider.getProxy().pauseSession(activity);
    }

    /**
     * Sessions should be tracked automatically without invoking this method.
     * See {@link AppMetricaConfig.Builder#withSessionsAutoTrackingEnabled(boolean)}
     *
     * Use this method only if your case is special and you don't see automatically tracked user sessions.
     *
     * <p>Helper method for tracking the lifecycle of the application</p>
     * Sessions should be tracked automatically
     *
     * @param application {@link android.app.Application} whose activities starts and stops should be tracked
     *                                                   automatically
     *
     * @throws java.lang.IllegalArgumentException if {@code application} is null.
     * *
     */
    public static void enableActivityAutoTracking(@NonNull Application application) {
        AppMetricaProxyProvider.getProxy().enableActivityAutoTracking(application);
    }

    /**
     * Sends report by event name.
     *
     * @param eventName Event name. In the {@code eventName} parameter,
     *                  pass a short name or description of the event.
     *
     * @throws java.lang.IllegalArgumentException If {@code eventName} is null.
     * @see AppMetrica#reportEvent(String)
     */
    public static void reportEvent(@NonNull String eventName) {
        AppMetricaProxyProvider.getProxy().reportEvent(eventName);
    }

    /**
     * Sends an error. Use this method to report un unexpected situation.
     * If you use this method errors will be grouped by {@code error} stacktrace.
     * If you want to influence the way errors are grouped use
     * {@link AppMetrica#reportError(String, String, Throwable)} or
     * {@link AppMetrica#reportError(String, String)}
     *
     * @param message Short description or name of the error.
     * @param error   {@link Throwable} object for the error. Can be null.
     *
     * @throws java.lang.IllegalArgumentException If {@code message} is null.
     * @see AppMetrica#reportUnhandledException(Throwable)
     */
    public static void reportError(@NonNull String message, @Nullable Throwable error) {
        AppMetricaProxyProvider.getProxy().reportError(message, error);
    }

    /**
     * Sends an error. Use this method to report un unexpected situation.
     * This method should be used if you want to customize error grouping.
     * If not use {@link AppMetrica#reportError(String, Throwable)}
     *
     * @param identifier An identifier used for grouping errors.
     *                   Errors that have the same identifiers will belong in one group.
     *                   Do not use dynamically formed strings or exception messages as identifiers
     *                   to avoid having too many small crash groups.
     *                   Cannot be null.
     * @param message    Short description or name of the error. Can be null.
     *
     * @throws java.lang.IllegalArgumentException If {@code identifier} is null.
     */
    public static void reportError(@NonNull String identifier, @Nullable String message) {
        AppMetricaProxyProvider.getProxy().reportError(identifier, message, null);
    }

    /**
     * Sends an error. Use this method to report un unexpected situation.
     * This method should be used if you want to customize error grouping.
     * If not use {@link AppMetrica#reportError(String, Throwable)}
     *
     * {@code error} stacktrace will NOT be used for grouping, only {@code identifier}.
     *
     * @param identifier An identifier used for grouping errors.
     *                   Errors that have the same identifiers will belong in one group.
     *                   Do not use dynamically formed strings or exception messages as identifiers
     *                   to avoid having too many small crash groups.
     *                   Cannot be null.
     * @param message    Short description or name of the error. Can be null.
     * @param error      {@link Throwable} object for the error.
     *                   Its stacktrace will not be considered for error grouping. Can be null.
     *
     * @throws java.lang.IllegalArgumentException If {@code identifier} is null.
     */
    public static void reportError(@NonNull String identifier, @Nullable String message, @Nullable Throwable error) {
        AppMetricaProxyProvider.getProxy().reportError(identifier, message, error);
    }

    /**
     * Sends unhandled exception by {@link Throwable} object.
     *
     * @param exception {@link Throwable} object for the unhandled exception.
     *
     * @throws java.lang.IllegalArgumentException If {@code exception} is null.
     * @see AppMetricaConfig.Builder#withCrashReporting(boolean)
     * @see AppMetrica#reportError(String, Throwable)
     */
    public static void reportUnhandledException(@NonNull Throwable exception) {
        AppMetricaProxyProvider.getProxy().reportUnhandledException(exception);
    }

    /**
     * Sends report by event name and event value.
     *
     * @param eventName Event name. In the {@code eventName} parameter,
     *                  pass a short name or description of the event.
     * @param jsonValue Event value. In the {@code eventValue} parameter,
     *                  pass a {@link org.json.JSONObject} represented as a {@link String} object or
     *                  pass a {@link String} object represented in the {@code Java JSON format}.
     *                  Maximum level of nesting (for JSON object) - <b>5.</b>
     *                  <p><b>EXAMPLE:</b></p>
     *                  <pre>
     *                  {@code
     *                   {
     *                       "firstName": "John",
     *                       "lastName": "Smith",
     *                       "age": 25,
     *                       "nickname": "JS"
     *                       "address": {
     *                           "streetAddress": "21 2nd Street",
     *                           "city": "New York",
     *                           "state": "NY",
     *                       },
     *                       "phoneNumbers": [
     *                           {
     *                               "type": "HOME",
     *                               "number": "212 555-1234"
     *                           },
     *                           {
     *                               "type": "FAX",
     *                               "number": "646 555-4567"
     *                           }
     *                       ]
     *                    }
     *                  }
     *                  </pre>
     *
     * @throws java.lang.IllegalArgumentException If {@code eventName} or {@code jsonValue} is null.
     * @see AppMetrica#reportEvent(String, java.util.Map)
     * @see AppMetrica#reportEvent(String)
     * @see org.json.JSONObject#toString()
     */
    public static void reportEvent(@NonNull String eventName, @Nullable String jsonValue) {
        AppMetricaProxyProvider.getProxy().reportEvent(eventName, jsonValue);
    }

    /**
     * Sends report by dictionary.
     *
     * @param eventName  Event name. In the {@code eventName} parameter,
     *                   pass a short name or description of the event.
     * @param attributes Event value. In the {@code attributes} parameter,
     *                   pass a {@link java.util.Map} whose keys are of type {@link String}
     *                   and whose values are of supported types:
     *                   {@link java.lang.Boolean}, {@link java.lang.String},
     *                   and the wrappers for the primitive number types
     *                   {@link java.lang.Double}, {@link java.lang.Integer}, ...
     *
     * @throws java.lang.IllegalArgumentException If {@code eventName} is null.
     * @see AppMetrica#reportEvent(String, String)
     * @see AppMetrica#reportEvent(String)
     */
    public static void reportEvent(@NonNull String eventName, @Nullable Map<String, Object> attributes) {
        AppMetricaProxyProvider.getProxy().reportEvent(eventName, attributes);
    }

    /**
     * Sends report about open app via deeplink
     * @param activity - opened activity with corresponding intent with deeplink
     */
    public static void reportAppOpen(@NonNull Activity activity) {
        AppMetricaProxyProvider.getProxy().reportAppOpen(activity);
    }

    /**
     * Sends report about open app via deeplink. Null and empty values will be ignored.
     * @param deeplink Deeplink value.
     */
    public static void reportAppOpen(@NonNull String deeplink) {
        AppMetricaProxyProvider.getProxy().reportAppOpen(deeplink);
    }

    /**
     * Sends report about open app via deeplink
     * @param intent - intent used to open activity with deeplink
     */
    public static void reportAppOpen(@NonNull Intent intent) {
        AppMetricaProxyProvider.getProxy().reportAppOpen(intent);
    }

    /**
     * Sets referral URL for this installation.
     * This might be required to track some specific traffic sources like Facebook.
     * @param referralUrl referral URL value.
     * @deprecated
     */
    public static void reportReferralUrl(@NonNull String referralUrl) {
        AppMetricaProxyProvider.getProxy().reportReferralUrl(referralUrl);
    }

    /**
     * <p>Sets {@link android.location.Location} to be used as location for reports of AppMetrica.</p>
     * <p>If location is set using this method, it will be used instead of auto collected location.
     * To switch back to auto collected location, pass {@code null} to {@link #setLocation(Location)}.</p>
     *
     * <p><b>NOTE:</b> Permissions:
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION},
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     * improve the quality of auto collected location.</p>
     *
     * @param location location that will be used instead of auto collected
     *
     * @see AppMetrica#setLocationTracking(boolean)
     * @see AppMetrica#setLocationTracking(Context, boolean)
     */
    public static void setLocation(@Nullable Location location) {
        AppMetricaProxyProvider.getProxy().setLocation(location);
    }

    /**
     * Sets whether AppMetrica should include location information within its reports.<p>
     * <b>NOTE:</b> Default value is {@code false}.
     *
     * @param enabled {@code true} to allow AppMetrica to record location information in reports,
     *                otherwise {@code false}.
     *
     * @see AppMetrica#setLocation(android.location.Location)
     */
    public static void setLocationTracking(boolean enabled) {
        AppMetricaProxyProvider.getProxy().setLocationTracking(enabled);
    }

    /**
     * Sets whether AppMetrica should include location information within its reports.<p>
     * <b>NOTE:</b> Default value is {@code false}.
     *
     * @param context Context object
     * @param enabled {@code true} to allow AppMetrica to record location information in reports,
     *                otherwise {@code false}.
     *
     * @see AppMetrica#setLocation(android.location.Location)
     */
    public static void setLocationTracking(@NonNull Context context, boolean enabled) {
        AppMetricaProxyProvider.getProxy().setLocationTracking(context, enabled);
    }

    /**
     * Enables/disables statistics sending to the AppMetrica server. By default, the sending is enabled.
     *
     * <p><b>NOTE:</b> Disabling this option also turns off data sending from the reporters that initialized
     * for different apiKey.
     *
     * @param context Context object
     * @param enabled {@code true} to allow AppMetrica sending statistics,
     *                otherwise {@code false}.
     */
    public static void setStatisticsSending(@NonNull Context context, boolean enabled) {
        AppMetricaProxyProvider.getProxy().setStatisticsSending(context, enabled);
    }

    /**
     * Activates the reporter with {@link ReporterConfig}.
     *
     * @param context Context object.
     * @param config The ReporterConfig object.
     */
    public static void activateReporter(@NonNull Context context, @NonNull ReporterConfig config) {
        AppMetricaProxyProvider.getProxy().activateReporter(context, config);
    }

    /**
     * <p>Creates an {@link io.appmetrica.analytics.IReporter} that can send events to an alternative api key.</p>
     * For every api key only one {@link io.appmetrica.analytics.IReporter} instance is created.
     * You can either query it each time you need it, or save the reference by yourself.
     *
     * @param context Context object
     * @param apiKey api key of the reporter
     *
     * @return reporter instance for given api key
     */
    @NonNull
    public static IReporter getReporter(@NonNull Context context, @NonNull String apiKey) {
        return AppMetricaProxyProvider.getProxy().getReporter(context, apiKey);
    }

    /**
     * @return <b>VERSION</b> of library.
     *
     * @see AppMetrica#getLibraryApiLevel()
     */
    @NonNull
    public static String getLibraryVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * @return <b>API LEVEL</b> of library.
     *
     * @see AppMetrica#getLibraryVersion()
     */
    public static int getLibraryApiLevel() {
        return BuildConfig.API_LEVEL;
    }

    /**
     * Requests deferred deeplink parameters. Parameters will be available after receiving
     * <a href = "https://developers.google.com/analytics/devguides/collection/android/v4/campaigns">
     * Google Play installation referrer from Google Play</a>. Google Play installation referrer
     * is usually received soon after first launch of application.
     * After Google Play installation referrer is received, deferred deeplink parameters will be extracted
     * and delivered to {@link DeferredDeeplinkParametersListener#onParametersLoaded(Map)} listener.
     * If error occurs it will be delivered to
     * {@link DeferredDeeplinkParametersListener#onError(DeferredDeeplinkParametersListener.Error, String)}
     *
     * @param listener the object that receives callbacks when Google Play referrer is received or
     *                 error occurs.
     */
    public static void requestDeferredDeeplinkParameters(@NonNull DeferredDeeplinkParametersListener listener) {
        AppMetricaProxyProvider.getProxy().requestDeferredDeeplinkParameters(listener);
    }

    /**
     * Requests deferred deeplink. It will be available after receiving
     * <a href = "https://developers.google.com/analytics/devguides/collection/android/v4/campaigns">
     * Google Play installation referrer from Google Play</a>. Google Play installation referrer
     * is usually received soon after first launch of application.
     * After Google Play installation referrer is received, deferred deeplink will be extracted
     * and delivered to {@link io.appmetrica.analytics.DeferredDeeplinkListener#onDeeplinkLoaded(String)} listener.
     * If error occurs it will be delivered to
     * {@link io.appmetrica.analytics.DeferredDeeplinkListener#onError(DeferredDeeplinkListener.Error, String)}
     *
     * @param listener the object that receives callbacks when Google Play referrer is received or
     *                 error occurs.
     */
    public static void requestDeferredDeeplink(@NonNull DeferredDeeplinkListener listener) {
        AppMetricaProxyProvider.getProxy().requestDeferredDeeplink(listener);
    }

    /**
     * Sets the ID of the user profile.
     *
     * <b>NOTE:</b> The string value can contain up to 200 characters.
     *
     * @param userProfileID The custom user profile ID.
     */
    public static void setUserProfileID(@Nullable String userProfileID) {
        AppMetricaProxyProvider.getProxy().setUserProfileID(userProfileID);
    }

    /**
     * Sends information about the user profile.
     *
     * @param profile The {@link io.appmetrica.analytics.profile.UserProfile} object. Contains user profile information.
     */
    public static void reportUserProfile(@NonNull UserProfile profile) {
        AppMetricaProxyProvider.getProxy().reportUserProfile(profile);
    }

    /**
     * Sends information about the purchase.
     *
     * @param revenue The {@link io.appmetrica.analytics.Revenue} object. It contains purchase information
     */
    public static void reportRevenue(@NonNull Revenue revenue) {
        AppMetricaProxyProvider.getProxy().reportRevenue(revenue);
    }

    /**
     * Sends e-commerce event.
     * @see io.appmetrica.analytics.ecommerce.ECommerceEvent
     *
     * @param event The {@link ECommerceEvent} object to be sent.
     */
    public static void reportECommerce(@NonNull ECommerceEvent event) {
        AppMetricaProxyProvider.getProxy().reportECommerce(event);
    }

    /**
     * Sets key - value data to be used as additional information, associated
     * with your unhandled exception and error reports.
     * @param key the environment key.
     * @param value the environment value. To remove pair from environment pass {@code null} value.
     */
    public static void putErrorEnvironmentValue(@NonNull String key, @Nullable String value) {
        AppMetricaProxyProvider.getProxy().putErrorEnvironmentValue(key, value);
    }

    /**
     * Adds Javascript interface named "AppMetrica" to WebView's javascript.
     * It allows then to report events to AppMetrica from JS code.
     * <br>
     * <b>NOTE:</b> This method must be called from main thread.
     * <br>
     * <b>NOTE:</b> Reporting from JS code will not be enabled on API levels 16 and less due to security issues.
     *
     * @see io.appmetrica.analytics.AppMetricaJsInterface
     *
     * @param webView WebView where AppMetrica should enable reporting
     */
    @MainThread
    public static void initWebViewReporting(@NonNull WebView webView) {
        AppMetricaProxyProvider.getProxy().initWebViewReporting(webView);
    }

    /**
     * <p>Creates a {@link io.appmetrica.analytics.plugins.AppMetricaPlugins} instance that can send plugin events to main API key.</p>
     * Only one {@link io.appmetrica.analytics.plugins.AppMetricaPlugins} instance is created per each app process.
     * You can either query it each time you need it, or save the reference by yourself.<br>
     * <b>NOTE:</b> to use this extension you must activate AppMetrica first
     * via {@link AppMetrica#activate(android.content.Context, AppMetricaConfig)}.
     *
     * @return plugin extension instance
     */
    @NonNull
    public static AppMetricaPlugins getPluginExtension() {
        return AppMetricaPluginsImplProvider.getImpl();
    }

    /**
     * Sends information about ad revenue.
     *
     * @param adRevenue Object containing the information about ad revenue.
     *
     * @see AdRevenue
     */
    public static void reportAdRevenue(@NonNull AdRevenue adRevenue) {
        AppMetricaProxyProvider.getProxy().reportAdRevenue(adRevenue);
    }

    /**
     * Returns <code>deviceId</code>.
     *
     * @param context
     *         any Context object
     *
     * @return <code>deviceId</code> if present, <code>null</code> otherwise.
     * @see AppMetrica#requestStartupParams
     */
    @Nullable
    public static String getDeviceId(@NonNull Context context) {
        return AppMetricaProxyProvider.getProxy().getDeviceId();
    }

    /**
     * Returns <code>uuid</code>.
     *
     * @param context
     *         any Context object
     *
     * @return <code>uuid</code> if present, <code>null</code> otherwise.
     * @see AppMetrica#requestStartupParams
     */
    @Nullable
    public static String getUuid(@NonNull Context context) {
        return AppMetricaProxyProvider.getProxy().getUuid(context).id;
    }

    /**
     * Sets key - value pair to be used as additional information, associated
     * with your application runtime's environment. This environment is unique for every unique
     * APIKey and shared between processes. Application's environment persists to storage and
     * retained between application launches. To reset environment use
     * {@link AppMetrica#clearAppEnvironment()}
     * <p>If called before metrica initialization, environment will be added right after metrica initialize
     * <p> <b>WARNING:</b> Application's environment is a global permanent state and
     * can't be changed too often. For frequently changed parameters use extended reportMessage methods.
     * @param key the environment key.
     * @param value the environment value. To remove pair from environment pass {@code null} value.
     * @see AppMetrica#reportEvent(String, Map)
     * @see AppMetrica#reportEvent(String, String)
     * @see AppMetrica#clearAppEnvironment()
     */
    public static void putAppEnvironmentValue(@NonNull String key, @Nullable String value) {
        AppMetricaProxyProvider.getProxy().putAppEnvironmentValue(key, value);
    }

    /**
     * Clears app environment and removes it from storage.
     * <p>If called before metrica initialization, app environment will be cleared right after init</p>
     * @see AppMetrica#putAppEnvironmentValue(String, String)
     */
    public static void clearAppEnvironment() {
        AppMetricaProxyProvider.getProxy().clearAppEnvironment();
    }

    /**
     * <p>Returns only requested params (possible options are listed in {@link StartupParamsCallback}
     * and any other custom keys assuming they are present in startup)
     * by {@link StartupParamsCallback}.
     * For example, to retrieve custom host which will be present in startup by key "my_sdk_host",
     * use the following invocation:
     * <code>AppMetrica.requestStartupParams(context, callback, Arrays.asList("my_sdk_host"));</code>.
     *
     * It's possible, that params won't be returned immediately if they haven't yet received.
     * But the <code>callback</code> is notified immediately when the params are received,
     * otherwise if they have already presented, then the <code>callback</code> will be notified immediately.
     * When the <code>callback</code> is notified once, then it will be removed. </p>
     *
     * <p>Please don't use fully anonymous callbacks, because they are wrapped
     * by {@link java.lang.ref.WeakReference} and cleaned-up automatically.
     * The problem with this approach that you can't have a callback
     * which is only referenced in the collection
     * as it will disappear randomly (on the next <code>GC</code>).</p>
     *
     * {@code NOTE}: You're able to call this method without general initialization
     * via {@link AppMetrica#activate(Context, AppMetricaConfig)}, but without
     * this call it will take longer to retrieve startup identifiers.
     *
     * @param context Context object
     * @param callback An object that implements {@link StartupParamsCallback} interface.
     * @param params List of params to be requested.
     *               If params is empty list of {@link StartupParamsCallback#APPMETRICA_UUID},
     *               {@link StartupParamsCallback#APPMETRICA_DEVICE_ID},
     *               {@link StartupParamsCallback#APPMETRICA_DEVICE_ID_HASH} will be requested.
     */
    public static void requestStartupParams(
            @NonNull final Context context,
            @NonNull final StartupParamsCallback callback,
            @NonNull final List<String> params
    ) {
        AppMetricaProxyProvider.getProxy().requestStartupParams(
            context,
            callback,
            params.isEmpty() ?
                Arrays.asList(
                    StartupParamsCallback.APPMETRICA_UUID,
                    StartupParamsCallback.APPMETRICA_DEVICE_ID,
                    StartupParamsCallback.APPMETRICA_DEVICE_ID_HASH
                ) : params
        );
    }

    /**
     * Add listener for detect ANR.
     * It should be called after {@link AppMetrica#activate} method.
     *
     * @param listener An object that implements {@link AnrListener} interface.
     */
    public static void registerAnrListener(@NonNull AnrListener listener) {
        AppMetricaProxyProvider.getProxy().registerAnrListener(listener);
    }
}
