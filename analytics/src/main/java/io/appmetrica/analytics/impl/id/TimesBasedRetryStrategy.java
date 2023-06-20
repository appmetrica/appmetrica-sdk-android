package io.appmetrica.analytics.impl.id;

public class TimesBasedRetryStrategy implements RetryStrategy {

    private final int maxAttempts;
    private final int timeout;
    private int attempts = 0;

    public TimesBasedRetryStrategy(int maxAttempts, int timeout) {
        this.maxAttempts = maxAttempts;
        this.timeout = timeout;
    }

    @Override
    public boolean nextAttempt() {
        return attempts++ < maxAttempts;
    }

    @Override
    public void reset() {
        attempts = 0;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}
