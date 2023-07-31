package io.appmetrica.analytics.impl.proxy.validation;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator;
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.impl.utils.validation.api.ApiKeyValidator;
import java.util.List;

public class MainFacadeBarrier extends ReporterBarrier {

    private static final Validator<AppMetricaConfig> sConfigValidator =
            new ThrowIfFailedValidator<AppMetricaConfig>(
                    new NonNullValidator<AppMetricaConfig>("Config")
            );
    private static final Validator<Activity> sActivityValidator = new ThrowIfFailedValidator<Activity>(
            new NonNullValidator<Activity>("Activity")
    );
    private static final Validator<Intent> sIntentValidator = new ThrowIfFailedValidator<Intent>(
            new NonNullValidator<Intent>("Intent")
    );
    private static final Validator<Application> sApplicationValidator = new ThrowIfFailedValidator<Application>(
            new NonNullValidator<Application>("Application")
    );
    private static final Validator<Context> sContextValidator = new ThrowIfFailedValidator<Context>(
            new NonNullValidator<Context>("Context")
    );
    private static final Validator<Object> sDeeplinkListenerValidator =
            new ThrowIfFailedValidator<Object>(
                    new NonNullValidator<Object>("Deeplink listener")
    );
    private static final Validator<ReporterConfig> sReporterConfigValidator =
            new ThrowIfFailedValidator<ReporterConfig>(
                    new NonNullValidator<ReporterConfig>("Reporter Config")
            );
    private static final Validator<String> sDeeplinkValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Deeplink")
    );
    private static final Validator<String> sReferralUrlValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Referral url")
    );
    private static final Validator<String> sApiKeyValidator = new ThrowIfFailedValidator<String>(new ApiKeyValidator());
    private static final Validator<String> sNonNullKeyValidator =
            new ThrowIfFailedValidator<String>(
                    new NonNullValidator<String>("Key")
            );
    private static final Validator<WebView> sNonNullWebViewValidator =
            new ThrowIfFailedValidator<WebView>(
                    new NonNullValidator<WebView>("WebView")
            );
    private static final Validator<String> sSilentNonEmptyValueValidator = new NonEmptyStringValidator("value");
    private static final Validator<String> sSilentNonEmptyNameValidator = new NonEmptyStringValidator("name");
    private static final Validator<Object> sCallbackValidator =
            new ThrowIfFailedValidator<>(
                    new NonNullValidator<>("AppMetricaDeviceIdentifiers callback")
            );
    private static final Validator<AnrListener> sAnrListenerValidator =
        new ThrowIfFailedValidator<AnrListener>(
            new NonNullValidator<AnrListener>("ANR listener")
        );

    public void enableActivityAutoTracking(@NonNull Application application) {
        sApplicationValidator.validate(application);
    }

    public void reportAppOpen(@NonNull Activity activity) {
        sActivityValidator.validate(activity);
    }

    public void reportAppOpen(@NonNull String deeplink) {
        sDeeplinkValidator.validate(deeplink);
    }

    public void reportAppOpen(@NonNull Intent intent) {
        sIntentValidator.validate(intent);
    }

    public void reportReferralUrl(@NonNull String referralUrl) {
        sReferralUrlValidator.validate(referralUrl);
    }

    public void setLocation(@Nullable Location location) {}

    public void setLocationTracking(boolean enabled) {}

    public void setLocationTracking(@NonNull Context context, boolean enabled) {
        sContextValidator.validate(context);
    }

    public void requestDeferredDeeplinkParameters(@NonNull DeferredDeeplinkParametersListener listener) {
        sDeeplinkListenerValidator.validate(listener);
    }

    public void requestDeferredDeeplink(@NonNull DeferredDeeplinkListener listener) {
        sDeeplinkListenerValidator.validate(listener);
    }

    public void setStatisticsSending(@NonNull Context context, boolean enabled) {
        sContextValidator.validate(context);
    }

    public void getReporter(@NonNull Context context, @NonNull String apiKey) {
        sContextValidator.validate(context);
        sApiKeyValidator.validate(apiKey);
    }

    public void activateReporter(@NonNull Context context, @NonNull ReporterConfig config) {
        sContextValidator.validate(context);
        sReporterConfigValidator.validate(config);
    }

    public void activate(@NonNull Context context, @NonNull AppMetricaConfig config) {
        sContextValidator.validate(context);
        sConfigValidator.validate(config);
    }

    public void putErrorEnvironmentValue(@NonNull String key, @Nullable String value) {
        sNonNullKeyValidator.validate(key);
    }

    public void initWebViewReporting(@Nullable WebView webView) {
        sNonNullWebViewValidator.validate(webView);
    }

    public boolean reportJsEvent(@Nullable String eventName, @Nullable String eventValue) {
        return sSilentNonEmptyNameValidator.validate(eventName).isValid();
    }

    public boolean reportJsInitEvent(@Nullable String value) {
       return sSilentNonEmptyValueValidator.validate(value).isValid();
    }

    public void requestStartupParams(
            @NonNull final Context context,
            @NonNull final StartupParamsCallback callback,
            @NonNull final List<String> params
    ) {
        sContextValidator.validate(context);
        sCallbackValidator.validate(callback);
    }

    public void getUuid(@NonNull Context context) {
        sCallbackValidator.validate(context);
    }

    public void registerAnrListener(@NonNull AnrListener listener) {
        sAnrListenerValidator.validate(listener);
    }
}
