package io.appmetrica.analytics.impl.component.session;

import android.content.ContentValues;
import android.text.TextUtils;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;

import static io.appmetrica.analytics.impl.component.session.SessionDefaults.MIN_VALID_UI_SESSION_ID;

public class Session {

    private final ComponentUnit mComponent;
    private final SessionStorage mSessionStorage;
    private final SessionArgumentsInternal mArguments;

    private long mId;
    private long mCreationTime;
    private AtomicLong mCurrentReportId;
    private boolean mAliveNeeded;
    private volatile SessionRequestParams mSessionRequestParams;
    private long mSleepStartSeconds;
    private long mLastEventTimeOffset;

    private SystemTimeProvider mSystemTimeProvider;

    Session(ComponentUnit component, SessionStorage storage, SessionArgumentsInternal arguments) {
        this(component, storage, arguments, new SystemTimeProvider());
    }

    Session(ComponentUnit component, SessionStorage storage, SessionArgumentsInternal arguments,
            SystemTimeProvider systemTimeProvider) {
        mComponent = component;
        mSessionStorage = storage;
        mArguments = arguments;
        mSystemTimeProvider = systemTimeProvider;
        initializeWithArguments();
    }

    private void initializeWithArguments() {
        mCreationTime = mArguments.getCreationTime(mSystemTimeProvider.elapsedRealtime());
        mId = mArguments.getId(SessionDefaults.INVALID_UI_SESSION_ID);
        mCurrentReportId = new AtomicLong(mArguments.getCurrentReportId(SessionDefaults.INITIAL_REPORT_ID));
        mAliveNeeded = mArguments.isAliveNeeded(true);
        mSleepStartSeconds = mArguments.getSleepStart(SessionDefaults.INITIAL_SESSION_TIME);
        mLastEventTimeOffset = mArguments.getLastEventOffset(mSleepStartSeconds - mCreationTime);
    }

    protected SessionType getType() {
        return mArguments.getType();
    }

    protected int getTimeout() {
        return mArguments.getTimeout(mComponent.getFreshReportRequestConfig().getSessionTimeout());
    }

    public long getId() {
        return mId;
    }

    long getAliveReportOffset() {
        return Math.max(mSleepStartSeconds - TimeUnit.MILLISECONDS.toSeconds(mCreationTime), mLastEventTimeOffset);
    }

    boolean isValid(long reportElapsedRealtime) {
        boolean validID = mId >= MIN_VALID_UI_SESSION_ID;
        boolean consistentRequestParameters = consistentRequestParameters();
        boolean notExpired = isExpired(reportElapsedRealtime, mSystemTimeProvider.elapsedRealtime()) == false;
        YLogger.d("Session id=%d and type %s validity. validID=%b, consistentRequestParameters=%b, notExpired=%b",
                mId, mArguments.getType(), validID, consistentRequestParameters, notExpired);
        return validID && consistentRequestParameters && notExpired;
    }

    private boolean consistentRequestParameters() {
        SessionRequestParams requestParams = getSessionRequestParams();
        boolean consistentRequestParameters = false;
        if (requestParams != null) {
            ReportRequestConfig reportRequestConfig = mComponent.getFreshReportRequestConfig();
            consistentRequestParameters = requestParams.areParamsSameAsInConfig(reportRequestConfig);
        } else {
            YLogger.d("SessionRequestParameters are null. SessionID %d, SessionType %s", mId, mArguments.getType());
        }

        return consistentRequestParameters;
    }

    private long getSessionTimeOffset(long ellapsedRealtime) {
        return TimeUnit.MILLISECONDS.toSeconds(ellapsedRealtime - mCreationTime);
    }

    @VisibleForTesting
    boolean isExpired(long reportElapsedRealtime, long currentElapsedRealtime) {
        final long sleepStartTimeSeconds = mSleepStartSeconds;
        boolean wasRebooted = TimeUnit.MILLISECONDS.toSeconds(currentElapsedRealtime) < sleepStartTimeSeconds;
        final long sinceLastActiveSeconds =
                TimeUnit.MILLISECONDS.toSeconds(reportElapsedRealtime) - sleepStartTimeSeconds;
        long sessionLength = getSessionTimeOffset(reportElapsedRealtime);
        if (YLogger.DEBUG) {
            YLogger.d("Session: id = %d, type = %s, sleepStartTimeSeconds = %d, " +
                            "sinceLastActiveSeconds = %d, sessionTimeout = %d",
                    getId(), getType().toString(), sleepStartTimeSeconds, sinceLastActiveSeconds, getTimeout());
        }
        return wasRebooted
                || sinceLastActiveSeconds >= getTimeout() || sessionLength >= SessionDefaults.SESSION_MAX_LENGTH_SEC;
    }

    synchronized void stopSession() {
        mSessionStorage.clear();
        mSessionRequestParams = null;
    }

    void updateLastActiveTime(long elapsedRealtime) {
        mSessionStorage.putSleepStart(mSleepStartSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedRealtime)).commit();
    }

    long getAndUpdateLastEventTime(long elapsedRealtime) {
        mSessionStorage.putLastEventOffset(mLastEventTimeOffset = getSessionTimeOffset(elapsedRealtime));
        return mLastEventTimeOffset;
    }

    long getLastEventTimeOffset() {
        return mLastEventTimeOffset;
    }

    long getNextReportId() {
        long reportId = mCurrentReportId.getAndIncrement();
        mSessionStorage.putReportId(mCurrentReportId.get()).commit();
        return reportId;
    }

    boolean isAliveNeeded() {
        return mAliveNeeded && getId() > 0;
    }

    public void updateAliveReportNeeded(final boolean value) {
        if (mAliveNeeded != value) {
            mAliveNeeded = value;
            mSessionStorage.putAliveReportNeeded(mAliveNeeded).commit();
        }
    }

    private SessionRequestParams getSessionRequestParams() {
        if (mSessionRequestParams == null) {
            synchronized (this) {
                if (mSessionRequestParams == null) {
                    try {
                        ContentValues params = mComponent.getDbHelper().getSessionRequestParameters(getId(), getType());
                        final String paramsJson = params.getAsString(
                                Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS
                        );

                        if (TextUtils.isEmpty(paramsJson) == false) {
                            JSONObject requestParameters = new JSONObject(paramsJson);
                            mSessionRequestParams = new SessionRequestParams(requestParameters);
                        } else {
                            YLogger.d("SessionRequestParameters is empty sessionID=%d, SessionType=%s",
                                    mId, mArguments.getType());
                        }
                    } catch (Throwable e) {
                        YLogger.e(e, "Something was wrong while getting session's request parameters.");
                    }
                }
            }
        }
        return mSessionRequestParams;
    }

    @Override
    public String toString() {
        return "Session{" +
                "mId=" + mId +
                ", mInitTime=" + mCreationTime +
                ", mCurrentReportId=" + mCurrentReportId +
                ", mSessionRequestParams=" + mSessionRequestParams +
                ", mSleepStartSeconds=" + mSleepStartSeconds +
                '}';
    }

    @VisibleForTesting
    public long getSleepStart() {
        return mSleepStartSeconds;
    }

    @VisibleForTesting
    public long getCreationTime() {
        return mCreationTime;
    }

    static class SessionRequestParams {

        private final String mKitVersionName;
        private final String mKitBuildNumber;
        private final String mAppVersion;
        private final String mAppBuild;
        private final String mOsVersion;
        private final int mApiLevel;
        private final int mAttributionId;

        SessionRequestParams(final JSONObject requestParameters) {
            mKitVersionName = requestParameters
                    .optString(Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME,  null);
            mKitBuildNumber = requestParameters.optString(
                Constants.RequestParametersJsonKeys.ANALYTICS_SDK_BUILD_NUMBER,
                null
            );
            mAppVersion = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_VERSION,  null);
            mAppBuild = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_BUILD,  null);
            mOsVersion = requestParameters.optString(Constants.RequestParametersJsonKeys.OS_VERSION,  null);
            mApiLevel = requestParameters.optInt(Constants.RequestParametersJsonKeys.OS_API_LEVEL, -1);
            mAttributionId = requestParameters.optInt(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, 0);
        }

        boolean areParamsSameAsInConfig(final ReportRequestConfig reportRequestConfig) {
            boolean result = TextUtils.equals(reportRequestConfig.getAnalyticsSdkVersionName(), mKitVersionName) &&
                    TextUtils.equals(reportRequestConfig.getAnalyticsSdkBuildNumber(), mKitBuildNumber) &&
                    TextUtils.equals(reportRequestConfig.getAppVersion(), mAppVersion) &&
                    TextUtils.equals(reportRequestConfig.getAppBuildNumber(), mAppBuild) &&
                    TextUtils.equals(reportRequestConfig.getOsVersion(), mOsVersion) &&
                    mApiLevel == reportRequestConfig.getOsApiLevel() &&
                    mAttributionId == reportRequestConfig.getAttributionId();
            if (result == false) {
                YLogger.d("SessionRequestParameters are not equal: %s and %s", this,
                        requestConfigToString(reportRequestConfig));
            }
            return result;
        }

        private String requestConfigToString(ReportRequestConfig reportRequestConfig) {
            return "ReportRequestConfig{" +
                    "mKitVersionName='" + reportRequestConfig.getAnalyticsSdkVersionName() + '\'' +
                    ", mKitBuildNumber='" + reportRequestConfig.getAnalyticsSdkBuildNumber() + '\'' +
                    ", mAppVersion='" + reportRequestConfig.getAppVersion() + '\'' +
                    ", mAppBuild='" + reportRequestConfig.getAppBuildNumber() + '\'' +
                    ", mOsVersion='" + reportRequestConfig.getOsVersion() + '\'' +
                    ", mApiLevel=" + reportRequestConfig.getOsApiLevel() +
                    '}';
        }

        @Override
        public String toString() {
            return "SessionRequestParams{" +
                    "mKitVersionName='" + mKitVersionName + '\'' +
                    ", mKitBuildNumber='" + mKitBuildNumber + '\'' +
                    ", mAppVersion='" + mAppVersion + '\'' +
                    ", mAppBuild='" + mAppBuild + '\'' +
                    ", mOsVersion='" + mOsVersion + '\'' +
                    ", mApiLevel=" + mApiLevel +
                    ", mAttributionId=" + mAttributionId +
                    '}';
        }
    }
}
