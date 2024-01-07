package com.core.match.sequencer;

import com.core.match.services.events.SystemEventListener;
import com.core.match.util.MessageUtils;
import com.core.util.TextUtils;
import com.core.util.TimeUtils;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.time.LocalDate;

import static com.core.match.msgs.MatchConstants.SystemEvent.Close;
import static com.core.match.msgs.MatchConstants.SystemEvent.Open;


/**
 * Created by jgreco on 6/27/15.
 */
class SequencerMarketHoursService implements TimerHandler, MarketHoursService, SystemEventListener {
    private final TimerService timers;
    private final TimeSource time;

    private final SequencerMarketHoursServiceListener listener;
    private final LocalDate date;

    private long openTime;
    private long closeTime;
    private int startTimer;
    private int closeTimer;

    private boolean marketOpen;

    public SequencerMarketHoursService(TimeSource time,
                                       TimerService timers,
                                       SequencerMarketHoursServiceListener listener,
                                       String marketOpen,
                                       String marketClose) {
        this.timers = timers;
        this.time = time;
        this.listener = listener;
        this.date = TimeUtils.toLocalDateTime(time.getTimestamp(), MessageUtils.zoneID()).toLocalDate();
        setMarketOpen(marketOpen);
        setMarketClose(marketClose);
    }

    public void setMarketOpen(String marketOpen) {
        startTimer = timers.cancelTimer(startTimer);

        openTime = TimeUtils.toNanos(date, TextUtils.parseHHMM(marketOpen), MessageUtils.zoneID());
        long diffToOpen =  openTime - time.getTimestamp();
        if (diffToOpen > 0)  {
            startTimer = timers.scheduleTimer(diffToOpen, this, Open);
        }
        else {
            onTimer(0, Open);
        }
    }

    public void setMarketClose(String marketClose) {
        closeTimer = timers.cancelTimer(closeTimer);

        closeTime = TimeUtils.toNanos(date, TextUtils.parseHHMM(marketClose), MessageUtils.zoneID());
        long diffToClose = closeTime - time.getTimestamp();
        if (diffToClose > 0) {
            closeTimer = timers.scheduleTimer(diffToClose, this, Close);
        }
        else {
            onTimer(0, Close);
        }
    }

    public void forceClose() {
        this.marketOpen = false;
        onTimer(0, Close);
    }

    public void forceOpen() {
        this.marketOpen = true;
        onTimer(0, Open);
    }

    public void checkStatus() {
        if(!isMarketOpen() && time.getTimestamp() > openTime && time.getTimestamp() < closeTime) {
            onTimer(0, Open);
        }
        else if(isMarketOpen() && time.getTimestamp() > closeTime) {
            onTimer(0, Close);
        }
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        if (referenceData == Open) {
            if (listener.onOpen()) {
                this.marketOpen = true;
            }
        }
        else if (referenceData == Close) {
            if (listener.onClose()) {
                this.marketOpen = false;
            }
        }
    }

    @Override
    public boolean isMarketOpen() {
        return marketOpen;
    }

    @Override
    public void onOpen(long timestamp) {
        marketOpen=true;
    }

    @Override
    public void onClose(long timestamp) {
        marketOpen=false;
    }
}
