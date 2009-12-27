package com.plexobject.rbac.metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

/**
 * This class maintains profiling metrics. This is thread safe class.
 * 
 * @author Shahzad Bhatti
 * 
 */
public class Metric {
    private final String name;
    private AtomicLong totalDuration;
    private AtomicLong totalCalls;
    private static final long ONE_MILLI_SEC = 1000000L;
    private static final long ONE_SEC = ONE_MILLI_SEC * 1000;

    private static final Map<String, Metric> METRICS = new ConcurrentHashMap<String, Metric>();

    // This class can only be created through factory-method and it's using
    // package scope for testing.
    Metric(final String name) {
        this.name = name;
        this.totalCalls = new AtomicLong();
        this.totalDuration = new AtomicLong();
    }

    /**
     * This method finds or creates metrics
     * 
     * @param name
     *            - name of metrics
     * @return Metric instance
     */
    public static Metric getMetric(final String name) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("name not specified");
        }
        Metric metric = null;

        synchronized (name.intern()) {
            metric = METRICS.get(name);
            if (metric == null) {
                metric = new Metric(name);
                METRICS.put(name, metric);
            }
        }
        return metric;
    }

    /**
     * This method finds metric and creates a new timer for it. The caller must
     * call lapse or stop to add the results back to the metric.
     * 
     * @param name
     *            - name of metric
     * @return Timer instance
     */
    public static Timing newTiming(final String name) {
        return getMetric(name).newTiming();
    }

    /**
     * This method creates a new timer for it. The caller must call lapse or
     * stop to add the results back to the metric.
     * 
     * @return Timer instance
     */

    public Timing newTiming() {
        return new Timing(this);
    }

    /**
     * 
     * @return total duration in nano-secs
     */
    public long getTotalDurationInNanoSecs() {
        return totalDuration.get();
    }

    /**
     * 
     * @return average duration in nano-secs
     */
    public double getAverageDurationInNanoSecs() {
        return getTotalDurationInNanoSecs() / (double) getTotalCalls();
    }

    /**
     * 
     * @return total duration in milli-secs
     */
    public long getTotalDurationInMillis() {
        return (long) (getAverageDurationInNanoSecs() / ONE_MILLI_SEC);
    }

    /**
     * 
     * @return average duration in nano-secs
     */
    public double getAverageDurationInMillis() {
        return getAverageDurationInNanoSecs() / ONE_MILLI_SEC;
    }

    /**
     * 
     * @return number of calls invoked
     */
    public long getTotalCalls() {
        return totalCalls.get();
    }

    void finishedTimer(final int totalCalls, final long totalDuration) {
        this.totalCalls.addAndGet(totalCalls);
        this.totalDuration.addAndGet(totalDuration);
    }

    @Override
    public String toString() {
        double nanoAvg = getAverageDurationInNanoSecs();
        String avgTime = nanoAvg > ONE_SEC ? String.format("%.2f secs", nanoAvg
                / ONE_SEC) : nanoAvg > ONE_MILLI_SEC ? String.format(
                "%.2f millis", nanoAvg / ONE_MILLI_SEC) : String.format(
                "%.2f nanos", nanoAvg);
        return new ToStringBuilder(this).append("name", name).append("calls",
                getTotalCalls()).append("average duration", avgTime).toString();
    }
}
