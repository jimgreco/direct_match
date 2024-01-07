package com.core.match.fix.mdclient;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Dispatcher;
import com.core.fix.FIXPortInfo;
import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.InlineFixParser;
import com.core.fix.InlineFixWriter;
import com.core.fix.connector.FIXConnectionListener;
import com.core.fix.connector.FixClientTcpConnector;
import com.core.fix.connector.FixConnector;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.msgs.FixMarketDataSnapshotListener;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixTags;
import com.core.fix.store.FixFileStore;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.fix.util.FixPrinter;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.fix.FixStateMachine;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchInboundListener;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchQuoteCommand;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.util.PriceUtils;
import com.core.util.TextUtils;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.util.Iterator;

import static com.core.match.msgs.MatchConstants.IMPLIED_DECIMALS;
import static com.core.match.msgs.MatchConstants.Venue.InteractiveData;

/**
 * Created by jgreco on 8/29/15.
 */
public class FIXMarketDataClient extends MatchApplication implements
        FIXConnectionListener,
        MatchInboundListener,
        FixMarketDataSnapshotListener {
    private final FixTag mdReqId;
    private final FixTag subscriptionReqType;
    private final FixTag marketDepth;
    private final FixTag mdUpdateType;
    private final FixTag noMdEntryTypes;
    private final FixTag mdEntryType;
    private final FixTag noRelatedSym;
    private final FixTag noMdEntries;
    private final FixTag mdEntryPx;
    private final FixTag securityIdSource;
    private final FixTag securityId;
    private final FixTag mdEntryTime;

    private final TimeSource source;
    private final MatchMessages messages;
    private final SecurityService<BaseSecurity> securities;

    private final FIXPortInfo info;
    private final FixStateMachine stateMachine;
    private final FixConnector fixConnector;
    private final FixStore store;
    private boolean connected;

    @AppConstructor
    public FIXMarketDataClient(Log log,
                               TCPSocketFactory socketFactory,
                               FileFactory fileFactory,
                               TimerService timeService,
                               TimeSource timeSource,
                               Dispatcher dispatcher,
                               MatchCommandSender sender,
                               MatchMessages msgs,
                               @Param(name = "Host") String host,
                               @Param(name = "Port") short port,
                               @Param(name = "Version") int version,
                               @Param(name = "SenderCompId") String senderCompID,
                               @Param(name = "TargetCompId") String targetCompID) throws IOException {
        this(log,
                timeSource,
                timeService,
                dispatcher,
                sender,
                msgs,
                new FixClientTcpConnector(log, socketFactory, sender, timeService, host, port),
                new InlineFixParser(),
                new InlineFixWriter(timeSource, version, targetCompID, senderCompID),
                new FixFileStore(fileFactory, log, sender.getName()),
                new FixDispatcher(log),
                new FIXPortInfo(port, version, senderCompID, targetCompID));
    }

    public FIXMarketDataClient(Log log,
                               TimeSource source,
                               TimerService timerService,
                               Dispatcher dispatcher,
                               MatchCommandSender sender,
                               MatchMessages messages,
                               FixConnector fixConnector,
                               FixParser parser,
                               FixWriter writer,
                               FixStore store,
                               FixDispatcher fixDispatcher,
                               FIXPortInfo info) {
        super(log, sender);
        this.source = source;
        this.messages = messages;
        this.securities = SecurityService.create(log, source);
        this.info = info;

        this.mdReqId = parser.createWriteOnlyFIXTag(FixTags.MDReqID);
        this.subscriptionReqType = parser.createWriteOnlyFIXTag(FixTags.SubscriptionRequestType);
        this.marketDepth = parser.createWriteOnlyFIXTag(FixTags.MarketDepth);
        this.mdUpdateType = parser.createWriteOnlyFIXTag(FixTags.MDUpdateType);
        this.noMdEntryTypes = parser.createWriteOnlyFIXTag(FixTags.NoMDEntryTypes);
        this.noRelatedSym = parser.createWriteOnlyFIXTag(FixTags.NoRelatedSym);
        this.securityIdSource = parser.createWriteOnlyFIXTag(FixTags.SecurityIDSource);

        this.securityId = parser.createReadWriteFIXTag(FixTags.SecurityID);
        this.noMdEntries = parser.createReadWriteFIXTag(FixTags.NoMDEntries);
        this.mdEntryType = parser.createReadWriteFIXGroupTag(FixTags.MDEntryType);
        this.mdEntryPx = parser.createReadWriteFIXGroupTag(FixTags.MDEntryPx);
        this.mdEntryTime = parser.createReadWriteFIXGroupTag(FixTags.MDEntryTime);

        this.stateMachine = new FixStateMachine(log, fixDispatcher, timerService, sender, parser, store, fixConnector, messages, true, true, info.getMinorVersion(), info.getSenderCompID(), info.getTargetCompID());

        this.fixConnector = fixConnector;
        this.fixConnector.init(parser, stateMachine);
        this.fixConnector.addListener(new FixPrinter(log));
        this.fixConnector.addConnectionListener(this);

        this.store = store;
        this.store.init(writer, this.fixConnector);

        sender.addContributorDefinedListener(stateMachine);

        fixDispatcher.subscribe(this);

        dispatcher.subscribe(this);
        dispatcher.subscribe(stateMachine);
        dispatcher.subscribe(securities);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister register) {
        if (info != null) {
            info.addStatus(register);
        }
        stateMachine.addStatus(register);
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater register) {
        stateMachine.updateStatus();
    }

    @Override
    protected void onActive() {
        try {
            fixConnector.open();
        } catch (IOException e) {
            throw new CommandException("Could not open FIX connector");
        }
    }

    @Override
    protected void onPassive() {
        try {
            fixConnector.close();
        } catch (IOException e) {
            throw new CommandException("Error closing server socket");
        }
    }

    @Exposed(name = "setInbound")
    public void setNextInboundSeqNum(@Param(name = "SeqNum") int seqNum) {
        stateMachine.setNextInboundSeqNum(seqNum);
    }

    @Exposed(name = "setOutbound")
    public void setNextOutboundSeqNum(@Param(name = "SeqNum") int seqNum) {
        stateMachine.setNextOutboundSeqNo(seqNum, false);
    }

    @Exposed(name = "sendTestRequest")
    public void sendTestRequest() {
        if (!canSend()) {
            throw new CommandException("Cannot send a test request while a command is in flight");
        }
        stateMachine.sendTestRequest();
    }

    @Exposed(name = "sendRejectRequest")
    public void sendRejectRequest() {
        if (!canSend()) {
            throw new CommandException("Cannot send a test request while a command is in flight");
        }
        stateMachine.sendReject();
    }

    @Exposed(name = "sendHeartbeat")
    public void sendHeartbeat() {
        if (!canSend()) {
            throw new CommandException("Cannot send a heartbeat while a command is in flight");
        }
        stateMachine.sendHeartbeat();
    }

    @Override
    public void onFixMarketDataSnapshot() {
    	stateMachine.incInboundSeqNo();
        BaseSecurity security = this.securities.getByCUSIP(securityId.getValue());
        if( security == null )
        {
        	log.error(log.log().add("Could not find security for CUSIP: ").add(securityId.getValue()));
        	return;
        }

        long bidPrice = 0, askPrice = 0, bidTime = 0, askTime = 0;

        int noMDEntries = noMdEntries.getValueAsInt();
        for (int i=0; i<Math.min(2, noMDEntries); i++) {
            char type = mdEntryType.getValueAsChar();
            if (type == FixConstants.MDEntryType.Bid) {
                bidPrice = mdEntryPx.getValueAsPrice(IMPLIED_DECIMALS);
                bidTime = TextUtils.parseHHMMSSsss(mdEntryTime.getValue(), source.getTimestamp());
            }
            else if (type == FixConstants.MDEntryType.Offer) {
                askPrice = mdEntryPx.getValueAsPrice(IMPLIED_DECIMALS);
                askTime = TextUtils.parseHHMMSSsss(mdEntryTime.getValue(), source.getTimestamp());
            }
        }

        long timestamp = Math.max(bidTime, askTime);
        bidPrice = round(bidPrice);
        askPrice = round(askPrice);
        if(bidPrice>=askPrice){
            log.info(log.log().add("Bid Price >= Ask Price. Quote Dropped.").add(bidPrice).add(" >= ").add(askPrice));
            return;
        }

        MatchQuoteCommand quote = messages.getMatchQuoteCommand();
        quote.setVenueCode(InteractiveData);
        quote.setSecurityID(security.getID());
        quote.setBidPrice(bidPrice);
        quote.setOfferPrice(askPrice);
        quote.setSourceTimestamp(timestamp);
        send(quote);
    }
    
    private long round(long price) {
        long eighth = PriceUtils.getEighth(IMPLIED_DECIMALS);
        long sixteenth = PriceUtils.getSixteenth(IMPLIED_DECIMALS);
        long remainder = price % eighth;

        if (remainder == 0) {
            return price;
        }

        if (remainder >= sixteenth) {
            // If more then half, then round up
            return price + (eighth - remainder);
        }

        // if less than half, then round down
        return price - remainder;
    }

    @Override
    public void onConnect() {
        log.info(log.log().add("Connected -- sending login"));

        if (!canSend()) {
            log.info(log.log().add("Cannot send core message now.  Disconnecting."));
            try {
                fixConnector.close();
            } catch (IOException e) {
                log.error(log.log().add("Connection exception : ").add(e.getMessage()));
            }
            return;
        }

        // send logon message
        this.stateMachine.resetSequenceNums();
    	this.connected = true;
        this.stateMachine.sendLogonCommand(60);
    }

    @Override
    public void onDisconnect() {
        log.info(log.log().add("Disconnected fix connection"));
        this.stateMachine.resetSequenceNums();
        this.connected = false;
    }
    
    private void sendMarketDataRequest() {
    	if( !connected )
    		return;

        Iterator<BaseSecurity> iterator = securities.iterator();
        while (iterator.hasNext()) {
            BaseSecurity baseSecurity = iterator.next();
            if(baseSecurity.isMultiLegInstrument()){
                //TODO: MD for multiLeg
                return;
            }

            Bond security=(Bond) baseSecurity;
        	log.info(log.log().add("Sending market data request on CUSIP: " + security.getCUSIP()));
        	FixWriter writer = this.store.createMessage(FixMsgTypes.MarketDataRequest);
	        writer.writeString(mdReqId, security.getName());
	        writer.writeNumber(subscriptionReqType, 1);
	        writer.writeNumber(marketDepth, 1);
	        writer.writeNumber(mdUpdateType, 0);

	        writer.writeNumber(noMdEntryTypes, 2);
	        writer.writeChar(mdEntryType, FixConstants.MDEntryType.Bid); // bid
            writer.writeChar(mdEntryType, FixConstants.MDEntryType.Offer); // offer

	        // group of securities
	        writer.writeNumber(noRelatedSym, 1);
            writer.writeString(securityId, security.getCUSIP());
            writer.writeNumber(securityIdSource, 1);
            this.store.finalizeBusinessMessage();
        }
    }

	@Override
	public void onMatchInbound(MatchInboundEvent msg) {
		if (msg.getContributorID() == this.getContribID() && msg.getFixMsgType() == FixMsgTypes.Logon) {
			log.info(log.log().add("Successfully logged on -- sending MDRequest"));
			sendMarketDataRequest();
		}
	}
}
