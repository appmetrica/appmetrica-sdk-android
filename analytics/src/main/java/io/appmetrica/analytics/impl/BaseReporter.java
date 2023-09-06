package io.appmetrica.analytics.impl;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.Anr;
import io.appmetrica.analytics.impl.crash.client.CustomError;
import io.appmetrica.analytics.impl.crash.client.RegularError;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.UnhandledExceptionFactory;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.profile.UserProfileStorage;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper;
import io.appmetrica.analytics.impl.startup.StartupIdentifiersProvider;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PluginErrorDetailsExtensionKt;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.impl.utils.validation.revenue.RevenueValidator;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.profile.UserProfileUpdate;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PURGE_BUFFER;

public abstract class BaseReporter implements IBaseReporter {

    /*
        Java 15 does not allow static final fields changing. That approach was widely used for public api testing.
        Hopefully, new mockito version could easily mock static methods.
        So I introduced a set of proxy-classes with only one purpose - allow to mock those fields.
     */
    static final class ValidatorProvider {

        private static final Validator<Revenue> sRevenueValidator = new RevenueValidator();

        public static Validator<Revenue> getRevenueValidator() {
            return sRevenueValidator;
        }
    }

    private static final String TAG = "[BaseReporter]";

    private static final Collection<Integer> RESERVED_EVENT_TYPES =
        new HashSet<Integer>(Arrays.asList(
            EventProto.ReportMessage.Session.Event.EVENT_INIT,
            EventProto.ReportMessage.Session.Event.EVENT_FIRST
        ));

    private static final Validator<Userprofile.Profile> sUserProfileNonEmptyValidator =
            new Validator<Userprofile.Profile>() {
                @Override
                public ValidationResult validate(@NonNull Userprofile.Profile data) {
                    return Utils.isNullOrEmpty(data.attributes) ?
                            ValidationResult.failed(this, "attributes list is empty") :
                            ValidationResult.successful(this);
                }
    };

    protected final Context mContext;
    protected final ReporterEnvironment mReporterEnvironment;
    @NonNull protected final PublicLogger mPublicLogger;
    @NonNull
    protected final UnhandledExceptionConverter unhandledExceptionConverter;
    @NonNull
    protected final RegularErrorConverter regularErrorConverter;
    @NonNull
    protected final CustomErrorConverter customErrorConverter;
    @NonNull
    private final AnrConverter anrConverter;

    protected final ReportsHandler mReportsHandler;
    private KeepAliveHandler mKeepAliveHandler;
    private final ProcessDetector processDetector;

    @NonNull
    private final ExtraMetaInfoRetriever mExtraMetaInfoRetriever;
    @NonNull
    private final PluginErrorDetailsConverter pluginErrorDetailsConverter;

    BaseReporter(final Context context,
                 final ReportsHandler reportsHandler,
                 @NonNull ReporterEnvironment reporterEnvironment,
                 @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever,
                 @NonNull ProcessDetector processDetector,
                 @NonNull UnhandledExceptionConverter unhandledExceptionConverter,
                 @NonNull RegularErrorConverter regularErrorConverter,
                 @NonNull CustomErrorConverter customErrorConverter,
                 @NonNull AnrConverter anrConverter,
                 @NonNull PluginErrorDetailsConverter pluginErrorDetailsConverter) {
        mContext = context.getApplicationContext();
        mReportsHandler = reportsHandler;
        mReporterEnvironment = reporterEnvironment;
        mExtraMetaInfoRetriever = extraMetaInfoRetriever;
        this.unhandledExceptionConverter = unhandledExceptionConverter;
        this.regularErrorConverter = regularErrorConverter;
        this.customErrorConverter = customErrorConverter;
        this.anrConverter = anrConverter;
        this.pluginErrorDetailsConverter = pluginErrorDetailsConverter;

        mPublicLogger = LoggerStorage.getOrCreatePublicLogger(
                mReporterEnvironment.getReporterConfiguration().getApiKey());

        mReporterEnvironment.initialize(
                new SimpleMapLimitation(mPublicLogger, ErrorEnvironment.TAG)
        );
        if (BooleanUtils.isTrue(mReporterEnvironment.getReporterConfiguration().isLogEnabled())) {
            mPublicLogger.setEnabled();
        }
        this.processDetector = processDetector;
    }

    public void start() {
        mReportsHandler.reportActivationEvent(mReporterEnvironment);
    }

    void setStartupParamsProvider(final StartupIdentifiersProvider startupParamsProvider) {
        mReporterEnvironment.setConfigIdentifiers(startupParamsProvider);
    }

    void setKeepAliveHandler(KeepAliveHandler keepAliveHandler) {
        mKeepAliveHandler = keepAliveHandler;
    }

    public void putErrorEnvironmentValue(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.fw("Invalid Error Environment (key,value) pair: (%s,%s).", key, value);
            }
        } else {
            mReporterEnvironment.putErrorEnvironmentValue(key, value);
        }
    }

    protected void putAllToErrorEnvironment(Map<String, String> errorEnvironment) {
        if (Utils.isNullOrEmpty(errorEnvironment) == false) {
            for (Map.Entry<String, String> env : errorEnvironment.entrySet()) {
                putErrorEnvironmentValue(env.getKey(), env.getValue());
            }
        }
    }

    protected void putAllToAppEnvironment(final Map<String, String> appEnvironment) {
        if (Utils.isNullOrEmpty(appEnvironment) == false) {
            for (Map.Entry<String, String> envPair : appEnvironment.entrySet()) {
                putAppEnvironmentValue(envPair.getKey(), envPair.getValue());
            }
        }
    }

    @Override
    public void putAppEnvironmentValue(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.fw("Invalid App Environment (key,value) pair: (%s,%s).", key, value);
            }
        } else {
            mReportsHandler.sendAppEnvironmentValue(key, value, mReporterEnvironment);
        }
    }

    @Override
    public void clearAppEnvironment() {
        mReportsHandler.sendClearAppEnvironment(mReporterEnvironment);
    }

    @Override
    public void resumeSession() {
        onResumeForegroundSession(null);
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.i("Resume session");
        }
    }

    void onResumeForegroundSession(String name) {

        mReportsHandler.onResumeForegroundSession();

        // Even if App seems like inactive (but the App is currently used),
        // we still want to keep alive state. We confirm this each half of duration time of session.
        mKeepAliveHandler.onResumeForegroundSession();

        mReportsHandler.reportEvent(
                EventsManager.notifyServiceOnActivityStartReportEntry(name, mPublicLogger),
                mReporterEnvironment
        );

        mReporterEnvironment.onResumeForegroundSession();
    }

    @Override
    public void pauseSession() {
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.i("Pause session");
        }
        onPauseForegroundSession(null);
    }

    void onPauseForegroundSession(String tag) {
        if (!mReporterEnvironment.isForegroundSessionPaused()) {
            mReportsHandler.onPauseForegroundSession();

            // While no interacting with UI elements, we shouldn't keep alive state of App.
            mKeepAliveHandler.onPauseForegroundSession();

            // This line should be processed before reportEvent, otherwise there is a possibility
            // that client won't unbind: https://nda.ya.ru/t/86RmWFxg6Njj6g
            mReporterEnvironment.onPauseForegroundSession();

            mReportsHandler.reportEvent(
                EventsManager.activityEndReportEntry(tag, mPublicLogger),
                mReporterEnvironment
            );
        }
    }

    @Override
    public void reportEvent(@NonNull String eventName) {
        if (mPublicLogger.isEnabled()) {
            logEvent(eventName);
        }
        mReportsHandler.reportEvent(
            EventsManager.regularEventReportEntry(eventName, mPublicLogger),
            mReporterEnvironment
        );
    }

    @Override
    public void reportEvent(@NonNull String eventName, final String jsonValue) {
        if (mPublicLogger.isEnabled()) {
            logEvent(eventName, jsonValue);
        }
        mReportsHandler.reportEvent(
            EventsManager.regularEventReportEntry(eventName, jsonValue, mPublicLogger),
            mReporterEnvironment
        );
    }

    @Override
    public void reportEvent(@NonNull String eventName, @Nullable final Map<String, Object> attributes) {
        Map<String, Object> attributesCopy = CollectionUtils.copyOf(attributes);
        mReportsHandler.reportEvent(
            EventsManager.regularEventReportEntry(eventName, mPublicLogger),
            getEnvironment(),
            attributesCopy
        );
        if (mPublicLogger.isEnabled()) {
            logEvent(eventName, attributesCopy == null ? null : attributesCopy.toString());
        }
    }

    private void logEvent(String name) {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("Event received: ");
            builder.append(WrapUtils.wrapToTag(name));
            mPublicLogger.i(builder.toString());
        }
    }

    private void logEvent(String name, String value) {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("Event received: ");
            builder.append(WrapUtils.wrapToTag(name));
            builder.append(". With value: ");
            builder.append(WrapUtils.wrapToTag(value));
            mPublicLogger.i(builder.toString());
        }
    }

    @Override
    public void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    ) {
        if (reservedEventType(moduleEvent.getType())) {
            YLogger.w("Try to send custom event with event type = %s, reserved for metrica usage only",
                String.valueOf(moduleEvent.getType()));
        } else {
            final CounterReport event = EventsManager.customEventReportEntry(
                moduleEvent.getType(),
                moduleEvent.getName(),
                moduleEvent.getValue(),
                moduleEvent.getEnvironment(),
                moduleEvent.getExtras(),
                mPublicLogger
            );
            mReportsHandler.reportEvent(
                event,
                mReporterEnvironment,
                moduleEvent.getServiceDataReporterType(),
                moduleEvent.getAttributes()
            );
        }
    }

    @Override
    public void setSessionExtra(@NonNull String key, @Nullable byte[] value) {
        mReportsHandler.reportEvent(
            EventsManager.setSessionExtraReportEntry(key, value, mPublicLogger),
            mReporterEnvironment
        );
    }

    private boolean reservedEventType(final int type) {
        return RESERVED_EVENT_TYPES.contains(type);
    }

    @Override
    public void reportError(@NonNull String message, @Nullable final Throwable error) {
        RegularError regularError = new RegularError(message, formUnhandledException(error));
        mReportsHandler.reportEvent(EventsManager.regularErrorReportEntry(
                regularError.message,
                MessageNano.toByteArray(regularErrorConverter.fromModel(regularError)),
                mPublicLogger
        ), mReporterEnvironment);
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Error received: %s", WrapUtils.wrapToTag(message));
        }
    }

    @Override
    public void reportError(@NonNull String identifier, @Nullable String message) {
        reportError(identifier, message, (Throwable) null);
    }

    @Override
    public void reportError(
            @NonNull String identifier,
            @Nullable String message,
            @Nullable Throwable error
    ) {
        CustomError customError = new CustomError(
                new RegularError(message, formUnhandledException(error)),
                identifier
        );
        mReportsHandler.reportEvent(
            EventsManager.customErrorReportEntry(
                customError.regularError.message,
                MessageNano.toByteArray(customErrorConverter.fromModel(customError)),
                mPublicLogger
            ),
            mReporterEnvironment
        );
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Error received: id: %s, message: %s",
                WrapUtils.wrapToTag(identifier),
                WrapUtils.wrapToTag(message)
            );
        }
    }

    @NonNull
    private UnhandledException formUnhandledException(@Nullable Throwable throwable) {
        final Throwable originalError;
        final StackTraceElement[] methodCallStacktrace;
        if (throwable == null)  {
            originalError = null;
            methodCallStacktrace = null;
        } else if (throwable instanceof AppMetricaThrowable) {
            originalError = null;
            methodCallStacktrace = throwable.getStackTrace();
        } else {
            originalError = throwable;
            methodCallStacktrace = null;
        }
        return UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                originalError,
                new AllThreads(processDetector.getProcessName()),
                methodCallStacktrace == null ? null : Arrays.asList(methodCallStacktrace),
                mExtraMetaInfoRetriever.getBuildId(),
                mExtraMetaInfoRetriever.isOffline()
        );
    }

    @Override
    public void sendEventsBuffer() {
        YLogger.d("Send event buffer for %s.", mReporterEnvironment.getReporterConfiguration().getApiKey());
        mReportsHandler.reportEvent(
            EventsManager.reportEntry(EVENT_TYPE_PURGE_BUFFER, mPublicLogger),
            mReporterEnvironment
        );
    }

    boolean reportKeepAliveIfNeeded() {
        boolean isAlive = !isPaused();
        if (isAlive) {
            final CounterReport reportData =
                    EventsManager.activityEndReportEntry("", mPublicLogger);
            mReportsHandler.reportEvent(reportData, mReporterEnvironment);
        }

        return isAlive;
    }

    ReporterEnvironment getEnvironment() {
        return mReporterEnvironment;
    }

    @Override
    public void reportUnhandledException(@NonNull Throwable exception) {
        UnhandledException unhandledException = UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                exception,
                new AllThreads(processDetector.getProcessName()),
                null,
                mExtraMetaInfoRetriever.getBuildId(),
                mExtraMetaInfoRetriever.isOffline()
        );
        mReportsHandler.reportUnhandledException(unhandledException, mReporterEnvironment);
        logUnhandledException(unhandledException);
    }

    @Override
    // crash: send synchronously
    public void reportUnhandledException(@NonNull UnhandledException unhandledException) {
        mReportsHandler.reportCrash(unhandledException, mReporterEnvironment);
        logUnhandledException(unhandledException);
    }

    protected void logUnhandledException(@NonNull UnhandledException unhandledException) {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("Unhandled exception received: ");
            builder.append(unhandledException.toString());
            mPublicLogger.i(builder.toString());
        }
    }

    @Override
    public void reportRevenue(@NonNull Revenue revenue) {
        reportRevenueInternal(revenue);
    }

    private void reportRevenueInternal(@NonNull Revenue revenue) {
        ValidationResult revenueValidation = ValidatorProvider.getRevenueValidator().validate(revenue);
        if (revenueValidation.isValid()) {
            mReportsHandler.sendRevenue(new RevenueWrapper(revenue, mPublicLogger), mReporterEnvironment);
            logRevenue(revenue);
        } else {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.w("Passed revenue is not valid. Reason: "
                        + revenueValidation.getDescription());
            }
        }
    }

    private void logRevenue(@NonNull Revenue revenue) {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("Revenue received ");
            builder.append("for productID: ");
            builder.append(WrapUtils.wrapToTag(revenue.productID));
            builder.append(" of quantity: ");
            builder.append(WrapUtils.wrapToTag(revenue.quantity));
            builder.append(" with price (in micros): ");
            builder.append(revenue.priceMicros);
            builder.append(" ");
            builder.append(revenue.currency);
            mPublicLogger.i(builder.toString());
        }
    }

    @Override
    public void reportAdRevenue(@NonNull AdRevenue adRevenue) {
        mReportsHandler.sendAdRevenue(new AdRevenueWrapper(adRevenue, mPublicLogger), mReporterEnvironment);
        logAdRevenue(adRevenue);
    }

    private void logAdRevenue(@NonNull AdRevenue adRevenue) {
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.i(
                    "AdRevenue Received: AdRevenue{" +
                            "adRevenue=" + adRevenue.adRevenue +
                            ", currency='" + WrapUtils.wrapToTag(adRevenue.currency.getCurrencyCode()) + '\'' +
                            ", adType=" + WrapUtils.wrapToTag(adRevenue.adType) +
                            ", adNetwork='" + WrapUtils.wrapToTag(adRevenue.adNetwork) + '\'' +
                            ", adUnitId='" + WrapUtils.wrapToTag(adRevenue.adUnitId) + '\'' +
                            ", adUnitName='" + WrapUtils.wrapToTag(adRevenue.adUnitName) + '\'' +
                            ", adPlacementId='" + WrapUtils.wrapToTag(adRevenue.adPlacementId) + '\'' +
                            ", adPlacementName='" + WrapUtils.wrapToTag(adRevenue.adPlacementName) + '\'' +
                            ", precision='" + WrapUtils.wrapToTag(adRevenue.precision) + '\'' +
                            ", payload=" +  JsonHelper.mapToJsonString(adRevenue.payload) +
                            '}'
            );
        }
    }

    @Override
    public void reportECommerce(@NonNull ECommerceEvent event) {
        YLogger.debug(ECommerceConstants.FEATURE_TAG + getTag(), "receive e-commerce event: %s", event);
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.i("E-commerce event received: " + event.getPublicDescription());
        }
        mReportsHandler.sendECommerce(event, mReporterEnvironment);
    }

    @Override
    public void reportUserProfile(@NonNull UserProfile profile) {
        reportUserProfileInternal(profile);
    }

    private void reportUserProfileInternal(@NonNull final UserProfile profile) {
        UserProfileStorage storage = new UserProfileStorage();
        UserProfileUpdatePatcher userProfileUpdatePatcher;
        for (UserProfileUpdate userProfileUpdate : profile.getUserProfileUpdates()) {
            userProfileUpdatePatcher = userProfileUpdate.getUserProfileUpdatePatcher();
            userProfileUpdatePatcher.setPublicLogger(mPublicLogger);
            userProfileUpdatePatcher.apply(storage);
        }
        Userprofile.Profile protobuf = storage.toProtobuf();
        ValidationResult result = sUserProfileNonEmptyValidator.validate(protobuf);
        if (result.isValid()) {
            mReportsHandler.sendUserProfile(protobuf, mReporterEnvironment);
            logUserProfile();
        } else {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.w("UserInfo wasn't sent because " + result.getDescription());
            }
        }
    }

    private void logUserProfile() {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("User profile received");
            mPublicLogger.i(builder.toString());
            //todo (ddzina) https://nda.ya.ru/t/uF3haggP6Njj6h
        }
    }

    @Override
    public void setUserProfileID(@Nullable String userProfileID) {
        mReportsHandler.setUserProfileID(userProfileID, mReporterEnvironment);
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("Set user profile ID: ");
            builder.append(WrapUtils.wrapToTag(userProfileID));
            mPublicLogger.i(builder.toString());
        }
    }

    @Override
    public void setDataSendingEnabled(boolean value) {
        mReporterEnvironment.getReporterConfiguration().setDataSendingEnabled(value);
    }

    @Override
    public void reportAnr(@NonNull AllThreads value) {
        Anr anr = new Anr(value, mExtraMetaInfoRetriever.getBuildId(), mExtraMetaInfoRetriever.isOffline());
        mReportsHandler.reportEvent(
            EventsManager.anrEntry(
                MessageNano.toByteArray(anrConverter.fromModel(anr)),
                mPublicLogger
            ),
            mReporterEnvironment
        );
    }

    @Override
    public void reportJsEvent(@NonNull String eventName, @Nullable String eventValue) {
        logEvent(eventName, eventValue);
        mReportsHandler.reportEvent(
            ClientCounterReport.formJsEvent(eventName, eventValue, mPublicLogger),
            mReporterEnvironment
        );
    }

    @Override
    public void reportJsInitEvent(@NonNull String value) {
        mReportsHandler.reportEvent(
            CounterReport.formJsInitEvent(value),
            mReporterEnvironment
        );
    }

    @Override
    public void reportUnhandledException(@NonNull PluginErrorDetails errorDetails) {
        YLogger.debug(TAG, "report unhandled exception from plugin %s",
                PluginErrorDetailsExtensionKt.toLogString(errorDetails));
        UnhandledException unhandledException = pluginErrorDetailsConverter.toUnhandledException(errorDetails);
        mReportsHandler.reportEvent(
            EventsManager.unhandledExceptionReportEntry(
                UnhandledException.getErrorName(unhandledException),
                MessageNano.toByteArray(unhandledExceptionConverter.fromModel(unhandledException)),
                mPublicLogger
            ),
            mReporterEnvironment
        );
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Crash from plugin received: %s", WrapUtils.wrapToTag(errorDetails.getMessage()));
        }
    }

    @Override
    public void reportError(@NonNull PluginErrorDetails errorDetails, @Nullable String message) {
        YLogger.debug(TAG, "report error from plugin. Message: %s, error %s",
                message, PluginErrorDetailsExtensionKt.toLogString(errorDetails));
        RegularError error = pluginErrorDetailsConverter.toRegularError(message, errorDetails);
        mReportsHandler.reportEvent(
            EventsManager.customErrorReportEntry(
                error.message,
                MessageNano.toByteArray(regularErrorConverter.fromModel(error)),
                mPublicLogger
            ),
            mReporterEnvironment
        );
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Error from plugin received: %s", WrapUtils.wrapToTag(message));
        }
    }

    @Override
    public void reportError(@NonNull String identifier,
                            @Nullable String message,
                            @Nullable PluginErrorDetails errorDetails) {
        YLogger.debug(TAG, "report error from plugin. Message: %s, identifier: %s, error %s", message, identifier,
                errorDetails == null ? "null" : PluginErrorDetailsExtensionKt.toLogString(errorDetails));
        CustomError customError = new CustomError(
                pluginErrorDetailsConverter.toRegularError(message, errorDetails), identifier
        );
        mReportsHandler.reportEvent(
            EventsManager.customErrorReportEntry(
                customError.regularError.message,
                MessageNano.toByteArray(customErrorConverter.fromModel(customError)),
                mPublicLogger
            ),
            mReporterEnvironment
        );
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Error with identifier: %s from plugin received: %s",
                    identifier, WrapUtils.wrapToTag(message));
        }
    }

    @NonNull
    @Override
    public IPluginReporter getPluginExtension() {
        return this;
    }

    public boolean isPaused() {
        return mReporterEnvironment.isForegroundSessionPaused();
    }

    protected String getTag() {
        return TAG;
    }
}
