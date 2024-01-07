package com.core.testharness.ouch;

import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;
import com.gs.collections.impl.list.mutable.primitive.LongArrayList;
import com.gs.collections.impl.map.mutable.primitive.LongLongHashMap;

/**
 * Created by jgreco on 1/30/16.
 */
public class GenericLatencyMeasurer implements LatencyMeasurer {
    private final LongLongHashMap clOrdIDToLatencyMap = new LongLongHashMap();
    private final LongArrayList latencies = new LongArrayList();
    private final TimeSource timeSource;
    private final Log log;

    public GenericLatencyMeasurer(TimeSource timeSource, Log log) {
        this.timeSource = timeSource;
        this.log = log;
    }



    @Override
    public void start(long id) {
        clOrdIDToLatencyMap.put(id, timeSource.getTimestamp());
    }

    @Override
    public void stop(long id) {
        long stop = timeSource.getTimestamp();
        long start = clOrdIDToLatencyMap.getIfAbsent(id, 0);
        if (start == 0) {
            //log.error(log.log().add("Unknown ClOrdID ").add(id));
            return;
        }
        clOrdIDToLatencyMap.removeKey(id);
        latencies.add(stop - start);
    }

    public LatencyStats measure() {
        return new LatencyStats(latencies.toSortedArray(), clOrdIDToLatencyMap.size());
    }

    public int getOutstanding() {
        return clOrdIDToLatencyMap.size();
    }

    public static class LatencyStats {
        private final long[] latencies;
        private final int outstanding;

        public LatencyStats(long[] latencies, int outstanding) {
            this.latencies = latencies;
            this.outstanding = outstanding;
        }

        public int getOutstanding() {
            return outstanding;
        }

        public int getMeasurements() {
            return latencies.length;
        }

        public int getPercentileInMicros(double percentile) {
            if(latencies.length==0){
                return 0;
            }
            int position = (int) Math.round(percentile * latencies.length);
            long latencyNanos = latencies[Math.min(position, latencies.length - 1)];
            return (int) (latencyNanos / TimeUtils.NANOS_PER_MICRO);
        }
        public long getMean()
        {
            long sum = 0L;
            for(long a : latencies)
                sum += a;
            return sum/(TimeUtils.NANOS_PER_MICRO*getMeasurements());
        }

        public long getVariance()
        {
            long mean = getMean();
            long temp = 0;
            for(long a :latencies) {
                a=a/TimeUtils.NANOS_PER_MICRO;
                temp += (mean - a) * (mean - a);
            }
            return temp/getMeasurements();
        }

        double getStdDev()
        {
            return Math.sqrt(getVariance());
        }



    }
}
