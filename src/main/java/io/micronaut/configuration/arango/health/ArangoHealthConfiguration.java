package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 13.08.2021
 */
@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties(ArangoSettings.PREFIX + ".health")
public class ArangoHealthConfiguration {

    private boolean enabled = true;
    private long timeoutInMillis = 5000;
    private int retry = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTimeoutInMillis() {
        return timeoutInMillis;
    }

    public void setTimeoutInMillis(long timeoutInMillis) {
        if (timeoutInMillis < 0)
            throw new ConfigurationException("Timeout for health can not be less than 0");
        this.timeoutInMillis = timeoutInMillis;
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
        return "[enabled=" + enabled +
                ", timeoutInMillis=" + timeoutInMillis +
                ", retry=" + retry + ']';
    }
}
