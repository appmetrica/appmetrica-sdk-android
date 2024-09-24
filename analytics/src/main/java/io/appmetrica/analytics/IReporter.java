package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.Map;

/**
 * <p>{@link io.appmetrica.analytics.IReporter} can send events to an alternative api key, differ from
 * api key, passed to {@link AppMetrica#activate(android.content.Context, AppMetricaConfig)
 * }</p>
 * Instance of object, implements {@link io.appmetrica.analytics.IReporter}, can be obtained via
 * {@link AppMetrica#getReporter(android.content.Context, String)} method call.
 * <p>For every api key only one {@link io.appmetrica.analytics.IReporter} instance is created.
 * You can either query it each time you need it, or save the reference by yourself.
 *
 * @see AppMetrica#activate(android.content.Context, AppMetricaConfig)
 * @see AppMetrica#getReporter(android.content.Context, String)
 */
public interface IReporter {

    /**
     * Initiates forced sending of all stored events from the buffer.<p>
     * AppMetrica SDK doesn't send events immediately after they occurred. It stores events data in the buffer.
     * This method forcibly initiates sending all the data from the buffer and flushes it.<p>
     * Use the method after important checkpoints of user scenarios.
     *
     * <p> <b>WARNING:</b> Frequent use of the method can lead to increasing outgoing internet traffic and
     * energy consumption.
     * 
     * @see ReporterConfig.Builder#withMaxReportsCount(int) 
     * @see ReporterConfig.Builder#withDispatchPeriodSeconds(int) 
     */
    void sendEventsBuffer();

    /**
     * Sends report by event name.
     *
     * @param eventName Event name. In the {@code eventName} parameter,
     *                  pass a short name or description of the event.
     *
     * @throws java.lang.IllegalArgumentException If {@code eventName} is null.
     * @see IReporter#reportEvent(String)
     */
    void reportEvent(@NonNull String eventName);

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
     * @see IReporter#reportEvent(String, String)
     * @see org.json.JSONObject#toString()
     */
    void reportEvent(@NonNull String eventName, @Nullable String jsonValue);

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
     * @throws java.lang.IllegalArgumentException If {@code eventName} or {@code attributes} is null.
     * @see IReporter#reportEvent(String, java.util.Map)
     */
    void reportEvent(@NonNull String eventName, @Nullable Map<String, Object> attributes);

    /**
     * Sends an error. Use this method to report un unexpected situation.
     * If you use this method errors will be grouped by {@code error} stacktrace.
     * If you want to influence the way errors are grouped use
     * {@link io.appmetrica.analytics.IReporter#reportError(String, String, Throwable)} or
     * {@link io.appmetrica.analytics.IReporter#reportError(String, String)}
     *
     * @param message Short description or name of the error.
     * @param error   {@link Throwable} object for the error. Can be null.
     *
     * @throws java.lang.IllegalArgumentException If {@code message} is null.
     * @see io.appmetrica.analytics.IReporter#reportUnhandledException(Throwable)
     */
    void reportError(@NonNull String message, @Nullable Throwable error);

    /**
     * Sends an error. Use this method to report un unexpected situation.
     * This method should be used if you want to customize error grouping.
     * If not use {@link io.appmetrica.analytics.IReporter#reportError(String, Throwable)}
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
    void reportError(@NonNull String identifier, @Nullable String message);

    /**
     * Sends an error. Use this method to report un unexpected situation.
     * This method should be used if you want to customize error grouping.
     * If not use {@link io.appmetrica.analytics.IReporter#reportError(String, Throwable)}
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
    void reportError(@NonNull String identifier, @Nullable String message, @Nullable Throwable error);

    /**
     * Sends unhandled exception by {@link Throwable} object.
     *
     * @param exception {@link Throwable} object for the unhandled exception.
     *
     * @throws java.lang.IllegalArgumentException If {@code exception} is null.
     * @see IReporter#reportUnhandledException(Throwable)
     */
    void reportUnhandledException(@NonNull Throwable exception);

    /**
     * <p>Helper method for sessions tracking.</p>
     * Usually, it should be called into {@link android.app.Activity#onResume()}.
     *
     * @see android.app.Activity#onResume()
     * @see IReporter#pauseSession()
     */
    void resumeSession();

    /**
     * <p>Helper method for sessions tracking.</p>
     * Usually, it should be called into {@link android.app.Activity#onPause()}.
     *
     * @see android.app.Activity#onPause()
     * @see IReporter#resumeSession()
     */
    void pauseSession();

    /**
     * Sets the ID of the user profile.
     *
     * <b>NOTE:</b> The string value can contain up to 200 characters.
     *
     * @param profileID The custom user profile ID.
     */
    void setUserProfileID(@Nullable String profileID);

    /**
     * Sends information about the user profile.
     *
     * @param profile The {@link io.appmetrica.analytics.profile.UserProfile} object. Contains user profile information.
     */
    void reportUserProfile(@NonNull UserProfile profile);

    /**
     * Sends information about the purchase.
     *
     * @param revenue The {@link io.appmetrica.analytics.Revenue} object. It contains purchase information.
     */
    void reportRevenue(@NonNull Revenue revenue);

    /**
     * Sends e-commerce event.
     * @see io.appmetrica.analytics.ecommerce.ECommerceEvent
     *
     * @param event The {@link ECommerceEvent} object to be sent.
     */
    void reportECommerce(@NonNull ECommerceEvent event);

    /**
     * Enables/disables data sending to the AppMetrica server. By default, the sending is enabled.
     *
     * <p><b>NOTE:</b> Disabling this option doesn't affect data sending from the main apiKey and other
     * reporters.
     *
     * @param enabled {@code true} to allow AppMetrica sending data, otherwise {@code false}.
     */
    void setDataSendingEnabled(boolean enabled);

    /**
     * <p>Creates an {@link io.appmetrica.analytics.plugins.IPluginReporter} that can send plugin events to this reporter.</p>
     * For every reporter only one {@link io.appmetrica.analytics.plugins.IPluginReporter} instance is created.
     * You can either query it each time you need it, or save the reference by yourself.
     *
     * @return plugin extension instance for this reporter
     */
    @NonNull
    IPluginReporter getPluginExtension();

    /**
     * Sends information about ad revenue.
     *
     * @param adRevenue Object containing the information about ad revenue.
     *
     * @see AdRevenue
     */
    void reportAdRevenue(@NonNull AdRevenue adRevenue);

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
     */
    void putAppEnvironmentValue(@NonNull String key, @Nullable String value);

    /**
     * Clears app environment and removes it from storage.
     * <p>If called before metrica initialization, app environment will be cleared right after init
     */
    void clearAppEnvironment();
}
