package com.core.util.time;


import com.core.match.util.MessageUtils;
import com.core.util.TimeUtils;

/**
 * Created by jgreco
 */
public class SimulatedTimeSource
		implements
		TimeSource {
	public long timestamp;

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void advanceTime(long nanos) {
		setTimestamp(getTimestamp() + nanos);
	}

	public void setDate(int dateInt) {
		setTimestamp(TimeUtils.toNanos(TimeUtils.toLocalDate(dateInt), MessageUtils.zoneID()));
	}
}
