package com.core;

import com.core.match.util.MessageUtils;
import com.core.nio.SimulatedSelectorService;
import com.core.util.TimeUtils;
import com.core.util.file.File;
import com.core.util.file.FileFactory;
import com.core.util.log.SystemOutLog;
import com.core.util.tcp.StubTCPSocketFactory;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.time.TimerService;
import com.core.util.time.TimerServiceImpl;
import com.core.util.udp.StubUDPSocketFactory;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;

import static org.mockito.Matchers.anyString;

/**
 * Created by jgreco on 6/25/15.
 */
public class GenericTest {
    protected final SimulatedTimeSource timeSource;
    protected final SystemOutLog log;
    protected final SimulatedSelectorService select;
    protected final StubTCPSocketFactory tcpSockets;
    protected final StubUDPSocketFactory udpSockets;
    protected final FileFactory fileFactory;
    protected final TimerService timers;

    public GenericTest() {
        this.timeSource = new SimulatedTimeSource();
        this.log = new SystemOutLog("Test", "TEST", this.timeSource);
        this.log.setDebug(true);
        this.select = new SimulatedSelectorService(this.log, this.timeSource);
        this.tcpSockets = new StubTCPSocketFactory();
        this.udpSockets = new StubUDPSocketFactory();
        this.fileFactory = Mockito.mock(FileFactory.class);
        try
		{
			Mockito.when(fileFactory.createFile(anyString(), anyString())).thenReturn(new File() {

				@Override
				public int write(ByteBuffer buf) throws IOException
				{
					return 0;
				}

				@Override
				public long size()
				{
					return 0;
				}

				@Override
				public int write(ByteBuffer buf, long offset) throws IOException {
					return 0;
				}

				@Override
				public int read(ByteBuffer buffer) throws IOException
				{
					return 0;
				}

				@Override
				public int read(ByteBuffer buffer, long offset) throws IOException
				{
					return 0;
				}
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
        this.timers = new TimerServiceImpl(log, timeSource);

		timeSource.setTimestamp(TimeUtils.toNanos(LocalDate.now(), MessageUtils.zoneID()));
    }

    protected void advanceTime(int millis) {
        timeSource.advanceTime(millis * TimeUtils.NANOS_PER_MILLI);
        select.runOnce();
    }
}
