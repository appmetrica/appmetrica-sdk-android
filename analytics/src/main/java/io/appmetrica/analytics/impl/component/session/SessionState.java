package io.appmetrica.analytics.impl.component.session;

public class SessionState {

    private long mSessionId;
    private long mReportId;
    private long mReportTime;
    private SessionType mSessionType;

    public SessionState() {}

    public long getSessionId() {
        return mSessionId;
    }

    public SessionState withSessionId(final long sessionId) {
        this.mSessionId = sessionId;
        return this;
    }

    public SessionType getSessionType() {
        return mSessionType;
    }

    public SessionState withSessionType(final SessionType sessionType) {
        this.mSessionType = sessionType;
        return this;
    }

    public long getReportId() {
        return mReportId;
    }

    public SessionState withReportId(final long reportId) {
        this.mReportId = reportId;
        return this;
    }

    public long getReportTime() {
        return mReportTime;
    }

    public SessionState withReportTime(final long reportTime) {
        this.mReportTime = reportTime;
        return this;
    }
}
