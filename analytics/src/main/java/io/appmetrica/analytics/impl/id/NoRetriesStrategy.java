package io.appmetrica.analytics.impl.id;

public class NoRetriesStrategy extends TimesBasedRetryStrategy {

    public NoRetriesStrategy() {
        super(1, 0);
    }
}
