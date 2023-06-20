package io.appmetrica.analytics.impl.proxy.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.IReporter;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator;
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.Map;

public class ReporterBarrier implements IReporter {

    static final Validator<String> sEventNameValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Event name")
    );
    static final Validator<String> sErrorMessageValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Error message")
    );
    static final Validator<String> sErrorIdentifierValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Error identifier")
    );
    static final Validator<Throwable> sUnhandledExceptionValidator = new ThrowIfFailedValidator<Throwable>(
            new NonNullValidator<Throwable>("Unhandled exception")
    );
    static final Validator<String> sStringExceptionValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Unhandled exception")
    );
    static final Validator<UserProfile> sUserProfileValidator = new ThrowIfFailedValidator<UserProfile>(
            new NonNullValidator<UserProfile>("User profile")
    );
    static final Validator<Revenue> sRevenueNonNullValidator = new ThrowIfFailedValidator<Revenue>(
            new NonNullValidator<Revenue>("Revenue")
    );
    static final Validator<AdRevenue> sAdRevenueNonNullValidator = new ThrowIfFailedValidator<>(
            new NonNullValidator<AdRevenue>("AdRevenue")
    );

    static final Validator<ECommerceEvent> sECommerceNonNullValidator = new ThrowIfFailedValidator<ECommerceEvent>(
            new NonNullValidator<ECommerceEvent>("ECommerceEvent")
    );

    @NonNull
    private final PluginsBarrier pluginsBarrier;

    public ReporterBarrier() {
        this(new PluginsBarrier());
    }

    @VisibleForTesting
    ReporterBarrier(@NonNull PluginsBarrier pluginsBarrier) {
        this.pluginsBarrier = pluginsBarrier;
    }

    @Override
    public void reportEvent(@NonNull String eventName) throws ValidationException {
        sEventNameValidator.validate(eventName);
    }

    @Override
    public void reportEvent(@NonNull String eventName, @Nullable String jsonValue) throws ValidationException {
        sEventNameValidator.validate(eventName);
    }

    @Override
    public void reportEvent(@NonNull String eventName, @Nullable Map<String, Object> attributes)
            throws ValidationException {
        sEventNameValidator.validate(eventName);
    }

    @Override
    public void reportError(@NonNull String message, @Nullable Throwable error) throws ValidationException {
        sErrorMessageValidator.validate(message);
    }

    @Override
    public void reportError(@NonNull String identifier, @Nullable String message) {
        sErrorIdentifierValidator.validate(identifier);
    }

    @Override
    public void reportError(
            @NonNull String identifier,
            @Nullable String message,
            @Nullable Throwable nonNullError
    ) {
        sErrorIdentifierValidator.validate(identifier);
    }

    @Override
    public void reportUnhandledException(@NonNull Throwable exception) throws ValidationException {
        sUnhandledExceptionValidator.validate(exception);
    }

    @Override
    public void resumeSession() {

    }

    @Override
    public void pauseSession() {

    }

    @Override
    public void setUserProfileID(@Nullable String profileID) {}

    @Override
    public void reportUserProfile(@NonNull UserProfile profile) throws ValidationException {
        sUserProfileValidator.validate(profile);
    }

    @Override
    public void reportRevenue(@NonNull Revenue revenue) throws ValidationException {
        sRevenueNonNullValidator.validate(revenue);
    }

    @Override
    public void reportECommerce(@NonNull ECommerceEvent event) {
        sECommerceNonNullValidator.validate(event);
    }

    @Override
    public void setStatisticsSending(boolean enabled) {

    }

    @NonNull
    @Override
    public PluginsBarrier getPluginExtension() {
        return pluginsBarrier;
    }

    @Override
    public void reportAdRevenue(@NonNull AdRevenue adRevenue) {
        sAdRevenueNonNullValidator.validate(adRevenue);
    }

    @Override
    public void putAppEnvironmentValue(@NonNull String key, @Nullable String value) {

    }

    @Override
    public void clearAppEnvironment() {

    }

    @Override
    public void sendEventsBuffer() {

    }
}
