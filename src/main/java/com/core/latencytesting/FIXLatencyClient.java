package com.core.latencytesting;

import com.core.app.AppConstructor;
import com.core.fix.*;
import com.core.fix.connector.FIXConnectionListener;
import com.core.fix.msgs.*;
import com.core.fix.store.FIXMemoryStore;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.fix.util.FixPrinter;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.*;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.util.MatchPriceUtils;
import com.core.testharness.ouch.GenericLatencyMeasurer;
import com.core.testharness.ouch.LatencyTestClient;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class FIXLatencyClient implements
        FIXConnectionListener,LatencyTestClient,FixExecutionReportListener, TimerHandler

{
    private final FixTag account;
    private final FixTag side;
    private final FixTag orderQty;
    private final FixTag orderType;
    private final FixTag symbol;
    private final FixTag price;
    private final FixTag clOrdId;
    private final String acct;
    private final FixTag execID;


    private final TimeSource source;
    private final MatchMessages messages;
    private final SecurityService<BaseSecurity> securities;

    private final FIXPortInfo info;
    private final SimpleFixTestStateMachine stateMachine;
    private final FixClientConnector fixConnector;
    private final FixStore store;
    private final Log log;
    private final TimerService timerService;
    private final FixDispatcher fixDispatcher;
    private boolean connected;
    private Random random;
    private GenericLatencyMeasurer latencyMeasurer;

    @AppConstructor
    public FIXLatencyClient(Log log,
                            TCPSocketFactory socketFactory,
                            TimerService timeService,
                            TimeSource timeSource,
                            MatchMessages msgs,
                            String host,
                            short port,
                            int version,
                            String senderCompID,
                            String targetCompID,
                            String accountName) throws IOException {
        this(log,
                timeSource,
                timeService,
                msgs,
                new StubMoldSender(),
                new FixClientConnector(log, socketFactory, timeService, host, port),
                new InlineFixParser(),
                new InlineFixWriter(timeSource, version, targetCompID, senderCompID),
                new FixDispatcher(log),
                new FIXPortInfo(port, version, senderCompID, targetCompID), accountName);
    }

    public FIXLatencyClient(Log log,
                            TimeSource source,
                            TimerService timerService,
                            MatchMessages messages,
                            MatchCommandSender sender,
                            FixClientConnector fixConnector,
                            FixParser parser,
                            FixWriter writer,
                            FixDispatcher fixDispatcher,
                            FIXPortInfo info, String accountName) {
        this.store=new FIXMemoryStore(writer,fixConnector);
        this.source = source;
        this.messages = messages;
        this.securities = SecurityService.create(log, source);
        this.info = info;
        this.log=log;

        this.account = parser.createReadWriteFIXTag(FixTags.Account);
        this.side = parser.createReadWriteFIXTag(FixTags.Side);
        this.orderQty = parser.createReadWriteFIXTag(FixTags.OrderQty);
        this.orderType = parser.createReadWriteFIXTag(FixTags.OrdType);
        this.symbol = parser.createReadWriteFIXTag(FixTags.Symbol);
        this.price = parser.createReadWriteFIXTag(FixTags.Price);
        this.clOrdId = parser.createReadWriteFIXTag(FixTags.ClOrdID);
        this.execID=parser.createReadWriteFIXTag(FixTags.ExecID);

        this.stateMachine = new SimpleFixTestStateMachine(log, fixDispatcher, timerService, sender, parser, store, fixConnector, messages, true, true, info.getMinorVersion(), info.getSenderCompID(), info.getTargetCompID());

        this.fixConnector = fixConnector;
        this.fixConnector.init(parser, stateMachine);
        this.fixConnector.addListener(new FixPrinter(log));
        this.fixConnector.addConnectionListener(this);
        this.acct=accountName;
        this.fixDispatcher=fixDispatcher;
        fixDispatcher.subscribe(this);
        this.store.init(writer, this.fixConnector);

        this.random=new Random();
        fixDispatcher.subscribe(this);
        this.timerService=timerService;
        timerService.scheduleTimer(TimeUtils.NANOS_PER_MICRO,this);
        try {
            fixConnector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setLatencyMeasurer(GenericLatencyMeasurer latencyMeasurer){
        this.latencyMeasurer =latencyMeasurer;
    }

    @Override
    public void onConnect() {
        log.info(log.log().add("Connected -- sending login"));

        // send logon message
        this.stateMachine.resetSequenceNums();
        this.connected = true;
        this.stateMachine.sendLogonCommand(60);
        this.stateMachine.onMatchOutbound(createLoginCommand());
    }

    private MatchOutboundEvent createLoginCommand() {

        MatchOutboundCommand cmd= messages.getMatchOutboundCommand();
        cmd.setFixMsgType(FixMsgTypes.Logon);
        cmd.setContributorID((short)0);
        return cmd.toEvent();
    }

    @Override
    public void onDisconnect() {
        log.info(log.log().add("Disconnected fix connection"));
        this.stateMachine.resetSequenceNums();
        this.connected = false;
    }

    @Override
    public long sendNewOrder(String security, Boolean isBuy, int qty, double priceVal, String trader, boolean isIOC) {
        if( !connected )
            return 0;

        FixWriter writer = this.store.createMessage(FixMsgTypes.NewOrderSingle);
        int randomID = Math.abs(random.nextInt());
        String stringID = String.format("%08d", randomID);
        long clientIDInLong=Long.valueOf(stringID);
        writer.writeString(clOrdId,stringID);
        writer.writeChar(side,FixConstants.Side.Buy);

        writer.writeString(symbol,str(security));

        writer.writeNumber(orderQty,1*MatchConstants.QTY_MULTIPLIER);
        writer.writeString(account,acct);
        writer.writeChar(orderType,'2');
        writer.writePrice(price, MatchPriceUtils.toLong(priceVal), MatchConstants.IMPLIED_DECIMALS);
        this.stateMachine.incInboundSeqNo();
        this.store.finalizeBusinessMessage();
        latencyMeasurer.start(clientIDInLong);

        return clientIDInLong;

    }
    private static ByteBuffer str(String str) {
        return ByteBuffer.wrap(str.getBytes());
    }
    @Override
    public boolean isLoggedIn() {
        return stateMachine.isLoggedIn() ;
    }

    @Override
    public GenericLatencyMeasurer getOrderAcceptLatencyMeasurer() {
        return latencyMeasurer;
    }

    @Override
    public void onFixExecutionReport() {
        if(BinaryUtils.toString(execID.getValue()).startsWith("D")){
            long clID=Long.valueOf(BinaryUtils.toString(clOrdId.getValue()));
            latencyMeasurer.stop(clID);
        }



    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        this.fixConnector.resendComplete();
        timerService.scheduleTimer(TimeUtils.NANOS_PER_MICRO,this);

    }
}
