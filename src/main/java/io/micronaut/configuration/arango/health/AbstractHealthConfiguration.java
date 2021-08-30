package io.micronaut.configuration.arango.health;

import io.micronaut.context.exceptions.ConfigurationException;

import java.time.Duration;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 13.08.2021
 */
public abstract class AbstractHealthConfiguration {

    private boolean enabled = true;
    private Duration timeout = Duration.ofSeconds(5);
    private int retry = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        if (timeout.isNegative())
            throw new ConfigurationException("Timeout for health can not be less than 0");
        this.timeout = timeout;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        if (retry < 1)
            throw new ConfigurationException("Retry for health can not be less than 1");
        this.retry = retry;
    }

    @Override
    public String toString() {
        return "[enabled=" + enabled + ", timeout=" + timeout + ", retry=" + retry + ']';
    }
}
