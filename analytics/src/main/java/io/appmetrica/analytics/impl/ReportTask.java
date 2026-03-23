package io.appmetrica.analytics.impl;

import android.content.ContentValues;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.io.GZIPCompressor;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.request.appenders.ReportParamsAppender;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.PublicLogConstructor;
import io.appmetrica.analytics.impl.utils.limitation.BytesTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.DefaultNetworkResponseHandler;
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer;
import io.appmetrica.analytics.networktasks.internal.RequestBodyEncrypter;
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder;
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.networktasks.internal.SendingDataTaskHelper;
import io.appmetrica.analytics.networktasks.internal.UnderlyingNetworkTask;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;

import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session;

public class ReportTask implements UnderlyingNetworkTask {

    private static final String TAG = "[ReportTask]";

    @NonNull
    private final ComponentUnit mComponent;

    @NonNull
    private final Map<String, String> mQueryValues = new LinkedHashMap<String, String>();
    @Nullable
    private DbNetworkTaskConfig mDbReportRequestConfig;

    @NonNull
    private final ReportTaskDbInteractor mDbInteractor;

    @Nullable
    private PreparedReport mPreparedReport;

    @NonNull
    private final ReportMessagePreparer mPreparer;

    @NonNull
    private final PublicLogger mPublicLogger;
    @NonNull
    private final ReportParamsAppender paramsAppender;
    @NonNull
    private final FullUrlFormer<ReportRequestConfig> fullUrlFormer;
    @NonNull
    private final LazyReportConfigProvider configProvider;
    @NonNull
    private final RequestDataHolder requestDataHolder;
    @NonNull
    private final ResponseDataHolder responseDataHolder;
    @NonNull
    private final SendingDataTaskHelper sendingDataTaskHelper;

    private int mRequestId;

    private boolean shouldTriggerSendingEvents = false;

    public ReportTask(@NonNull final ComponentUnit component,
                      @NonNull final ReportParamsAppender paramsAppender,
                      @NonNull final LazyReportConfigProvider reportConfigProvider,
                      @NonNull final FullUrlFormer<ReportRequestConfig> fullUrlFormer,
                      @NonNull final RequestDataHolder requestDataHolder,
                      @NonNull final ResponseDataHolder responseDataHolder,
                      @NonNull final RequestBodyEncrypter requestBodyEncrypter) {
        this.sendingDataTaskHelper = new SendingDataTaskHelper(
                requestBodyEncrypter,
                new GZIPCompressor(),
                requestDataHolder,
                responseDataHolder,
                new DefaultNetworkResponseHandler()
        );
        this.paramsAppender = paramsAppender;
        mComponent = component;
        mDbInteractor = new ReportTaskDbInteractor(component);
        mPublicLogger = component.getPublicLogger();
        this.configProvider = reportConfigProvider;
        this.requestDataHolder = requestDataHolder;
        this.responseDataHolder = responseDataHolder;
        this.fullUrlFormer = fullUrlFormer;
        mPreparer = new ReportMessagePreparer(
                mDbInteractor,
                new BytesTrimmer(
                        EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE,
                        "event value in ReportTask",
                        mPublicLogger
                ),
                AppMetricaSelfReportFacade.getReporter(),
                GlobalServiceLocator.getInstance().getTelephonyDataProvider()
        );
    }

    private void withQueryValues(@NonNull ContentValues dbValues) {
        mQueryValues.clear();
        for (final Map.Entry<String, Object> entry : dbValues.valueSet()) {
            mQueryValues.put(entry.getKey(), entry.getValue().toString());
        }

        final String value =
                dbValues.getAsString(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS);

        if (!TextUtils.isEmpty(value)) {
            try{
                final JsonHelper.OptJSONObject requestParameters = new JsonHelper.OptJSONObject(value);
                mDbReportRequestConfig = new DbNetworkTaskConfig(requestParameters);
                paramsAppender.setDbReportRequestConfig(mDbReportRequestConfig);
            } catch (Throwable exception) {
                DebugLogger.INSTANCE.warning(
                    TAG,
                    "Something was wrong while filling request parameters.\n%s",
                    exception
                );
                withEmptyRequestConfig();
            }
        } else {
            withEmptyRequestConfig();
        }
        DebugLogger.INSTANCE.info(TAG,"inited mDbReportRequestConfig: %s", mDbReportRequestConfig);
    }

    private void withEmptyRequestConfig() {
        mDbReportRequestConfig = new DbNetworkTaskConfig();
        paramsAppender.setDbReportRequestConfig(mDbReportRequestConfig);
    }

    @Override
    public boolean onCreateTask() {
        DebugLogger.INSTANCE.info(TAG, "onCreateTask: %s", description());
        final ContentValues queryParameters = mDbInteractor.collectAllQueryParameters();

        if (queryParameters == null) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Could not create task %s: queryParameters are empty",
                description()
            );
            return false;
        }

        withQueryValues(queryParameters);

        ReportRequestConfig requestConfig = configProvider.getConfig();
        DebugLogger.INSTANCE.info(TAG, "Apply config %s", requestConfig);

        final List<String> certificates = requestConfig.getCertificates();
        if (Utils.isNullOrEmpty(certificates)) {
            DebugLogger.INSTANCE.info(TAG, "Could not create task %s: no certificates", description());
            return false;
        }

        fullUrlFormer.setHosts(requestConfig.getReportHosts());
        if (!requestConfig.isReadyForSending() || Utils.isNullOrEmpty(fullUrlFormer.getAllHosts())) {
            DebugLogger.INSTANCE.info(TAG, "Could not create task %s: not ready for sending", description());
            shouldTriggerSendingEvents = true;
            return false;
        }

        final DbNetworkTaskConfig dbRequestConfig =
                mDbReportRequestConfig != null ? mDbReportRequestConfig : new DbNetworkTaskConfig();
        mPreparedReport = mPreparer.prepare(mQueryValues, requestConfig, certificates, dbRequestConfig);
        if (mPreparedReport == null) {
            DebugLogger.INSTANCE.info(TAG, "Could not create task %s: empty sessions", description());
            return false;
        }

        mRequestId = mPreparedReport.getRequestId();
        paramsAppender.setRequestId(mRequestId);
        sendingDataTaskHelper.prepareAndSetPostData(MessageNano.toByteArray(mPreparedReport.getReportMessage()));

        return true;
    }

    @Override
    public void onPerformRequest() {
        DebugLogger.INSTANCE.info(TAG, "onPerformRequest (%s)", description());
        sendingDataTaskHelper.onPerformRequest();
    }

    private void cleanPostedData(boolean isBadRequest) {
        mDbInteractor.cleanPostedData(
                mPreparedReport.getReportMessage().sessions,
                mPreparedReport.getInternalSessionsIds(),
                mRequestId,
                isBadRequest
        );
    }

    @Override
    public boolean onRequestComplete() {
        boolean successful = sendingDataTaskHelper.isResponseValid();
        DebugLogger.INSTANCE.info(TAG, "onRequestComplete (%s) with success = %b", description(), successful);
        return successful;
    }

    @Override
    public void onPostRequestComplete(boolean success) {
        DebugLogger.INSTANCE.info(
            TAG,
            "onPostRequestComplete (%s) with success = %b",
            description(),
            success
        );
        if (success) {
            cleanPostedData(false);
        } else if (Utils.isBadRequest(responseDataHolder.getResponseCode())) {
            DebugLogger.INSTANCE.info(TAG, "Bad request (%s)", description());
            cleanPostedData(true);
        }
        if (success) {
            logSentEvents();
        }
    }

    private void logSentEvents() {
        for (Session session : mPreparedReport.getReportMessage().sessions) {
            for (Session.Event event : session.events) {
                if (event != null) {
                    String log = PublicLogConstructor.constructEventLogForProtoEvent(event, "Event sent");
                    if (log != null) {
                        mPublicLogger.info(log);
                    }
                }
            }
        }
    }

    @Override
    public void onTaskAdded() {
        DebugLogger.INSTANCE.info(TAG, "onTaskAdded: %s", description());
        mComponent.getEventTrigger().disableTrigger();
    }

    @Override
    public void onTaskFinished() {
        DebugLogger.INSTANCE.info(TAG, "onTaskFinished: %s", description());
    }

    @Override
    public void onTaskRemoved() {
        DebugLogger.INSTANCE.info(
            TAG,
            "onTaskRemoved: %s - should trigger events sending = %s",
            description(),
            shouldTriggerSendingEvents
        );
        mComponent.getEventTrigger().enableTrigger();
        if (shouldTriggerSendingEvents) {
            mComponent.getEventTrigger().triggerAsync();
        }
    }

    @Override
    public void onSuccessfulTaskFinished() {
        DebugLogger.INSTANCE.info(TAG, "onSuccessfulTaskFinished: %s", description());
        shouldTriggerSendingEvents = true;
    }

    @Override
    public void onShouldNotExecute() {
        DebugLogger.INSTANCE.info(TAG, "onShouldNotExecute: %s", description());
        shouldTriggerSendingEvents = true;
    }

    @NonNull
    @Override
    public String description() {
        return "ReportTask_" + mComponent.getComponentId().getAnonymizedApiKey();
    }

    @Override
    @Nullable
    public RetryPolicyConfig getRetryPolicyConfig() {
        return mComponent.getFreshReportRequestConfig().getRetryPolicyConfig();
    }

    @NonNull
    @Override
    public RequestDataHolder getRequestDataHolder() {
        return requestDataHolder;
    }

    @NonNull
    @Override
    public ResponseDataHolder getResponseDataHolder() {
        return responseDataHolder;
    }

    @NonNull
    @Override
    public FullUrlFormer<?> getFullUrlFormer() {
        return fullUrlFormer;
    }

    @Nullable
    @Override
    public SSLSocketFactory getSslSocketFactory() {
        return GlobalServiceLocator.getInstance().getSslSocketFactoryProvider().getSslSocketFactory();
    }

    // region overridden methods with default implementation

    @Override
    public void onRequestError(@Nullable Throwable error) {
        // do nothing
    }

    @Override
    public void onUnsuccessfulTaskFinished() {
        // do nothing
    }

    // endregion
}
