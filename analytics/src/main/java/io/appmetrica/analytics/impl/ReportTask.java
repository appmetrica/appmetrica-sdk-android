package io.appmetrica.analytics.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.db.DBUtils;
import io.appmetrica.analytics.coreutils.internal.io.GZIPCompressor;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.event.DbEventModel;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter;
import io.appmetrica.analytics.impl.db.session.DbSessionModel;
import io.appmetrica.analytics.impl.preparer.EventFromDbModel;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.request.appenders.ReportParamsAppender;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.telephony.SimInfo;
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider;
import io.appmetrica.analytics.impl.telephony.TelephonyInfoAdapter;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.BytesTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.DefaultNetworkResponseHandler;
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer;
import io.appmetrica.analytics.networktasks.internal.RequestBodyEncrypter;
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder;
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.networktasks.internal.SendingDataTaskHelper;
import io.appmetrica.analytics.networktasks.internal.UnderlyingNetworkTask;
import io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONObject;

import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session;
import static io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc;

public class ReportTask implements UnderlyingNetworkTask {

    private static final String TAG = "[ReportTask]";

    private static final int SESSION_ID_FIELD_NUMBER = 1;
    private static final int SESSION_DESC_FIELD_NUMBER = 2;
    private static final int REPORT_REQUEST_PARAMETERS_FILED_NUMBER = 4;
    private static final int REPORT_MESSAGE_NETWORK_INTERFACES_NUMBER = 8;
    private static final int REPORT_MESSAGE_SIM_INFO_NUMBER = 10;
    private static final int ENVIRONMENT_VARIABLE_FIELD_NUMBER = 7;
    private static final int EVENT_FIELD_NUMBER = 3;

    private static final int MAX_EVENT_COUNT_PER_REQUEST = 100;

    private static final String PROTOBUF_ERROR_EVENT_NAME = "protobuf_serialization_error";

    @NonNull
    private final ComponentUnit mComponent;

    @NonNull
    private final Map<String, String> mQueryValues = new LinkedHashMap<String, String>();
    @Nullable
    private DbNetworkTaskConfig mDbReportRequestConfig;

    private EventProto.ReportMessage mProtoReportMessage;

    @NonNull
    private final DatabaseHelper mDbHelper;
    @Nullable
    private List<Long> mAllInternalSessionsIds;

    private int mReportDataSize = 0;
    private int eventsCount = 0;
    private int mEnvironmentSize = -1;

    @Nullable
    private RetrievedSessions mMessageToSend;

    @NonNull
    private final Trimmer<byte[]> mTrimmer;
    @NonNull
    private final PublicLogger mPublicLogger;
    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;
    @NonNull
    private final IReporterExtended mSelfReporter;
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

    public ReportTask(@NonNull final ComponentUnit component,
                      @NonNull final ReportParamsAppender paramsAppender,
                      @NonNull final LazyReportConfigProvider reportConfigProvider,
                      @NonNull final FullUrlFormer<ReportRequestConfig> fullUrlFormer,
                      @NonNull final RequestDataHolder requestDataHolder,
                      @NonNull final ResponseDataHolder responseDataHolder,
                      @NonNull final RequestBodyEncrypter requestBodyEncrypter) {
        this(
                component,
                paramsAppender,
                reportConfigProvider,
                fullUrlFormer,
                requestDataHolder,
                responseDataHolder,
                component.getDbHelper(),
                component.getPublicLogger(),
                component.getVitalComponentDataProvider(),
                requestBodyEncrypter
        );
    }

    private ReportTask(@NonNull final ComponentUnit component,
                       @NonNull final ReportParamsAppender paramsAppender,
                       @NonNull final LazyReportConfigProvider reportConfigProvider,
                       @NonNull final FullUrlFormer<ReportRequestConfig> fullUrlFormer,
                       @NonNull final RequestDataHolder requestDataHolder,
                       @NonNull final ResponseDataHolder responseDataHolder,
                       @NonNull final DatabaseHelper dbHelper,
                       @NonNull final PublicLogger publicLogger,
                       @NonNull final VitalComponentDataProvider vitalComponentDataProvider,
                       @NonNull final RequestBodyEncrypter requestBodyEncrypter) {
        this(
                component,
                publicLogger,
                dbHelper,
                paramsAppender,
                vitalComponentDataProvider,
                reportConfigProvider,
                new BytesTrimmer(
                        EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE,
                        "event value in ReportTask",
                        publicLogger
                ),
                AppMetricaSelfReportFacade.getReporter(),
                fullUrlFormer,
                requestDataHolder,
                responseDataHolder,
                requestBodyEncrypter
        );
    }

    @VisibleForTesting
    ReportTask(@NonNull final ComponentUnit component,
               @NonNull final PublicLogger publicLogger,
               @NonNull final DatabaseHelper dbHelper,
               @NonNull final ReportParamsAppender paramsAppender,
               @NonNull final VitalComponentDataProvider vitalComponentDataProvider,
               @NonNull final LazyReportConfigProvider reportConfigProvider,
               @NonNull final BytesTrimmer bytesTrimmer,
               @NonNull final IReporterExtended selfReporter,
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
        mDbHelper = dbHelper;
        mPublicLogger = publicLogger;
        mTrimmer = bytesTrimmer;
        this.vitalComponentDataProvider = vitalComponentDataProvider;
        this.configProvider = reportConfigProvider;
        mSelfReporter = selfReporter;
        this.requestDataHolder = requestDataHolder;
        this.responseDataHolder = responseDataHolder;
        this.fullUrlFormer = fullUrlFormer;
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
                DebugLogger.warning(TAG, "Something was wrong while filling request parameters.\n%s", exception);
                withEmptyRequestConfig();
            }
        } else {
            withEmptyRequestConfig();
        }
        DebugLogger.info(TAG,"inited mDbReportRequestConfig: %s", mDbReportRequestConfig);
    }

    private void withEmptyRequestConfig() {
        mDbReportRequestConfig = new DbNetworkTaskConfig();
        paramsAppender.setDbReportRequestConfig(mDbReportRequestConfig);
    }

    @NonNull
    private EventProto.ReportMessage createProtoReportMessage(@NonNull RetrievedSessions messageToSend,
                                                              @NonNull List<String> certificates,
                                                              @NonNull ReportRequestConfig requestConfig) {
        EventProto.ReportMessage reportMessage = new EventProto.ReportMessage();
        EventProto.ReportMessage.RequestParameters requestParameters = new EventProto.ReportMessage.RequestParameters();
        // mDbReportRequestConfig is NonNull because createProtoReportMessage is called after its initialization
        // (withQueryValues)
        requestParameters.uuid =
                WrapUtils.getOrDefaultIfEmpty(mDbReportRequestConfig.uuid, requestConfig.getUuid());
        requestParameters.deviceId =
                WrapUtils.getOrDefaultIfEmpty(mDbReportRequestConfig.deviceId, requestConfig.getDeviceId());
        mReportDataSize += CodedOutputByteBufferNano
                .computeMessageSize(REPORT_REQUEST_PARAMETERS_FILED_NUMBER, requestParameters);
        reportMessage.reportRequestParameters = requestParameters;
        fillTelephonyProviderInfo(reportMessage);
        reportMessage.sessions = messageToSend.sessions.
                toArray(new EventProto.ReportMessage.Session[messageToSend.sessions.size()]);
        reportMessage.appEnvironment = extractEnvironment(messageToSend.environment);
        reportMessage.certificatesSha1Fingerprints = certificates.toArray(new String[certificates.size()]);

        mReportDataSize += CodedOutputByteBufferNano.computeTagSize(REPORT_MESSAGE_NETWORK_INTERFACES_NUMBER);
        return reportMessage;
    }

    private void fillTelephonyProviderInfo(@NonNull final EventProto.ReportMessage reportMessage) {
        final TelephonyDataProvider telephonyDataProvider =
            GlobalServiceLocator.getInstance().getTelephonyDataProvider();
         telephonyDataProvider.adoptSimInfo(new TelephonyInfoAdapter<List<SimInfo>>() {
             @Override
             public void adopt(List<SimInfo> value) {
                 fillSimInfo(value, reportMessage);
             }

             private void fillSimInfo(@NonNull List<SimInfo> simInfos,
                                      @NonNull EventProto.ReportMessage reportMessage) {
                 if (!Utils.isNullOrEmpty(simInfos)) {
                     reportMessage.simInfo = new EventProto.ReportMessage.SimInfo[simInfos.size()];

                     for (int i = 0; i < simInfos.size(); i ++) {
                         SimInfo simInfo = simInfos.get(i);
                         reportMessage.simInfo[i] = ProtobufUtils.buildSimInfo(simInfo);
                         mReportDataSize += CodedOutputByteBufferNano.computeMessageSizeNoTag(reportMessage.simInfo[i]);
                         mReportDataSize += CodedOutputByteBufferNano.computeTagSize(REPORT_MESSAGE_SIM_INFO_NUMBER);
                     }
                 }
             }
         });
    }

    @Override
    public boolean onCreateTask() {
        DebugLogger.info(TAG, "onCreateTask: %s", description());
        final List<ContentValues> queryParameters = mComponent.getDbHelper().collectAllQueryParameters();

        if (queryParameters.isEmpty()) {
            DebugLogger.info(TAG, "Could not create task %s: queryParameters are empty", description());
            return false;
        }

        withQueryValues(queryParameters.get(0));

        ReportRequestConfig requestConfig = configProvider.getConfig();
        DebugLogger.info(TAG, "Apply config %s", requestConfig);

        final List<String> certificates = requestConfig.getCertificates();
        if (Utils.isNullOrEmpty(certificates)) {
            DebugLogger.info(TAG, "Could not create task %s: no certificates", description());
            return false;
        }

        fullUrlFormer.setHosts(requestConfig.getReportHosts());
        if (!requestConfig.isReadyForSending() || Utils.isNullOrEmpty(fullUrlFormer.getAllHosts())) {
            DebugLogger.info(TAG, "Could not create task %s: not ready for sending", description());
            return false;
        }

        mAllInternalSessionsIds = null;

        mMessageToSend = getSessions(requestConfig);
        DebugLogger.info(TAG, "Selected for sending %s events", eventsCount);

        // Check if no sessions to report
        if (mMessageToSend.sessions.isEmpty()) {
            DebugLogger.info(TAG, "Could not create task %s: empty sessions", description());
            return false;
        }

        mRequestId = vitalComponentDataProvider.getReportRequestId() + 1;
        paramsAppender.setRequestId(mRequestId);
        mProtoReportMessage = createProtoReportMessage(mMessageToSend, certificates, requestConfig);

        mAllInternalSessionsIds = mMessageToSend.internalSessionsIds;

        sendingDataTaskHelper.prepareAndSetPostData(MessageNano.toByteArray(mProtoReportMessage));

        return true;
    }

    @Override
    public void onPerformRequest() {
        DebugLogger.info(TAG, "onPerformRequest (%s)", description());
        sendingDataTaskHelper.onPerformRequest();
    }

    private EventProto.ReportMessage.EnvironmentVariable[] extractEnvironment(JSONObject data) {
        int envLength = data.length();
        if (envLength > 0) {
            final EventProto.ReportMessage.EnvironmentVariable[] variables =
                    new EventProto.ReportMessage.EnvironmentVariable[envLength];
            Iterator<String> appEnvironmentKeys = data.keys();
            int i = 0;
            while (appEnvironmentKeys.hasNext()) {
                String key = appEnvironmentKeys.next();
                try {
                    final EventProto.ReportMessage.EnvironmentVariable variable =
                            new EventProto.ReportMessage.EnvironmentVariable();
                    variable.name = key;
                    variable.value = data.getString(key);
                    variables[i] = variable;
                } catch (Throwable e) {
                    DebugLogger.error(TAG, e, "Can not find string value for key %s", key);
                }
                i++;
            }
            return variables;
        } else {
            return null;
        }
    }

    private void cleanPostedData(boolean isBadRequest) {
        saveRequestId();
        Session[] listSession = mProtoReportMessage.sessions;

        for (int sessionIndex = 0; sessionIndex < listSession.length; ++ sessionIndex) {
            try {
                final Session session = listSession[sessionIndex];
                // mAllInternalSessionsIds is NonNull because it is initialized in onCreateTask
                final long internalSessionId = mAllInternalSessionsIds.get(sessionIndex);
                final SessionType sessionType = ProtobufUtils.sessionTypeToInternal(session.sessionDesc.sessionType);

                mDbHelper.removeTop(internalSessionId, sessionType.getCode(), session.events.length, isBadRequest);
                ProtobufUtils.logSessionEvents(session);
            } catch (Throwable ex) {
                DebugLogger.error(TAG, ex, "Something went wrong while removing session from db");
            }
        }

        int count = mDbHelper.removeEmptySessions(mComponent.getSessionManager()
                .getThresholdSessionIdForActualSessions());
        DebugLogger.info(TAG, "Remove %s sessions", String.valueOf(count));
    }

    private void saveRequestId() {
        DebugLogger.info(TAG, "save request id: %d", mRequestId);
        vitalComponentDataProvider.setReportRequestId(mRequestId);
    }

    @Override
    public boolean onRequestComplete() {
        boolean successful = sendingDataTaskHelper.isResponseValid();
        DebugLogger.info(TAG, "onRequestComplete (%s) with success = %b", description(), successful);
        return successful;
    }

    @Override
    public void onPostRequestComplete(boolean success) {
        DebugLogger.info(TAG, "onPostRequestComplete (%s) with success = %b", description(), success);
        if (success) {
            cleanPostedData(false);
        } else if (Utils.isBadRequest(responseDataHolder.getResponseCode())) {
            DebugLogger.info(TAG, "Bad request (%s)", description());
            cleanPostedData(true);
        }
        if (success) {
            logSentEvents();
        }
    }

    private void logSentEvents() {
        if (mPublicLogger.isEnabled()) {
            // mMessageToSend is NonNull because logSentEvents is called only after its initialization
            for (int i = 0; i < mMessageToSend.sessions.size(); i++) {
                mPublicLogger.logSessionEvents(mMessageToSend.sessions.get(i), "Event sent");
            }
        }
    }

    @SuppressWarnings("checkstyle:methodLength")
    @NonNull
    protected RetrievedSessions getSessions(@NonNull ReportRequestConfig config) {
        final List<Session> allSessions = new ArrayList<Session>();
        final List<Long> internalSessionsIDs = new ArrayList<Long>();
        AppEnvironment.EnvironmentRevision latestRevision = null;
        JSONObject environmentJSON = new JSONObject();
        Cursor cursor = null;

        final List<Throwable> exceptions = new ArrayList<Throwable>();
        try {
            cursor = getSessionsCursor();
            if (cursor != null) {
                // EventsCount incrementing in getSession method during adding events to request proto
                while (cursor.moveToNext() && eventsCount < MAX_EVENT_COUNT_PER_REQUEST) {
                    final ContentValues sessionValues = new ContentValues();
                    DBUtils.cursorRowToContentValues(cursor, sessionValues);
                    final DbSessionModel sessionModel = new DbSessionModelConverter().toModel(sessionValues);
                    final Long sessionId = sessionModel.getId();
                    if (sessionId == null) {
                        DebugLogger.error(TAG, "no session_id in values: %s", sessionValues.toString());
                        continue;
                    }

                    final EventProto.ReportMessage.Time time = ProtobufUtils.buildTime(
                        sessionModel.getDescription().getStartTime(),
                        sessionModel.getDescription().getServerTimeOffset(),
                        sessionModel.getDescription().getObtainedBeforeFirstSynchronization()
                    );

                    SessionDesc sessionDesc = ProtobufUtils.buildSessionDesc(
                        config.getLocale(),
                        sessionModel.getType(),
                        time);

                    mReportDataSize += CodedOutputByteBufferNano.computeUInt64Size(
                            SESSION_ID_FIELD_NUMBER,
                            Long.MAX_VALUE
                    );
                    mReportDataSize += CodedOutputByteBufferNano.computeMessageSize(
                            SESSION_DESC_FIELD_NUMBER,
                            sessionDesc
                    );
                    if (mReportDataSize >= EventLimitationProcessor.SESSIONS_DATA_MAX_SIZE) {
                        break;
                    }

                    RetrievedSession session = getSession(
                            sessionId,
                            sessionDesc,
                            config,
                            exceptions,
                            allSessions.size()
                    );
                    if (session != null) {
                        if (null == latestRevision) {
                            latestRevision = session.environmentRevision;
                        } else if (!latestRevision.equals(session.environmentRevision)) {
                            break;
                        }
                        internalSessionsIDs.add(sessionId);
                        allSessions.add(session.session);
                        if (!TextUtils.isEmpty(session.environmentRevision.value)) {
                            try {
                                environmentJSON = new JSONObject(session.environmentRevision.value);
                            } catch (Throwable e) {
                                DebugLogger.error(TAG, e, "Some problems while parsing environment");
                            }
                        }
                        if (session.nextEventWithOtherEnvironment) {
                            break;
                        }
                    }
                }
            } else {
                DebugLogger.error(TAG, "no sessions cursor");
            }
        } catch (Throwable ex) {
            DebugLogger.error(TAG, ex, "Some problems while getting sessions");
            exceptions.add(ex);
        } finally {
            Utils.closeCursor(cursor);
        }
        for (Throwable exception : exceptions) {
            mSelfReporter.reportError(
                    PROTOBUF_ERROR_EVENT_NAME,
                    exception
            );
        }
        return new RetrievedSessions(allSessions, internalSessionsIDs, environmentJSON);
    }

    @NonNull
    private AppEnvironment.EnvironmentRevision getEnvironmentRevision(@NonNull ContentValues cv) {
        final DbEventModel eventModel = new DbEventModelConverter().toModel(cv);
        return new AppEnvironment.EnvironmentRevision(
            WrapUtils.getOrDefault(
                eventModel.getDescription().getAppEnvironment(),
                StringUtils.EMPTY
            ),
            WrapUtils.getOrDefault(
                eventModel.getDescription().getAppEnvironmentRevision(),
                0L
            )
        );
    }

    private int computeEnvironmentSize(@NonNull AppEnvironment.EnvironmentRevision revision) {
        try {
            int size = 0;
            JSONObject object = new JSONObject(revision.value);
            EventProto.ReportMessage.EnvironmentVariable[] env = extractEnvironment(object);
            if (env != null) {
                for (EventProto.ReportMessage.EnvironmentVariable variable : env) {
                    size += CodedOutputByteBufferNano.computeMessageSize(ENVIRONMENT_VARIABLE_FIELD_NUMBER, variable);
                }
            }
            return size;
        } catch (Throwable ignored) {

        }
        return 0;
    }

    @SuppressWarnings("checkstyle:methodLength")
    @Nullable
    @VisibleForTesting
    RetrievedSession getSession(final long sessionId,
                                final SessionDesc sessionDesc,
                                @NonNull ReportRequestConfig config,
                                @NonNull List<Throwable> exceptions,
                                final int sessionNumber) {
        final Session session = new Session();
        session.id = sessionId;
        session.sessionDesc = sessionDesc;
        SessionType sessionType = ProtobufUtils.sessionTypeToInternal(sessionDesc.sessionType);
        AppEnvironment.EnvironmentRevision latestRevision = null;
        boolean nextEventHasDifferentRevision = false;
        RetrievedSession retrievedSession = null;

        Cursor cursor = null;
        try {
            cursor = getReportsCursor(sessionId, sessionType);
            if (cursor != null) {
                final List<Session.Event> eventsOfSession = new ArrayList<Session.Event>();

                while (cursor.moveToNext() && eventsCount < MAX_EVENT_COUNT_PER_REQUEST) {
                    ContentValues contentValues = new ContentValues();
                    DBUtils.cursorRowToContentValues(cursor, contentValues);
                    final Session.Event sessionEvent = getEvent(contentValues, config, exceptions);
                    if (sessionEvent != null) {
                        AppEnvironment.EnvironmentRevision revision = getEnvironmentRevision(contentValues);
                        if (latestRevision == null) {
                            latestRevision = revision;
                            if (mEnvironmentSize < 0) {
                                mEnvironmentSize = computeEnvironmentSize(latestRevision);
                                mReportDataSize += mEnvironmentSize;
                            }
                        } else if (!latestRevision.equals(revision)) {
                            nextEventHasDifferentRevision = true;
                            break;
                        }

                        cutValueSize(sessionEvent);

                        mReportDataSize += CodedOutputByteBufferNano.computeMessageSize(
                                EVENT_FIELD_NUMBER,
                                sessionEvent
                        );
                        boolean isEventExtended = eventsOfSession.isEmpty() && sessionNumber == 0;
                        if (isEventsLimitExceeded(isEventExtended)) {
                            break;
                        }

                    } else {
                        DebugLogger.warning(TAG, "Event #%d in session %d is null", eventsOfSession.size(), sessionId);
                    }
                    eventsOfSession.add(sessionEvent);
                    eventsCount++;
                }

                if (eventsOfSession.size() > 0) {
                    session.events = eventsOfSession.toArray(new Session.Event[eventsOfSession.size()]);
                    DebugLogger.info(
                        TAG,
                        "Session %d, Send %d events with env %d %s",
                        sessionId,
                        eventsOfSession.size(),
                        latestRevision.revisionNumber,
                        latestRevision.value
                    );
                    retrievedSession = new RetrievedSession(session, latestRevision, nextEventHasDifferentRevision);
                }
            } else {
                DebugLogger.error(TAG, "no reports cursor for session: %s", session.toString());
            }
        } catch (Throwable ex) {
            DebugLogger.error(TAG, ex, "Some problems while getting session with id = %d.", sessionId);
            exceptions.add(ex);
        } finally {
            Utils.closeCursor(cursor);
        }

        return retrievedSession;
    }

    private boolean isEventsLimitExceeded(final boolean isEventExtended) {
        if (isEventExtended) {
            return mReportDataSize >= EventLimitationProcessor.EXTENDED_SINGLE_EVENT_SESSION_DATA_MAX_SIZE;
        } else {
            return mReportDataSize >= EventLimitationProcessor.SESSIONS_DATA_MAX_SIZE;
        }
    }

    private void cutValueSize(@NonNull final Session.Event sessionEvent) {
        final byte[] cut = mTrimmer.trim(sessionEvent.value);
        if (sessionEvent.value != cut) {
            sessionEvent.bytesTruncated += getBytesArraySize(sessionEvent.value) - getBytesArraySize(cut);
            sessionEvent.value = cut;
            DebugLogger.info(TAG, "truncated %d bytes", sessionEvent.bytesTruncated);
        }
    }

    private int getBytesArraySize(@Nullable byte[] arr) {
        return arr == null ? 0 : arr.length;
    }

    @Nullable
    @VisibleForTesting
    Session.Event getEvent(@NonNull ContentValues contentValues,
                           @NonNull ReportRequestConfig config,
                           @NonNull List<Throwable> exceptions) {
        try {
            final EventFromDbModel eventModel = new EventFromDbModel(contentValues);
            return ProtobufUtils.getEventPreparer(eventModel.getEventType()).toSessionEvent(eventModel, config);
        } catch (Throwable ex) {
            DebugLogger.error(TAG, ex, "Something went wrong while getting event");
            exceptions.add(ex);
        }
        return null;
    }

    @Nullable
    private Cursor getSessionsCursor() {
        return mDbHelper.querySessions(mQueryValues);
    }

    @Nullable
    private Cursor getReportsCursor(final long sessionId, @NonNull final SessionType sessionType) {
        return mDbHelper.queryReports(sessionId, sessionType);
    }

    @Override
    public void onTaskAdded() {
        DebugLogger.info(TAG, "onTaskAdded: %s", description());
        mComponent.getEventTrigger().disableTrigger();
    }

    @Override
    public void onTaskFinished() {
        DebugLogger.info(TAG, "onTaskFinished: %s", description());
        mComponent.getDbHelper().clearIfTooManyEvents();
        mComponent.getEventTrigger().enableTrigger();
    }

    @Override
    public void onTaskRemoved() {
        DebugLogger.info(TAG, "onTaskRemoved: %s", description());
        mComponent.getEventTrigger().enableTrigger();
    }

    @Override
    public void onSuccessfulTaskFinished() {
        DebugLogger.info(TAG, "onSuccessfulTaskFinished: %s", description());
        mComponent.getEventTrigger().trigger();
    }

    @NonNull
    @Override
    public String description() {
        return "ReportTask_" + mComponent.getComponentId().getApiKey();
    }

    // Stores list of {@code Metrica.ReportMessage.Session} objects and internal IDs of the sessions (in the database)
    static final class RetrievedSessions {

        @NonNull
        final List<Session> sessions;
        @NonNull
        final List<Long> internalSessionsIds;
        @NonNull
        final JSONObject environment;

        RetrievedSessions(@NonNull final List<Session> sessions,
                          @NonNull final List<Long> internalSessionsIDs,
                          @NonNull final JSONObject environment) {
            this.sessions = sessions;
            this.internalSessionsIds = internalSessionsIDs;
            this.environment = environment;
        }
    }

    static final class RetrievedSession {
        @NonNull
        final Session session;
        final AppEnvironment.EnvironmentRevision environmentRevision;
        final boolean nextEventWithOtherEnvironment;

        RetrievedSession(@NonNull Session session,
                         AppEnvironment.EnvironmentRevision environmentRevision,
                         boolean nextEventWithOtherEnvironment) {
            this.session = session;
            this.environmentRevision = environmentRevision;
            this.nextEventWithOtherEnvironment = nextEventWithOtherEnvironment;
        }
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
    public void onShouldNotExecute() {
        // do nothing
    }

    @Override
    public void onUnsuccessfulTaskFinished() {
        // do nothing
    }

    // endregion
}
