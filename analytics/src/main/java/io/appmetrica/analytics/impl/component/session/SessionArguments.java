package io.appmetrica.analytics.impl.component.session;

public class SessionArguments {

    public final long creationElapsedRealtime;
    public final long creationTimestamp;

    public SessionArguments(long creationElapsedRealtime, long creationTimestamp) {
        this.creationElapsedRealtime = creationElapsedRealtime;
        this.creationTimestamp = creationTimestamp;
    }
}
