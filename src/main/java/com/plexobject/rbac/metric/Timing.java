package com.plexobject.rbac.metric;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;

/**
 * This class collects timing data but it's not thread safe.
 * 
 * @author Shahzad Bhatti
 * 
 */
public class Timing {
    private static final Logger LOGGER = Logger.getLogger(Timing.class);
    private static final long ONE_MILL_SEC = 1000000L;

    private static final long LOGGING_THRESHOLD_IN_NANOSECS = ONE_MILL_SEC
            * Configuration.getInstance().getInteger(
                    "timer.logging.threshold.millis", 1000);
    private final Metric metric;

    private static class TimingData {
        private long started;
        private long ended;

        private TimingData() {
            this.started = System.nanoTime();
        }

        private void stop() {
            this.ended = System.nanoTime();
        }

        private long getTotalDurationInNanosecs() {
            return ended - started;
        }
    }

    private TimingData currentTimingData;
    private Collection<TimingData> elapsedTimingData;

    // This class can only be created by Metric and it's using
    // package scope for testing.
    Timing(final Metric metric) {
        this.metric = metric;
        this.currentTimingData = new TimingData();
    }

    /**
     * stop - stops the timer
     * 
     * @throws IllegalStateException
     *             - if timer is used again after stop
     */
    public void stop() {
        stop("");
    }

    /**
     * stop - stops the timer
     * 
     * @throws IllegalStateException
     *             - if timer is used again after stop
     */
    public void stop(final String logMessage) {
        if (currentTimingData == null) {
            throw new IllegalStateException("Timer has already been stopped");
        }
        currentTimingData.stop();

        final int totalCalls = elapsedTimingData == null ? 1
                : elapsedTimingData.size() + 1;
        long totalDurationInNanosecs = currentTimingData
                .getTotalDurationInNanosecs();
        if (elapsedTimingData != null) {
            for (TimingData data : elapsedTimingData) {
                totalDurationInNanosecs += data.getTotalDurationInNanosecs();
            }
        }
        metric.finishedTimer(totalCalls, totalDurationInNanosecs);
        if (totalDurationInNanosecs > LOGGING_THRESHOLD_IN_NANOSECS
                && LOGGER.isInfoEnabled()) {
            LOGGER.info("ended " + logMessage + " for metrics " + metric
                    + getSystemStats());
        }
        currentTimingData = null;

    }

    /**
     * lapse - marks current timing and starts another timer
     * 
     * @throws IllegalStateException
     *             - if timer is already stopped
     */
    public void lapse() {
        lapse("");
    }

    /**
     * lapse - marks current timing and starts another timer
     * 
     * @param logMessage
     *            - will print log message if duration exceeds logging threshold
     * @throws IllegalStateException
     *             - if timer is already stopped
     */
    public void lapse(final String logMessage) {
        if (currentTimingData == null) {
            throw new IllegalStateException("Timer has already been stopped");
        }
        currentTimingData.stop();
        getElapsedTimingData().add(currentTimingData);
        if (currentTimingData.getTotalDurationInNanosecs() > LOGGING_THRESHOLD_IN_NANOSECS
                && LOGGER.isInfoEnabled()) {
            LOGGER.info("ended " + logMessage + " for metrics " + metric
                    + getSystemStats());
        }
        currentTimingData = null;

    }

    private Collection<TimingData> getElapsedTimingData() {
        if (elapsedTimingData == null) {
            elapsedTimingData = new ArrayList<TimingData>();
        }
        return elapsedTimingData;
    }

    private static String getSystemStats() {
        final StringBuilder sb = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        sb.append(", load: ").append(
                String.format("%.2f", ManagementFactory
                        .getOperatingSystemMXBean().getSystemLoadAverage()));
        sb.append(", memory(M): ").append(runtime.freeMemory() / 1024 / 1024)
                .append("/").append(runtime.totalMemory() / 1024 / 1024);
        sb.append(", threads: ").append(Thread.activeCount());
        return sb.toString();
    }
}
